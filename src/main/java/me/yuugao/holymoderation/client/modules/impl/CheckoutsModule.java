package me.yuugao.holymoderation.client.modules.impl;

import static me.yuugao.holymoderation.client.util.Colors.*;


import me.yuugao.holymoderation.client.di.annotations.Inject;
import me.yuugao.holymoderation.client.di.annotations.Singleton;
import me.yuugao.holymoderation.client.gui.drawable.element.impl.impl.singleton.CheckoutsDrawableElement;
import me.yuugao.holymoderation.client.modules.DrawableModule;
import me.yuugao.holymoderation.client.util.command.*;
import me.yuugao.holymoderation.client.util.service.*;
import me.yuugao.holymoderation.client.util.service.config.ConfigManagerService;
import me.yuugao.holymoderation.client.util.service.config.impl.SettingsConfig;
import me.yuugao.holymoderation.client.util.service.eventbus.Subscribe;
import me.yuugao.holymoderation.client.util.service.eventbus.event.impl.chat.CommandSendEvent;
import me.yuugao.holymoderation.client.util.service.eventbus.event.impl.chat.MessageReceiveEvent;
import me.yuugao.holymoderation.client.util.service.state.PlayerStateService;
import me.yuugao.holymoderation.client.util.service.state.UserStateService;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Singleton
public class CheckoutsModule extends DrawableModule<CheckoutsDrawableElement> implements CommandProvider {
    private final String[] ServerFreezeCommands = {"/freezing", "/frz"};
    private final ConfigManagerService configManagerService;
    private final PlayerStateService playerStateService;
    private final UserStateService userStateService;
    private final NotificationsService notificationsService;
    private final ChatService chatService;
    private final CheckoutsService checkoutsService;
    private final PunishmentsService punishmentsService;
    private final NetService netService;
    private boolean startingCheckout = false;
    private boolean banChecking = false;
    private boolean destroyStash;
    private boolean messageIsCheckbanInfo;
    private String banReason;

    @Inject
    public CheckoutsModule(ConfigManagerService configManagerService,
                           CheckoutsDrawableElement drawableElement,
                           PlayerStateService playerStateService,
                           UserStateService userStateService,
                           NotificationsService notificationsService,
                           ChatService chatService,
                           CheckoutsService checkoutsService,
                           PunishmentsService punishmentsService,
                           NetService netService) {
        super(drawableElement);
        this.configManagerService = configManagerService;
        this.playerStateService = playerStateService;
        this.userStateService = userStateService;
        this.notificationsService = notificationsService;
        this.chatService = chatService;
        this.checkoutsService = checkoutsService;
        this.punishmentsService = punishmentsService;
        this.netService = netService;
    }

    @Override
    public void registerCommands(CommandRegistry registry) {
        registry.register(CommandSpec.of("freezing", Argument.player("игрок")).group("Проверки").description("заморозить игрока и начать проверку").handler(this::cmdFreezing));
        registry.register(CommandSpec.of("frz", Argument.player("игрок")).group("Проверки").description("алиас freezing").handler(this::cmdFreezing));
        registry.register(CommandSpec.of("unfreezing").group("Проверки").description("разморозить игрока и завершить проверку").handler(this::cmdUnfreezing));
        registry.register(CommandSpec.of("unfrz").group("Проверки").description("алиас unfreezing").handler(this::cmdUnfreezing));
        registry.register(CommandSpec.of("sban", Argument.text("время"), Argument.text("причина")).group("Проверки").description("забанить проверяемого и завершить проверку").handler(this::cmdSban));
        registry.register(CommandSpec.of("sendtexts", Argument.player("игрок")).group("Проверки").description("отправить игроку заготовленные тексты").handler(this::cmdSendTexts));
        registry.register(CommandSpec.of("startcheckout", Argument.player("игрок"), Argument.choice("причина", List.of("report", "checkout", "autobuy", "autosell", "customka", "personal", "toManyChecks", "candidate"))).group("Проверки").description("внести проверку в журнал").handler(this::cmdStartCheckout));
        registry.register(CommandSpec.of("endcheckout", Argument.choice("результат", List.of("clean", "ban", "autobuy", "autosell")), Argument.player("игрок"), Argument.choice("снести_стеш", List.of("true", "false")), Argument.text("причина_бана")).group("Проверки").description("завершить проверку в журнале").handler(this::cmdEndCheckout));
    }

