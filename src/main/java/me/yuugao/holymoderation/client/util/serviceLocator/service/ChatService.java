package me.yuugao.holymoderation.client.util.serviceLocator.service;

import static me.yuugao.holymoderation.client.util.Colors.*;


import me.yuugao.holymoderation.client.util.serviceLocator.ServiceLocator;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.Arrays;

public class ChatService extends Service {
    public final Text HMTextComponent = Text.of(YELLOW + BOLD + "[" + DARK_AQUA + BOLD + "HM" + YELLOW + BOLD + "] " + WHITE);
    public final char[] Chars = {'!', '/', '#', '$', '%', '&', '\'', '(', ')', '*', '+', '-', ',', '.', ':', ';', '<', '>', '=', '?', '@', '[', ']', '^', '`', '|', '~', '{', '}'};
    public final String[] NoArgCommands = {
            "autoban", "autocopy", "autodupeip", "autofly", "autogm3", "autogod", "autoha", "autotp", "autovanish",
            "copy", "disable", "enable", "me", "net", "sounds", "spyfrz", "stats", "textsclear", "textslist",
            "unfreezing", "unfrz", "twinks"
    };
    public final String[] PlayerCommands = {
            "freezing", "frz", "sendtexts", "spy"
    };
    public final String[] OneArgCommands = {
            "setapitoken", "setcopy", "setmarker", "setspydelay", "textadd", "textremove", "setsoundsvolume"
    };
    public final String[] TwoArgCommands = {
            "sban", "startcheckout", "textedit"
    };
    public final String[] FourArgCommands = {
            "endcheckout"
    };

    public void chatMessage(String message) {
        ClientPlayNetworkHandler networkHandler = ServiceLocator.getMinecraftService().getClient().getNetworkHandler();
        if (networkHandler != null) {
            if (message.startsWith("/")) {
                networkHandler.sendCommand(message.substring(1));
            } else {
                networkHandler.sendChatMessage(message);
            }
        }
    }

    public void clientMessage(Text text) {
        ClientPlayerEntity player = ServiceLocator.getMinecraftService().getPlayer();
        if (player != null) {
            player.sendMessage(generateComponent(HMTextComponent, text), false);
        }
    }

    public String formatReceivedText(String text) {
        text = text.replaceAll("§[0-9a-zA-Z]", "");
        for (String ignoredString : new String[]{"[ALL] ʟ", "[Тихий] ❖", "SC |", "HW >", " ▬▬▬", "▬▬▬", "[PMS]:", "◀", "[HM]", "[HAC]", "[я"}) {
            if (text.startsWith(ignoredString)) {
                return null;
            }
        }
        text = text.replace(ServiceLocator.getConfigManager().getConfig().getCopyButtonText().replaceAll("§[0-9a-zA-Z]", ""), "");
        return text;
    }

    public String formatLocation(String location) {
        return location.equals("l2anarchy") ? "lite120-1"
                : location.equals("lanarchy") ? "lite-1"
                : location.equals("anarchy") ? "classic-1"
                : location.equals("lpvp") ? "lpvp"
                : location.startsWith("l2") ? "lite120-" + location.split("anarchy")[1]
                : location.startsWith("l") ? "lite-" + location.split("anarchy")[1]
                : "classic-" + location.split("anarchy")[1];
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
                Runtime.getRuntime().exec(new String[]{"powershell", "-command", "Set-Clipboard -Value '" + text + "'"});
            } else if (osName.contains("mac")) {
                Runtime.getRuntime().exec(new String[]{"sh", "-c", "echo \"" + text + "\" | pbcopy"});
            } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
                Runtime.getRuntime().exec(new String[]{"sh", "-c", "echo \"" + text + "\" | xclip -selection clipboard"});
            } else {
                throw new Exception("Неизвестная OS.");
            }
        } catch (Exception e) {
            ServiceLocator.getNotificationService().addNotification(NotificationType.EXCEPTION, DARK_AQUA + BOLD + "Исключение", "Исключение в ChatService/copyToClipboard: " + DARK_RED + e, 5f);
        }
    }

    public MutableText suggestTextComponent(String componentText) {
        Text suggestComponent = Text.of((componentText));
        return suggestComponent.copy().setStyle(
                suggestComponent.getStyle()
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                Text.of("Нажмите, чтобы подставить команду.")))
                        .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                                componentText.replaceAll("§[0-9a-zA-Z]", ""))));
    }

    public MutableText suggestTextComponent(String componentText, String hint, String toSuggestText) {
        Text suggestComponent = Text.of(componentText);
        return suggestComponent.copy().setStyle(
                suggestComponent.getStyle().
                        withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of(hint)))
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