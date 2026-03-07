package me.yuugao.holymoderation.client.modules.impl;

import me.yuugao.holymoderation.client.config.SettingsConfig;
import me.yuugao.holymoderation.client.config.manager.ConfigManager;
import me.yuugao.holymoderation.client.eventbus.Subscribe;
import me.yuugao.holymoderation.client.eventbus.event.MessageReceiveEvent;
import me.yuugao.holymoderation.client.modules.Module;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;
import me.yuugao.holymoderation.client.util.serviceLocator.service.ChatService;
import me.yuugao.holymoderation.client.util.serviceLocator.service.StateService;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public class MessageModule extends Module {
    public MessageModule(ServiceContext serviceContext) {
        super(serviceContext);
    }

    @Subscribe(priority = 95)
    public void onMessageReceive(MessageReceiveEvent event) {
        StateService stateService = serviceContext.getStateService();
        ChatService chatService = serviceContext.getChatService();
        ConfigManager configManager = serviceContext.getConfigManager();

        String checkoutPlayer = stateService.getCheckoutPlayer();
        SettingsConfig settingsConfig = configManager.getSettingsConfig();

        Text component = event.getMessage();
        String message = component.getString().replaceAll("§[0-9a-zA-Z]", StringUtils.EMPTY);

        if (!checkoutPlayer.isEmpty() && (message.contains(":") &&
                Arrays.asList(message.split(":")[0].split(" ")).contains(checkoutPlayer)) &&
                (message.startsWith("ʟ") || message.startsWith("ɢ"))) {
            String playerPart = message.split(": ")[message.split(": ").length - 1];
            String originalTip = "Оригинальное сообщение: %s\nНажмите, чтобы скопировать сообщение игрока.".formatted(message);
            event.setMessage(chatService.generateComponent(
                    Text.literal("%s §f%s §5-> ".formatted(settingsConfig.getPlayerMarker(), checkoutPlayer)),
                    chatService.copyTextComponent(playerPart, originalTip, message.split(": ")[1])
            ));
        } else if (settingsConfig.isCopyButtonEnabled() && !message.startsWith("[HM]")) {
            MutableText copyComponent = Text.literal(" ")
                    .append(chatService.copyTextComponent(
                            settingsConfig.getCopyButtonText(),
                            "Нажмите, чтобы скопировать сообщение.",
                            message
                    ));
            event.setMessage(chatService.generateComponent(component, copyComponent));
        }
    }
}