    @Subscribe
    public void onCommandSend(CommandSendEvent event) {
        String checkoutPlayer = playerStateService.getCheckoutPlayer();
        String eventCommand = event.getCommand();
        String[] commandSplit = eventCommand.split(" ");
        if (eventCommand.startsWith("hm")) return;
        String command = "/%s".formatted(commandSplit[0]);

        if (chatService.isArrayContains(ServerFreezeCommands, command)) {
            event.setCancelled(true);
            if (commandSplit.length == 1) {
                notificationsService.error("Вы не указали ник игрока.");
                return;
            }
            String player = commandSplit[1];
            if (player.equals(checkoutPlayer)) {
                notificationsService.warning("Этот игрок находиться у вас на проверке. Для его разморозки используйте %s%s%s/unfreezing%s или %s%s%s/unfrz%s".formatted(GOLD, GOLD, BOLD, WHITE, GOLD, GOLD, BOLD, WHITE));
                return;
            }
            chatService.chatMessage("/freezing %s".formatted(player));
        }
    }

    private void cmdFreezing(CommandContext ctx) {
        if (userStateService.isInHub()) {
            notificationsService.warning("В хабе этого делать нельзя.");
            return;
        }
        if (!ctx.hasArg(0)) {
            notificationsService.error("Вы не указали ник игрока.");
            return;
        }
        String nickname = ctx.arg(0);
        startingCheckout = true;
        if (checkoutsService.startCheckOut(nickname)) {
            this.drawableElement.coStartForLocal(nickname);
        }
    }

    private void cmdUnfreezing(CommandContext ctx) {
        String checkoutPlayer = playerStateService.getCheckoutPlayer();
        if (checkoutPlayer.isEmpty()) {
            notificationsService.warning("Вы никого не проверяете.");
            return;
        }
        checkoutsService.endCheckOut(false, true);
    }

    private void cmdSban(CommandContext ctx) {
        String checkoutPlayer = playerStateService.getCheckoutPlayer();
        if (checkoutPlayer.isEmpty()) {
            notificationsService.warning("Вы никого не проверяете.");
            return;
        }
        if (!ctx.hasArg(0)) {
            notificationsService.error("Вы не указали время и причину бана.");
            return;
        }
        String time = ctx.arg(0);
        String reason = ctx.hasArg(1) ? "2.4 (%s)".formatted(ctx.arg(1)) : "2.4";
        if (!punishmentsService.punish("/banip", checkoutPlayer, time, reason, true)) return;
        checkoutsService.endCheckOut(false, false);
    }

    private void cmdSendTexts(CommandContext ctx) {
        if (userStateService.isInHub()) {
            notificationsService.warning("В хабе этого делать нельзя.");
            return;
        }
        if (!ctx.hasArg(0)) {
            notificationsService.error("Вы не указали ник игрока.");
            return;
        }
        checkoutsService.sendTexts(ctx.arg(0));
    }

    private void cmdStartCheckout(CommandContext ctx) {
        if (!ctx.hasArg(0)) {
            notificationsService.error("Вы не указали ник игрока и причину проверки.");
            return;
        }
        if (!ctx.hasArg(1)) {
            notificationsService.error("Вы не указали причину проверки.");
            return;
        }
        String player = ctx.arg(0);
        String reason = ctx.arg(1);
        String loc = userStateService.getUserLocation();
        String mode = loc.startsWith("lite120") ? "lite120" : loc.startsWith("lite") ? "lite" : loc.startsWith("classic") ? "classic" : "lpvp";
        if (mode.equals("lpvp")) {
            netService.startCheckout(player, reason, "lite", 1, true);
        } else {
            netService.startCheckout(player, reason, mode, Integer.parseInt(loc.split("%s-".formatted(mode))[1]), false);
        }
    }

