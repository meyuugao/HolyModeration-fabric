package me.yuugao.holymoderation.client.modules.impl;

import static me.yuugao.holymoderation.client.util.Colors.*;


import me.yuugao.holymoderation.client.di.annotations.Inject;
import me.yuugao.holymoderation.client.di.annotations.Singleton;
import me.yuugao.holymoderation.client.util.service.*;
import me.yuugao.holymoderation.client.util.service.config.ConfigManagerService;
import me.yuugao.holymoderation.client.util.service.config.impl.ApiConfig;
import me.yuugao.holymoderation.client.util.service.eventbus.Subscribe;
import me.yuugao.holymoderation.client.util.service.eventbus.event.impl.chat.CommandSendEvent;
import me.yuugao.holymoderation.client.util.service.state.PlayerStateService;

import org.apache.commons.lang3.StringUtils;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor_ = @Inject)
@Singleton
public class PunishmentsModule {
    private final String[] PunishmentsCommands = {
            "/mute", "/muteip", "/tempmute", "/tempmuteip", "/ban", "/banip", "/tempban", "/warn"
    };
    private final String[] TempPunishments = {
            "/tempmute", "/tempmuteip", "/tempban"
    };
    private final String[] InfinityPunishments = {
            "/mute", "/muteip", "/ban", "/banip"
    };
    private final String[] BanCommands = {
            "/ban", "/banip", "/tempban"
    };
    private final String[] MuteCommands = {
            "/mute", "/muteip", "/tempmute", "/tempmuteip"
    };
    private final String[] VkCommands = {
            "/mute", "/muteip", "/ban", "/banip", "/tempban"
    };
    private final ChatService chatService;
    private final NotificationsService notificationsService;
    private final PlayerStateService playerStateService;
    private final PunishmentsService punishmentsService;
    private final CheckoutsService checkoutsService;
    private final ConfigManagerService configManagerService;
    private boolean nicknameHasChar = false;
    private boolean StrangePunishmentConfirm = false;
    private boolean StrangeFrzPunishmentConfirm = false;
    private String StrangeMessage = StringUtils.EMPTY;
    private String StrangeFrzMessage = StringUtils.EMPTY;

