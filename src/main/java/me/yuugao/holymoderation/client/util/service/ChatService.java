package me.yuugao.holymoderation.client.util.service;

import static me.yuugao.holymoderation.client.util.Colors.*;


import me.yuugao.holymoderation.client.di.annotations.Inject;
import me.yuugao.holymoderation.client.di.annotations.Singleton;
import me.yuugao.holymoderation.client.util.service.config.ConfigManagerService;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.regex.Pattern;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor_ = @Inject)
@Singleton
public class ChatService {
    public static final Text HM_TEXT_COMPONENT = Text.of("%s%s[%s%sHM%s%s]%s".formatted(BLUE, BOLD, DARK_AQUA, BOLD, BLUE, BOLD, WHITE));
    public static final char[] CHARS = {'!', '/', '#', '$', '%', '&', '\'', '(', ')', '*', '+', '-', ',', '.', ':', ';', '<',
            '>', '=', '?', '@', '[', ']', '^', '`', '|', '~', '{', '}'};
    private static final Pattern COLOR_CODE = Pattern.compile("§[0-9a-zA-Z]");
    private final MinecraftService minecraftService;
    private final ConfigManagerService configManagerService;
    private final NotificationsService notificationsService;

    public void chatMessage(String message) {
        ClientPlayNetworkHandler networkHandler = minecraftService.getClient().getNetworkHandler();

        if (networkHandler != null) {
            if (message.startsWith("/")) {
                networkHandler.sendCommand(message.substring(1));
            } else {
                networkHandler.sendChatMessage(message);
            }
        }
    }

    public void clientMessage(Text text) {
        ClientPlayerEntity player = minecraftService.getPlayer();

        if (player != null) {
            player.sendMessage(generateComponent(HM_TEXT_COMPONENT, text), false);
        }
    }

    public static String stripColor(String text) {
        return COLOR_CODE.matcher(text).replaceAll(StringUtils.EMPTY);
    }

    public String formatReceivedText(String text) {
        text = stripColor(text);
        for (String ignoredString : HolyWorldPatterns.IGNORED_PREFIXES) {
            if (text.startsWith(ignoredString)) return null;
        }
        text = text.replace(stripColor(configManagerService.getSettingsConfig().getCopyButtonText()), StringUtils.EMPTY);
        return text;
    }

    public String formatLocation(String location) {
        return HolyWorldPatterns.formatLocation(location);
    }

    public boolean isArrayContains(String[] array, String value) {
        return Arrays.asList(array).contains(value);
    }

    public boolean checkCorrectInt(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public boolean checkCorrectLong(String value) {
        try {
            Long.parseLong(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public void copyToClipboard(String text) {
        try {
            String osName = System.getProperty("os.name").toLowerCase();
            if (osName.contains("win")) {
                Runtime.getRuntime().exec(new String[]{"powershell", "-command", "Set-Clipboard -Value '%s'".formatted(text)});
            } else if (osName.contains("mac")) {
                Runtime.getRuntime().exec(new String[]{"sh", "-c", "echo \"%s\" | pbcopy".formatted(text)});
            } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
                try {
                    Runtime.getRuntime().exec(new String[]{"sh", "-c", "printf \"%s\" \"" + text.replace("\"", "\\\"") + "\" | xclip -selection clipboard"}).waitFor();
                } catch (Exception e) {
                    try {
                        Runtime.getRuntime().exec(new String[]{"sh", "-c", "printf \"%s\" \"" + text.replace("\"", "\\\"") + "\" | wl-copy"}).waitFor();
                    } catch (Exception e3) {
                        throw new UnsupportedOperationException("Ни одна утилита для буфера обмена не найдена (xclip для X11 или wl-copy для WayLand).");
                    }
                }
            } else {
                throw new UnsupportedOperationException("Неизвестная OS.");
            }
        } catch (Exception e) {
            notificationsService.addNotification(NotificationType.EXCEPTION, "%s%sИсключение".formatted(DARK_RED, BOLD),
                    "Исключение в ChatService/copyToClipboard: %s%s".formatted(DARK_RED, e), 5f);
            throw new RuntimeException(e);
        }
    }

    public MutableText suggestTextComponent(String componentText) {
        Text suggestComponent = Text.of(componentText);
        return suggestComponent.copy().setStyle(
                suggestComponent.getStyle()
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("Нажмите, чтобы подставить команду.")))
                        .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, stripColor(componentText))));
    }

    public MutableText suggestTextComponent(String componentText, String hint, String toSuggestText) {
        Text suggestComponent = Text.of(componentText);
        return suggestComponent.copy().setStyle(
                suggestComponent.getStyle()
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of(hint)))
                        .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, toSuggestText)));
    }

    public MutableText hoverTextComponent(String componentText, String hint) {
        Text hoverComponent = Text.of(componentText);
        return hoverComponent.copy().setStyle(
                hoverComponent.getStyle()
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of(hint))));
    }

    public MutableText copyTextComponent(String componentText, String hint, String toCopyText) {
        Text copyComponent = Text.of(componentText);
        return copyComponent.copy().setStyle(
                copyComponent.getStyle()
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of(hint)))
                        .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, toCopyText)));
    }

    public MutableText openURLTextComponent(String componentText, String hint, String url) {
        Text openURLComponent = Text.of(componentText);
        return openURLComponent.copy().setStyle(
                openURLComponent.getStyle()
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of(hint)))
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url)));
    }

    public MutableText generateComponent(Text... components) {
        MutableText newComponent = Text.empty();
        Arrays.asList(components).forEach(newComponent::append);
        return newComponent;
    }
}