    private void cmdEndCheckout(CommandContext ctx) {
        if (!ctx.hasArg(0)) {
            notificationsService.error("Вы не указали результат проверки.");
            return;
        }
        String result = ctx.arg(0);
        switch (result) {
            case "clean" -> netService.endCheckout(result, result, false);
            case "ban" -> {
                if (!ctx.hasArg(1)) {
                    notificationsService.error("Вы не указали ник игрока и необходимость снести стеш.");
                } else if (!ctx.hasArg(2)) {
                    notificationsService.error("Вы не указали необходимость снести стеш.");
                } else {
                    destroyStash = ctx.arg(2).equals("true");
                    if (!ctx.hasArg(3)) {
                        banChecking = true;
                        chatService.chatMessage("/checkban %s".formatted(ctx.arg(1)));
                    } else {
                        netService.endCheckout(result, ctx.arg(3), destroyStash);
                    }
                }
            }
            case "autobuy", "autosell" -> {
                if (!ctx.hasArg(2)) {
                    notificationsService.error("Вы не указали необходимость снести стеш.");
                } else {
                    destroyStash = ctx.arg(2).equals("true");
                    netService.endCheckout(result, result, destroyStash);
                }
            }
        }
    }

    @Subscribe(priority = 99)
    public void onMessageReceive(MessageReceiveEvent event) {
        String checkoutPlayer = playerStateService.getCheckoutPlayer();
        SettingsConfig settingsConfig = configManagerService.getSettingsConfig();
        String receivedText = chatService.formatReceivedText(event.getMessage().getString());
        if (receivedText == null) return;

        if (startingCheckout) {
            if (receivedText.equals(HolyWorldPatterns.FREEZE_OK)) startingCheckout = false;
            else if (receivedText.equals(HolyWorldPatterns.FREEZE_NOT_FOUND)) {
                startingCheckout = false;
                checkoutsService.endCheckOut(true, false);
            }
        }

        if (settingsConfig.isAutoAnyDeskEnabled() && !checkoutPlayer.isEmpty() && receivedText.contains(checkoutPlayer)) {
            String chatText;
            String msgText;
            if (receivedText.startsWith("[%s ->".formatted(checkoutPlayer)) && chatService.checkCorrectLong(msgText = receivedText.split("я]", 0)[1].replace(" ", StringUtils.EMPTY)) && msgText.length() >= 9 && msgText.length() <= 11) {
                chatService.copyToClipboard(msgText);
                notificationsService.success("Скопирован анидеск из лс: %s".formatted(msgText));
            } else if (chatService.checkCorrectLong(chatText = receivedText.split(":")[1].replace(" ", StringUtils.EMPTY)) && chatText.length() >= 9 && chatText.length() <= 11) {
                chatService.copyToClipboard(chatText);
                notificationsService.success("Скопирован анидеск из чата: %s".formatted(chatText));
            }
        }

        if (banChecking) {
            if (HolyWorldPatterns.isCheckbanAbort(receivedText)) {
                event.setCancelled(true);
                notificationsService.error("Проверка не была закончена, т.к. не удалось определить причину бана игрока. Пожалуйста, допишите причину вручную.");
                banChecking = false;
            }
            if (receivedText.startsWith(HolyWorldPatterns.CHECKBAN_INFO_PREFIX)) messageIsCheckbanInfo = true;
            String parsedReason = HolyWorldPatterns.extractBanReason(receivedText);
            if (parsedReason != null) banReason = parsedReason;
            if (messageIsCheckbanInfo) event.setCancelled(true);
            if (receivedText.startsWith(HolyWorldPatterns.CHECKBAN_IPBAN_PREFIX)) {
                messageIsCheckbanInfo = false;
                banChecking = false;
                netService.endCheckout("ban", banReason, destroyStash);
            }
        }
    }

    @Override
    public int getRenderPriority() {
        return 1002;
    }
}