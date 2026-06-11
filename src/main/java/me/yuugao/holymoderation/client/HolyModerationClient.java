package me.yuugao.holymoderation.client;

import me.yuugao.holymoderation.client.di.DIAccessor;
import me.yuugao.holymoderation.client.di.DIContainer;
import me.yuugao.holymoderation.client.di.DIRegistry;
import me.yuugao.holymoderation.client.di.module.impl.*;
import me.yuugao.holymoderation.client.util.handler.GlobalExceptionHandler;
import me.yuugao.holymoderation.client.util.service.ChatService;
import me.yuugao.holymoderation.client.util.service.HwidService;
import me.yuugao.holymoderation.client.util.service.ModuleManagerService;
import me.yuugao.holymoderation.client.util.service.UserValidationService;
import me.yuugao.holymoderation.client.util.service.eventbus.EventBusService;
import me.yuugao.obfuscator.DontObf;
import me.yuugao.obfuscator.ObfRule;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.argument.EntityArgumentType;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import lombok.Getter;
import oshi.SystemInfo;
import oshi.hardware.ComputerSystem;

public class HolyModerationClient implements ClientModInitializer {
    @Getter
    private static DIContainer di;

    @Override
    @DontObf(ObfRule.MAP_METHOD)
    public void onInitializeClient() {
        di = new DIRegistry()
                .addModule(new ContainerModule())
                .addModule(new ServicesModule())
                .addModule(new ModulesModule())
                .addModule(new DrawableElementsModule())
                .addModule(new FactoriesModule())
                .addModule(new HandlerModule())
                .build();

        di.get(GlobalExceptionHandler.class).register();

        DIAccessor.initialize(di);

        EventBusService eventBusService = di.get(EventBusService.class);
        eventBusService.initializeEventBus();

        di.get(ModuleManagerService.class).registerAll();

        commandsInitialize();

        di.get(HwidService.class).calculateHwid();
        di.get(UserValidationService.class).onMinecraftStart();
    }

    private void commandsInitialize() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            ChatService chat = di.get(ChatService.class);
            LiteralArgumentBuilder<FabricClientCommandSource> hm = ClientCommandManager.literal("hm");

            for (String cmd : chat.NoArgCommands) {
                hm.then(ClientCommandManager.literal(cmd));
            }
            for (String cmd : chat.PlayerCommands) {
                hm.then(ClientCommandManager.literal(cmd)
                        .then(ClientCommandManager.argument("player", EntityArgumentType.player())
                                .suggests(getOnlinePlayers())));
            }
            for (String cmd : chat.OneArgCommands) {
                hm.then(ClientCommandManager.literal(cmd)
                        .then(ClientCommandManager.argument("arg1", StringArgumentType.greedyString())));
            }
            for (String cmd : chat.TwoArgCommands) {
                hm.then(ClientCommandManager.literal(cmd)
                        .then(ClientCommandManager.argument("arg1", StringArgumentType.greedyString())
                                .then(ClientCommandManager.argument("arg2", StringArgumentType.greedyString()))));
            }
            for (String cmd : chat.FourArgCommands) {
                hm.then(ClientCommandManager.literal(cmd)
                        .then(ClientCommandManager.argument("arg1", StringArgumentType.greedyString())
                                .then(ClientCommandManager.argument("arg2", StringArgumentType.greedyString())
                                        .then(ClientCommandManager.argument("arg3", StringArgumentType.greedyString())
                                                .then(ClientCommandManager.argument("arg4", StringArgumentType.greedyString()))))));
            }
            dispatcher.register(hm);
        });
    }

    private SuggestionProvider<FabricClientCommandSource> getOnlinePlayers() {
        return (context, builder) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.getNetworkHandler() == null) return builder.buildFuture();
            String remaining = builder.getRemaining().toLowerCase();
            client.getNetworkHandler().getPlayerList().stream()
                    .map(p -> p.getProfile().getName())
                    .filter(name -> name.toLowerCase().startsWith(remaining))
                    .forEach(builder::suggest);
            return builder.buildFuture();
        };
    }

    private String getHardwareId() {
        try {
            SystemInfo systemInfo = new SystemInfo();
            ComputerSystem computerSystem = systemInfo.getHardware().getComputerSystem();
            String hardwareUuid = computerSystem.getHardwareUUID();
            if (hardwareUuid != null && !hardwareUuid.isEmpty() && !"unknown".equalsIgnoreCase(hardwareUuid)) {
                return hardwareUuid;
            } else {
                return "UNKNOWN_HWID";
            }
        } catch (Throwable t) {
            return t.toString();
        }
    }
}