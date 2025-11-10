package me.yuugao.holymoderation.client.manager;

import static me.yuugao.holymoderation.client.manager.SoundManager.playSound;
import static me.yuugao.holymoderation.client.util.ColorsService.*;


import me.yuugao.holymoderation.client.HolyModerationClient;
import me.yuugao.holymoderation.client.util.MinecraftService;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.Arrays;

public class ChatManager {
    public static final Text HMTextComponent = Text.of(YELLOW + BOLD + "[" + DARK_AQUA + BOLD + "HM" + YELLOW + BOLD + "] " + WHITE);
    public static final char[] Chars = {'!', '/', '#', '$', '%', '&', '\'', '(', ')', '*', '+', '-', ',', '.', ':', ';', '<', '>', '=', '?', '@', '[', ']', '^', '`', '|', '~', '{', '}'};

    public static void printException(String message) {
        clientMessage(RED + BOLD + message);
        playSound("exception.wav", 70);
    }

    public static void printError(String message) {
        clientMessage(RED + BOLD + message);
        playSound("error.wav", 70);
    }

    public static void printSuccess(String message) {
        clientMessage(AQUA + BOLD + message);
        playSound("success.wav", 70);
    }

    public static void chatMessage(String message) {
        ClientPlayNetworkHandler networkHandler = MinecraftClient.getInstance().getNetworkHandler();
        if (networkHandler != null) {
            if (message.startsWith("/")) {
                networkHandler.sendCommand(message.substring(1));
            } else {
                networkHandler.sendChatMessage(message);
            }
        }
    }

    public static void clientMessage(String message) {
        if (MinecraftService.getPlayer() != null) {
            MinecraftService.getPlayer().sendMessage(Text.of(YELLOW + BOLD + "[" + DARK_AQUA + BOLD + "HM" + YELLOW + BOLD + "] " + WHITE + message), false);
        }
    }

    public static String formatReceivedText(String text) {
        text = text.replaceAll("§[0-9a-zA-Z]", "");
        for (String ignoredString : new String[]{"[ALL] ʟ", "[Тихий] ❖", "SC |", "HW >", " ▬▬▬", "▬▬▬", "[PMS]:", "◀", "[HM]", "[HAC]", "[я"}) {
            if (text.startsWith(ignoredString)) {
                return null;
            }
        }
        text = text.replace(HolyModerationClient.CONFIG.copyButtonText.replaceAll("§[0-9a-zA-Z]", ""), "");
        return text;
    }

    public static String formatLocation(String location) {
        return location.equals("l2anarchy") ? "lite120-1"
                : location.equals("lanarchy") ? "lite-1"
                : location.equals("anarchy") ? "classic-1"
                : location.equals("lpvp") ? "lpvp"
                : location.startsWith("l2") ? "lite120-" + location.split("anarchy")[1]
                : location.startsWith("l") ? "lite-" + location.split("anarchy")[1]
                : "classic-" + location.split("anarchy")[1];
    }

    public static boolean isArrayContains(String[] array, String value) {
        return Arrays.asList(array).contains(value);
    }

    public static boolean checkCorrectInt(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean checkCorrectLong(String value) {
        try {
            Long.parseLong(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static void copyToClipboard(String text) {
        try {
            String osName = System.getProperty("os.name").toLowerCase();

            if (osName.contains("win")) {
                Runtime.getRuntime().exec(new String[]{"powershell", "-command", "Set-Clipboard -Value '" + text + "'"});
            } else if (osName.contains("mac")) {
                Runtime.getRuntime().exec(new String[]{"sh", "-c", "echo \"" + text + "\" | pbcopy"});
            } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
                Runtime.getRuntime().exec(new String[]{"sh", "-c", "echo \"" + text + "\" | xclip -selection clipboard"});
            } else {
                throw new Exception("Неизвестная OS.");
            }
        } catch (Exception e) {
            printException("Исключение в ChatManager/copyToClipboard: " + e);
        }
    }

    public static MutableText suggestTextComponent(String componentText) {
        Text suggestComponent = Text.of((componentText));
        return suggestComponent.copy().setStyle(
                suggestComponent.getStyle()
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                Text.of("Нажмите, чтобы подставить команду.")))
                        .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                                componentText.replaceAll("§[0-9a-zA-Z]", ""))));
    }

    public static MutableText suggestTextComponent(String componentText, String hint, String toSuggestText) {
        Text suggestComponent = Text.of(componentText);
        return suggestComponent.copy().setStyle(
                suggestComponent.getStyle().
                        withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of(hint)))
                        .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, toSuggestText)));
    }

    public static MutableText hoverTextComponent(String componentText, String hint) {
        Text hoverComponent = Text.of(componentText);
        return hoverComponent.copy().setStyle(
                hoverComponent.getStyle()
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of(hint))));
    }

    public static MutableText copyTextComponent(String componentText, String hint, String toCopyText) {
        Text copyComponent = Text.of(componentText);
        return copyComponent.copy().setStyle(
                copyComponent.getStyle()
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of(hint)))
                        .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, toCopyText)));
    }

    public static MutableText openURLTextComponent(String componentText, String hint, String url) {
        Text openURLComponent = Text.of(componentText);
        return openURLComponent.copy().setStyle(
                openURLComponent.getStyle()
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of(hint)))
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url)));
    }

    public static MutableText generateComponent(Text... components) {
        MutableText newComponent = Text.empty();
        Arrays.asList(components).forEach(newComponent::append);
        return newComponent;
    }
}