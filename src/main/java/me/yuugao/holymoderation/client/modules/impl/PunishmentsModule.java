package me.yuugao.holymoderation.client.modules.impl;

import static me.yuugao.holymoderation.client.util.Colors.*;


import me.yuugao.holymoderation.client.eventbus.Subscribe;
import me.yuugao.holymoderation.client.eventbus.event.CommandSendEvent;
import me.yuugao.holymoderation.client.modules.Module;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;
import me.yuugao.holymoderation.client.util.serviceLocator.service.*;

import org.apache.commons.lang3.StringUtils;

public class PunishmentsModule extends Module {
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

    private boolean nicknameHasChar = false;
    private boolean StrangePunishmentConfirm = false;
    private boolean StrangeFrzPunishmentConfirm = false;
    private String StrangeMessage = StringUtils.EMPTY;
    private String StrangeFrzMessage = StringUtils.EMPTY;

    public PunishmentsModule(ServiceContext serviceContext) {
        super(serviceContext);
    }

    @Subscribe
    public void onCommandSend(CommandSendEvent event) {
        ChatService chatService = serviceContext.getChatService();
        NotificationsService notificationsService = serviceContext.getNotificationsService();
        StateService stateService = serviceContext.getStateService();
        PunishmentsService punishmentsService = serviceContext.getPunishmentsService();
        CheckoutsService checkoutsService = serviceContext.getCheckoutsService();

        String eventCommand = event.getCommand();
        String[] commandSplit = eventCommand.split(" ", 3);

        String command = "/%s".formatted(commandSplit[0]);

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
                if (nickname.equals(stateService.getCheckoutPlayer())) {
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
                    if (!punishmentsService.punish(command, nick, time, reason, true)) {
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
                punishmentsService.punish(command, nick, reason, chatService.isArrayContains(VkCommands, command));
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
        }
    }
}