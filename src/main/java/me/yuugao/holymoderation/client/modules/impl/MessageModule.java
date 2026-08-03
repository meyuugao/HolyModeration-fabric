package me.yuugao.holymoderation.client.modules.impl;

import me.yuugao.holymoderation.client.di.annotations.Inject;
import me.yuugao.holymoderation.client.di.annotations.Singleton;
import me.yuugao.holymoderation.client.util.service.ChatService;

import me.yuugao.holymoderation.client.util.service.config.ConfigManagerService;
import me.yuugao.holymoderation.client.util.service.config.impl.SettingsConfig;
import me.yuugao.holymoderation.client.util.service.eventbus.Subscribe;
import me.yuugao.holymoderation.client.util.service.eventbus.event.impl.chat.MessageReceiveEvent;
import me.yuugao.holymoderation.client.util.service.state.PlayerStateService;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor_ = @Inject)
@Singleton
public class MessageModule {
    private final PlayerStateService playerStateService;
    private final ChatService chatService;
    private final ConfigManagerService configManagerService;

    @Subscribe(priority = 95)
    public void onMessageReceive(MessageReceiveEvent event) {
        String checkoutPlayer = playerStateService.getCheckoutPlayer();
        SettingsConfig settingsConfig = configManagerService.getSettingsConfig();

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