package me.yuugao.holymoderation.client.manager;

import static me.yuugao.holymoderation.client.util.Colors.*;
import static me.yuugao.holymoderation.client.manager.ChatManager.*;
import static me.yuugao.holymoderation.client.manager.ChatManager.*;
import static me.yuugao.holymoderation.client.util.Constants.MC;


import me.yuugao.holymoderation.client.HolyModerationClient;

import java.util.Arrays;

public class ChatManager {
    public static final StringTextComponent HMTextComponent = new StringTextComponent(YELLOW + BOLD + "[" + DARK_AQUA + BOLD + "HM" + YELLOW + BOLD + "] " + WHITE);
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
        if (MC.getNetworkHandler() != null) {
            if (message.startsWith("/")) {
                MC.getNetworkHandler().sendCommand(message.substring(1));
            } else {
                MC.getNetworkHandler().sendChatMessage(message);
            }
        }
    }

    public static void clientMessage(String message) {
        MC.player.sendMessage(new StringTextComponent(YELLOW + BOLD + "[" + DARK_AQUA + BOLD + "HM" + YELLOW + BOLD + "] " + WHITE + message), Minecraft.getInstance().player.getUniqueID());
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

    public static StringTextComponent suggestTextComponent(String componentText) {
        StringTextComponent suggestComponent = new StringTextComponent(componentText);

        suggestComponent.setStyle(suggestComponent.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent("Нажмите, чтобы подставить команду."))));
        suggestComponent.setStyle(suggestComponent.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, componentText.replace("§f", "").replace("§6", "").replace("§a", "").replace("§c", "").replace("§e", "").replace("§l", "").replace("§3", ""))));

        return suggestComponent;
    }

    public static StringTextComponent suggestTextComponent(String componentText, String hint, String toSuggestText) {
        StringTextComponent suggestComponent = new StringTextComponent(componentText);

        suggestComponent.setStyle(suggestComponent.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent(hint))));
        suggestComponent.setStyle(suggestComponent.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, toSuggestText)));

        return suggestComponent;
    }

    public static StringTextComponent hoverTextComponent(String componentText, String hint) {
        StringTextComponent hoverComponent = new StringTextComponent(componentText);

        hoverComponent.setStyle(hoverComponent.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent(hint))));

        return hoverComponent;
    }

    public static StringTextComponent copyTextComponent(String componentText, String hint, String toCopyText) {
        StringTextComponent copyComponent = new StringTextComponent(componentText);

        copyComponent.setStyle(copyComponent.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent(hint))));
        copyComponent.setStyle(copyComponent.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, toCopyText)));

        return copyComponent;
    }

    public static StringTextComponent openURLTextComponent(String componentText, String hint, String url) {
        StringTextComponent openURLComponent = new StringTextComponent(componentText);

        openURLComponent.setStyle(openURLComponent.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent(hint))));
        openURLComponent.setStyle(openURLComponent.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url)));

        return openURLComponent;
    }

    public static StringTextComponent generateComponent(ITextComponent... Components) {
        StringTextComponent newComponent = new StringTextComponent("");
        Arrays.asList(Components).forEach(newComponent::append);
        return newComponent;
    }
}