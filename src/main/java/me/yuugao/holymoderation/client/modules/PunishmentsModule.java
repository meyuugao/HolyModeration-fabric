package me.yuugao.holymoderation.client.modules;

import static me.yuugao.holymoderation.client.util.Colors.*;


import me.yuugao.holymoderation.client.eventbus.Subscribe;
import me.yuugao.holymoderation.client.eventbus.event.CommandSendEvent;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;
import me.yuugao.holymoderation.client.util.serviceLocator.service.NotificationType;

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
        String eventCommand = event.getCommand();
        String[] commandSplit = eventCommand.split(" ", 3);

        String command = "/" + commandSplit[0];

        if (!eventCommand.equals(StrangeMessage)) {
            StrangePunishmentConfirm = false;
        }

        if (!eventCommand.equals(StrangeFrzMessage)) {
            StrangeFrzPunishmentConfirm = false;
        }

        if (serviceContext.getChatService().isArrayContains(PunishmentsCommands, command)) {
            event.setCancelled(true);
            if (commandSplit.length > 1) {
                String nickname = commandSplit[1];
                char lastChar = nickname.charAt(nickname.length() - 1);
                for (char ch : serviceContext.getChatService().Chars) {
                    if (nickname.contains(String.valueOf(ch))) {
                        nicknameHasChar = true;
                        break;
                    }
                }
                if (nicknameHasChar) {
                    serviceContext.getNotificationService().addNotification(NotificationType.ERROR, RED + BOLD + "Ошибка", "Некорректный никнейм.", 5f);
                    return;
                }
                if (!String.valueOf(lastChar).matches("(?i)[smhd]") && serviceContext.getChatService().checkCorrectInt(nickname.substring(0, nickname.length() - 1))) {
                    if (!StrangePunishmentConfirm) {
                        StrangeMessage = eventCommand;
                        StrangePunishmentConfirm = true;
                        serviceContext.getNotificationService().addNotification(NotificationType.WARNING, GOLD + BOLD + "Предупреждение", WHITE + "Вы " + RED + BOLD + "УВЕРЕНЫ" + WHITE + ", что хотите выдать наказание игроку " + GREEN + BOLD + nickname + WHITE + "? Если вы " + RED + BOLD + "УВЕРЕНЫ" + WHITE + ", то введите команду ещё раз.", 5f);
                        return;
                    } else {
                        StrangePunishmentConfirm = false;
                        StrangeMessage = StringUtils.EMPTY;
                    }
                }
                if (nickname.equals(serviceContext.getStateService().getPlayer())) {
                    if (!StrangeFrzPunishmentConfirm) {
                        StrangeFrzMessage = eventCommand;
                        StrangeFrzPunishmentConfirm = true;
                        serviceContext.getNotificationService().addNotification(NotificationType.WARNING, GOLD + BOLD + "Предупреждение", WHITE + "Вы " + RED + BOLD + "УВЕРЕНЫ" + WHITE + ", что хотите выдать наказание игроку, который у вас на проверке? Если вы " + RED + BOLD + "УВЕРЕНЫ" + WHITE + ", то введите команду ещё раз.", 5f);
                        return;
                    } else {
                        StrangeFrzMessage = StringUtils.EMPTY;
                    }
                }
            }

            if (serviceContext.getChatService().isArrayContains(TempPunishments, command)) {
                commandSplit = eventCommand.split(" ", 4);
                switch (commandSplit.length) {
                    case (1):
                        serviceContext.getNotificationService().addNotification(NotificationType.ERROR, RED + BOLD + "Ошибка", "Вы не указали ник игрока, время и причину.", 5f);
                        return;
                    case (2):
                        serviceContext.getNotificationService().addNotification(NotificationType.ERROR, RED + BOLD + "Ошибка", "Вы не указали время и причину.", 5f);
                        return;
                    case (3):
                        serviceContext.getNotificationService().addNotification(NotificationType.ERROR, RED + BOLD + "Ошибка", "Вы не указали причину.", 5f);
                        return;
                }
                String nick = commandSplit[1];
                String time = commandSplit[2];
                String reason = commandSplit[3];
                if (serviceContext.getChatService().isArrayContains(MuteCommands, command)) {
                    if (!serviceContext.getPunishmentsService().punish(command, nick, time, reason, false)) {
                        return;
                    }
                }
                if (serviceContext.getChatService().isArrayContains(BanCommands, command)) {
                    if (!serviceContext.getPunishmentsService().punish(command, nick, time, reason, true)) {
                        return;
                    }
                }
            } else if (serviceContext.getChatService().isArrayContains(InfinityPunishments, command) || command.equals("/warn")) {
                commandSplit = eventCommand.split(" ", 3);
                switch (commandSplit.length) {
                    case (1):
                        serviceContext.getNotificationService().addNotification(NotificationType.ERROR, RED + BOLD + "Ошибка", "Вы не указали ник игрока и причину.", 5f);
                        return;
                    case (2):
                        serviceContext.getNotificationService().addNotification(NotificationType.ERROR, RED + BOLD + "Ошибка", "Вы не указали причину.", 5f);
                        return;
                }
                String nick = commandSplit[1];
                String reason = commandSplit[2];
                serviceContext.getPunishmentsService().punish(command, nick, reason, serviceContext.getChatService().isArrayContains(VkCommands, command));
            }

            if (serviceContext.getChatService().isArrayContains(BanCommands, command) || command.equals("/warn")) {
                if (StrangeFrzPunishmentConfirm && StrangeFrzMessage.isEmpty()) {
                    StrangeFrzPunishmentConfirm = false;
                    serviceContext.getCheckoutsService().endCheckOut();
                }
            }

            if (serviceContext.getChatService().isArrayContains(MuteCommands, command)) {
                if (StrangeFrzPunishmentConfirm && StrangeFrzMessage.isEmpty()) {
                    StrangeFrzPunishmentConfirm = false;
                }
            }
        }
    }
}