    @Subscribe
    public void onCommandSend(CommandSendEvent event) {
        ApiConfig apiConfig = configManagerService.getApiConfig();

        String eventCommand = event.getCommand();
        String[] commandSplit = eventCommand.split(" ", 3);

        String command = commandSplit[0].equals("hm") ? commandSplit[1] : "/%s".formatted(commandSplit[0]);

        if (!eventCommand.equals(StrangeMessage)) {
            StrangePunishmentConfirm = false;
        }

        if (!eventCommand.equals(StrangeFrzMessage)) {
            StrangeFrzPunishmentConfirm = false;
        }

        if (chatService.isArrayContains(PunishmentsCommands, command)) {
            event.setCancelled(true);
            if (commandSplit.length > 1) {
                String nickname = commandSplit[1];
                char lastChar = nickname.charAt(nickname.length() - 1);
                for (char ch : chatService.Chars) {
                    if (nickname.contains(String.valueOf(ch))) {
                        nicknameHasChar = true;
                        break;
                    } else {
                        nicknameHasChar = false;
                    }
                }
                if (nicknameHasChar) {
                    notificationsService.addNotification(NotificationType.ERROR, "%s%sОшибка".formatted(RED, BOLD),
                            "Некорректный никнейм.", 5f);
                    return;
                }
                if (!String.valueOf(lastChar).matches("(?i)[smhd]") && chatService.checkCorrectInt(nickname.substring(0, nickname.length() - 1))) {
                    if (!StrangePunishmentConfirm) {
                        StrangeMessage = eventCommand;
                        StrangePunishmentConfirm = true;
                        notificationsService.addNotification(NotificationType.WARNING, "%s%sПредупреждение".formatted(GOLD, BOLD),
                                "%sВы %s%sУВЕРЕНЫ%s, что хотите выдать наказание игроку %s%s%s%s? Если вы %s%sУВЕРЕНЫ%s, то введите команду ещё раз."
                                        .formatted(WHITE, RED, BOLD, WHITE, GREEN, BOLD, nickname, WHITE, RED, BOLD, WHITE), 5f);
                        return;
                    } else {
                        StrangePunishmentConfirm = false;
                        StrangeMessage = StringUtils.EMPTY;
                    }
                }
                if (nickname.equals(playerStateService.getCheckoutPlayer())) {
                    if (!StrangeFrzPunishmentConfirm) {
                        StrangeFrzMessage = eventCommand;
                        StrangeFrzPunishmentConfirm = true;
                        notificationsService.addNotification(NotificationType.WARNING, "%s%sПредупреждение".formatted(GOLD, BOLD),
                                "%sВы %s%sУВЕРЕНЫ%s, что хотите выдать наказание игроку, который у вас на проверке? Если вы %s%sУВЕРЕНЫ%s, то введите команду ещё раз."
                                        .formatted(WHITE, RED, BOLD, WHITE, RED, BOLD, WHITE), 5f);
                        return;
                    } else {
                        StrangeFrzMessage = StringUtils.EMPTY;
                    }
                }
            }

            if (chatService.isArrayContains(TempPunishments, command)) {
                commandSplit = eventCommand.split(" ", 4);
                switch (commandSplit.length) {
                    case 1 -> {
                        notificationsService.addNotification(NotificationType.ERROR, "%s%sОшибка".formatted(RED, BOLD),
                                "Вы не указали ник игрока, время и причину.", 5f);
                        return;
                    }
                    case 2 -> {
                        notificationsService.addNotification(NotificationType.ERROR, "%s%sОшибка".formatted(RED, BOLD),
                                "Вы не указали время и причину.", 5f);
                        return;
                    }

                    case 3 -> {
                        notificationsService.addNotification(NotificationType.ERROR, "%s%sОшибка".formatted(RED, BOLD),
                                "Вы не указали причину.", 5f);
                        return;
                    }
                }
                String nick = commandSplit[1];
                String time = commandSplit[2];
                String reason = commandSplit[3];
                if (chatService.isArrayContains(MuteCommands, command)) {
                    if (!punishmentsService.punish(command, nick, time, reason, false)) {
                        return;
                    }
                }
                if (chatService.isArrayContains(BanCommands, command)) {
                    if (!punishmentsService.punish(command, nick, time, reason, reason.toLowerCase().contains("вопросы?"))) {
                        return;
                    }
                }
            } else if (chatService.isArrayContains(InfinityPunishments, command) || command.equals("/warn")) {
                commandSplit = eventCommand.split(" ", 3);
                switch (commandSplit.length) {
                    case 1 -> {
                        notificationsService.addNotification(NotificationType.ERROR, "%s%sОшибка".formatted(RED, BOLD),
                                "Вы не указали ник игрока и причину.", 5f);
                        return;
                    }

                    case 2 -> {
                        notificationsService.addNotification(NotificationType.ERROR, "%s%sОшибка".formatted(RED, BOLD),
                                "Вы не указали причину.", 5f);
                        return;
                    }
                }
                String nick = commandSplit[1];
                String reason = commandSplit[2];

                boolean addVk = chatService.isArrayContains(VkCommands, command)
                        && !((command.equals("/mute") || command.equals("/muteip")) && punishmentsService.isTimeCorrect(reason.split(" ")[0]))
                        && reason.toLowerCase().contains("вопросы?");

                punishmentsService.punish(command, nick, reason, addVk);
            }

            if (chatService.isArrayContains(BanCommands, command) || command.equals("/warn")) {
                if (StrangeFrzPunishmentConfirm && StrangeFrzMessage.isEmpty()) {
                    StrangeFrzPunishmentConfirm = false;
                    checkoutsService.endCheckOut(false);
                }
            }

            if (chatService.isArrayContains(MuteCommands, command)) {
                if (StrangeFrzPunishmentConfirm && StrangeFrzMessage.isEmpty()) {
                    StrangeFrzPunishmentConfirm = false;
                }
            }
        } else if (command.equals("setvk")) {
            commandSplit = eventCommand.split(" ", 3);
            if (commandSplit.length == 2) {
                notificationsService.addNotification(NotificationType.ERROR, "%s%sОшибка".formatted(RED, BOLD),
                        "Вы не указали ссылку на вк.", 5f);
                return;
            }
            if (!commandSplit[2].matches("^(https://)?vk\\.(com|ru)/id\\d+$")) {
                notificationsService.addNotification(
                        NotificationType.ERROR,
                        "%s%sОшибка".formatted(RED, BOLD),
                        "Некорректная ссылка на VK. Используйте формат: vk.com/id123, vk.ru/id123, https://vk.com/id123 или https://vk.ru/id123",
                        5f
                );
                return;
            }

            apiConfig.setVk(commandSplit[2]);
            configManagerService.saveConfig(apiConfig);

            notificationsService.addNotification(
                    NotificationType.SUCCESS,
                    "%s%sУспех".formatted(GREEN, BOLD),
                    "Вы успешно установили новую ссылку на вк: %s".formatted(commandSplit[2]),
                    5f
            );
        }
    }
}