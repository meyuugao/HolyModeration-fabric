package me.yuugao.holymoderation.client.util.command;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import java.util.Collection;
import java.util.List;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;

public abstract class Argument {
    private final String name;

    protected Argument(String name) {
        this.name = name;
    }

    public static Argument text(String name) {
        return new Argument(name) {
            @Override
            public ArgumentType<?> brigadierType() {
                return StringArgumentType.greedyString();
            }

            @Override
            public SuggestionProvider<FabricClientCommandSource> suggestions() {
                return null;
            }
        };
    }


    public static Argument word(String name) {
        return new Argument(name) {
            @Override
            public ArgumentType<?> brigadierType() {
                return StringArgumentType.string();
            }

            @Override
            public SuggestionProvider<FabricClientCommandSource> suggestions() {
                return null;
            }
        };
    }

    public static Argument integer(String name, int min, int max, Collection<String> presets) {
        return new Argument(name) {
            @Override
            public ArgumentType<?> brigadierType() {
                return IntegerArgumentType.integer(min, max);
            }

            @Override
            public SuggestionProvider<FabricClientCommandSource> suggestions() {
                return (ctx, builder) -> {
                    String remaining = builder.getRemaining().toLowerCase();
                    for (String p : presets) {
                        if (p.toLowerCase().startsWith(remaining)) builder.suggest(p);
                    }
                    return builder.buildFuture();
                };
            }
        };
    }

    public static Argument choice(String name, List<String> options) {
        return new Argument(name) {
            @Override
            public ArgumentType<?> brigadierType() {
                return StringArgumentType.string();
            }

            @Override
            public SuggestionProvider<FabricClientCommandSource> suggestions() {
                return (ctx, builder) -> {
                    String remaining = builder.getRemaining().toLowerCase();
                    for (String o : options) {
                        if (o.toLowerCase().startsWith(remaining)) builder.suggest(o);
                    }
                    return builder.buildFuture();
                };
            }
        };
    }

    public static Argument player(String name) {
        return new Argument(name) {
            @Override
            public ArgumentType<?> brigadierType() {
                return StringArgumentType.greedyString();
            }

            @Override
            public SuggestionProvider<FabricClientCommandSource> suggestions() {
                return (ctx, builder) -> {
                    net.minecraft.client.MinecraftClient client = net.minecraft.client.MinecraftClient.getInstance();
                    if (client.getNetworkHandler() == null) return builder.buildFuture();
                    String remaining = builder.getRemaining().toLowerCase();
                    client.getNetworkHandler().getPlayerList().stream()
                            .map(p -> p.getProfile().getName())
                            .filter(n -> n != null && n.toLowerCase().startsWith(remaining))
                            .forEach(builder::suggest);
                    return builder.buildFuture();
                };
            }
        };
    }

    public final String name() {
        return name;
    }

    public abstract ArgumentType<?> brigadierType();

    public abstract SuggestionProvider<FabricClientCommandSource> suggestions();
}
