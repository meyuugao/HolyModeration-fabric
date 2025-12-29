package me.yuugao.holymoderation.client.modules;

import me.yuugao.holymoderation.client.eventbus.Subscribe;
import me.yuugao.holymoderation.client.eventbus.event.MessageReceiveEvent;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class MessageModule extends Module {
    @Subscribe
    public void onMessageReceive(MessageReceiveEvent event) {
        Text component = event.getMessage();
        String message = component.getString().replaceAll("§[0-9a-zA-Z]", "");

        if (!serviceContext.getStateService().getPlayer().isEmpty() && (message.contains(":") &&
                message.split(":")[0].contains(serviceContext.getStateService().getPlayer())) &&
                !message.startsWith("Игрок") && !message.startsWith("История") && !message.startsWith("[я ->")) {
            event.setMessage(serviceContext.getChatService().generateComponent(Text.literal(
                            serviceContext.getConfigManager().getConfig().getPlayerMarker()
                                    + " §f" + serviceContext.getStateService().getPlayer() + " §5-> "),
                    serviceContext.getChatService().copyTextComponent(message.split(": ")[message.split(": ").length - 1],
                            "Оригинальное сообщение: " + message + "\nНажмите, чтобы скопировать сообщение игрока.",
                            message.split(": ")[1]))); //tip: message.split(": ").length - 1 нужен для того, чтобы если в титуле было ": ", оно не ломалось к чертям
        } else if (serviceContext.getConfigManager().getConfig().isCopyButtonEnabled() && !message.startsWith("[HM]")) {
            MutableText copyComponent = Text.literal(" ").append(serviceContext.getChatService().copyTextComponent(
                    serviceContext.getConfigManager().getConfig().getCopyButtonText(), "Нажмите, чтобы скопировать сообщение.", message));
            event.setMessage(serviceContext.getChatService().generateComponent(component, copyComponent));
        }
    }
}