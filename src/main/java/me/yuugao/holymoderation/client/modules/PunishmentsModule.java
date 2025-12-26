package me.yuugao.holymoderation.client.modules;

import static me.yuugao.holymoderation.client.util.Colors.*;


import me.yuugao.holymoderation.client.eventbus.Subscribe;
import me.yuugao.holymoderation.client.eventbus.event.MessageSendEvent;

import org.apache.commons.lang3.StringUtils;

public class PunishmentsModule extends Module {
    private final String[] PunishmentsCommands = {"/mute", "/muteip", "/tempmute", "/tempmuteip", "/ban", "/banip", "/tempban", "/warn"};
    private final String[] TempPunishments = {"/tempmute", "/tempmuteip", "/tempban"};
    private final String[] InfinityPunishments = {"/mute", "/muteip", "/ban", "/banip"};
    private final String[] BanCommands = {"/ban", "/banip", "/tempban"};
    private final String[] MuteCommands = {"/mute", "/muteip", "/tempmute", "/tempmuteip"};
    private final String[] VkCommands = {"/mute", "/muteip", "/ban", "/banip", "/tempban"};

    private boolean nicknameHasChar = false;
    private boolean StrangePunishmentConfirm = false;
    private boolean StrangeFrzPunishmentConfirm = false;
    private String StrangeMessage = StringUtils.EMPTY;
    private String StrangeFrzMessage = StringUtils.EMPTY;

    @Subscribe
    public void onMessageSend(MessageSendEvent event) {
        String message = event.getContent();
        String[] messageSplit = message.split(" ", 3);
        String command = messageSplit[0];

        if (!message.equals(StrangeMessage)) {
            StrangePunishmentConfirm = false;
        }

        if (!message.equals(StrangeFrzMessage)) {
            StrangeFrzPunishmentConfirm = false;
        }

        if (serviceContext.getChatService().isArrayContains(PunishmentsCommands, command)) {
            event.setCancelled(true);
            if (messageSplit.length > 1) {
                String nickname = messageSplit[1];
                char lastChar = nickname.charAt(nickname.length() - 1);
                for (char ch : serviceContext.getChatService().Chars) {
                    if (nickname.contains(String.valueOf(ch))) {
                        nicknameHasChar = true;
                        break;
                    }
                }
                if (nicknameHasChar) {
                    serviceContext.getLoggerService().printError("Некорректный никнейм.");
                    return;
                }
                if (!String.valueOf(lastChar).matches("(?i)[smhd]") && serviceContext.getChatService().checkCorrectInt(nickname.substring(0, nickname.length() - 1))) {
                    if (!StrangePunishmentConfirm) {
                        StrangeMessage = message;
                        StrangePunishmentConfirm = true;
                        serviceContext.getLoggerService().printError(WHITE + BOLD + "Вы " + RED + BOLD + "УВЕРЕНЫ" + WHITE + BOLD + ", что хотите выдать наказание игроку " + GREEN + BOLD + nickname + WHITE + BOLD + "? Если вы " + RED + BOLD + "УВЕРЕНЫ" + WHITE + BOLD + ", то введите команду ещё раз.");
                        return;
                    } else {
                        StrangePunishmentConfirm = false;
                        StrangeMessage = StringUtils.EMPTY;
                    }
                }
                if (nickname.equals(serviceContext.getStateService().getPlayer())) {
                    if (!StrangeFrzPunishmentConfirm) {
                        StrangeFrzMessage = message;
                        StrangeFrzPunishmentConfirm = true;
                        serviceContext.getLoggerService().printError(WHITE + BOLD + "Вы " + RED + BOLD + "УВЕРЕНЫ" + WHITE + BOLD + ", что хотите выдать наказание игроку, который у вас на проверке? Если вы " + RED + BOLD + "УВЕРЕНЫ" + WHITE + BOLD + ", то введите команду ещё раз.");
                        return;
                    } else {
                        StrangeFrzMessage = StringUtils.EMPTY;
                    }
                }
            }

            if (serviceContext.getChatService().isArrayContains(TempPunishments, command)) {
                messageSplit = message.split(" ", 4);
                switch (messageSplit.length) {
                    case (1):
                        serviceContext.getLoggerService().printError("Вы не указали ник игрока, время и причину.");
                        return;
                    case (2):
                        serviceContext.getLoggerService().printError("Вы не указали время и причину.");
                        return;
                    case (3):
                        serviceContext.getLoggerService().printError("Вы не указали причину.");
                        return;
                }
                String nick = messageSplit[1];
                String time = messageSplit[2];
                String reason = messageSplit[3];
                if (serviceContext.getChatService().isArrayContains(MuteCommands, command)) {
                    if (!serviceContext.getPunishmentsService().punish(command, nick, time, reason, false, serviceContext)) {
                        return;
                    }
                }
                if (serviceContext.getChatService().isArrayContains(BanCommands, command)) {
                    if (!serviceContext.getPunishmentsService().punish(command, nick, time, reason, true, serviceContext)) {
                        return;
                    }
                }
            } else if (serviceContext.getChatService().isArrayContains(InfinityPunishments, command) || command.equals("/warn")) {
                messageSplit = message.split(" ", 3);
                switch (messageSplit.length) {
                    case (1):
                        serviceContext.getLoggerService().printError("Вы не указали ник игрока и причину.");
                        return;
                    case (2):
                        serviceContext.getLoggerService().printError("Вы не указали причину.");
                        return;
                }
                String nick = messageSplit[1];
                String reason = messageSplit[2];
                serviceContext.getPunishmentsService().punish(command, nick, reason, serviceContext.getChatService().isArrayContains(VkCommands, command), serviceContext);
            }

            if (serviceContext.getChatService().isArrayContains(BanCommands, command) || command.equals("/warn")) {
                if (StrangeFrzPunishmentConfirm && StrangeFrzMessage.isEmpty()) {
                    StrangeFrzPunishmentConfirm = false;
                    serviceContext.getCheckoutsService().endCheckOut(serviceContext);
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