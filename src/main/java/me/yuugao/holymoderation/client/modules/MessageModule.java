package me.yuugao.holymoderation.client.modules;

import me.yuugao.holymoderation.client.eventbus.Subscribe;
import me.yuugao.holymoderation.client.eventbus.event.MessageReceiveEvent;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageModule extends Module {
    @Subscribe
    public void onMessageReceive(MessageReceiveEvent event) {
        Text newComponent;
        Text component;
        String componentMessage;
        String message;

        component = event.getMessage();
        newComponent = component;
        componentMessage = component.getString();
        message = componentMessage.replaceAll("§[0-9a-zA-Z]", "");
        if (componentMessage.contains("§6§6")) {
            MutableText suggestTextComponent = Text.literal("");
            String regex = "§6§6(.*?)§f";
            List<String> matches = new ArrayList<>();
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(componentMessage);
            while (matcher.find()) {
                matches.add(matcher.group());
            }

            for (int i = 0; i < matches.size(); i++) {
                if (i == matches.size() - 1) {
                    suggestTextComponent.append(Text.literal(componentMessage.split(matches.get(i))[0]));
                    suggestTextComponent.append(serviceContext.getChatService().suggestTextComponent(matches.get(i)));
                    if (componentMessage.split(matches.get(i)).length != 1) {
                        suggestTextComponent.append(Text.literal(componentMessage.split(matches.get(i))[1]));
                    }
                } else {
                    suggestTextComponent.append(Text.literal(componentMessage.split(matches.get(i))[0]));
                    suggestTextComponent.append(serviceContext.getChatService().suggestTextComponent(matches.get(i)));
                    componentMessage = componentMessage.replace(componentMessage.split(matches.get(i))[0] + matches.get(i), "");
                }
            }

            newComponent = suggestTextComponent;
            component = newComponent;
        }

        boolean renderCopyButton = true;
        if (!serviceContext.getStateService().getPlayer().isEmpty()) {
            if ((message.contains(":") && message.split(":")[0].contains(serviceContext.getStateService().getPlayer())) && !message.startsWith("Игрок") && !message.startsWith("История") && !message.startsWith("[я ->")) {
                newComponent = serviceContext.getChatService().generateComponent(Text.literal(serviceContext.getConfigManager().getConfig().getPlayerMarker() + " §f" + serviceContext.getStateService().getPlayer() + " §5-> "), serviceContext.getChatService().copyTextComponent(message.split(": ")[message.split(": ").length - 1], "Оригинальное сообщение: " + message + "\nНажмите, чтобы скопировать сообщение игрока.", message.split(": ")[1])); //message.split(": ").length - 1 нужен для того, чтобы если в титуле было ": ", оно не ломалось к чертям
                renderCopyButton = false;
            }
        }

        if (serviceContext.getConfigManager().getConfig().isCopyButtonEnabled() && !message.startsWith("[HM]")) {
            if (renderCopyButton) {
                MutableText copyComponent = Text.literal(" ");
                copyComponent.append(serviceContext.getChatService().copyTextComponent(serviceContext.getConfigManager().getConfig().getCopyButtonText(), "Нажмите, чтобы скопировать сообщение.", message));

                newComponent = serviceContext.getChatService().generateComponent(component, copyComponent);
            }
        }

        event.setMessage(newComponent);
    }
}