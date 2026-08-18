package me.yuugao.holymoderation.client.modules.impl;

import static me.yuugao.holymoderation.client.util.Colors.*;


import me.yuugao.holymoderation.client.di.annotations.Inject;
import me.yuugao.holymoderation.client.di.annotations.Singleton;
import me.yuugao.holymoderation.client.util.command.*;
import me.yuugao.holymoderation.client.util.service.ChatService;
import me.yuugao.holymoderation.client.util.service.NotificationType;
import me.yuugao.holymoderation.client.util.service.NotificationsService;
import me.yuugao.holymoderation.client.util.service.config.ConfigManagerService;
import me.yuugao.holymoderation.client.util.service.config.impl.SettingsConfig;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor_ = @Inject)
@Singleton
public class SettingsModule implements CommandProvider {
    private static final int MAX_TEXT_LENGTH = 200;
    private static final float HUD_SCALE_MIN = 0.5f;
    private static final float HUD_SCALE_MAX = 2.0f;
    private final ChatService chatService;
    private final ConfigManagerService configManagerService;
    private final NotificationsService notificationsService;

    @Override
    public void registerCommands(CommandRegistry registry) {
        registry.register(CommandSpec.of("autocopy").group("Настройки").description("автокопирование AnyDesk из чата").handler(this::cmdAutocopy));
        registry.register(CommandSpec.of("autodupeip").group("Настройки").description("автоматический /dupeip при проверке").handler(this::cmdAutodupeip));
        registry.register(CommandSpec.of("autofly").group("Настройки").description("автоматический /fly при заходе").handler(this::cmdAutofly));
        registry.register(CommandSpec.of("autogm3").group("Настройки").description("автоматический /gm 3 при заходе").handler(this::cmdAutogm3));
        registry.register(CommandSpec.of("autogod").group("Настройки").description("автоматический /god при заходе").handler(this::cmdAutogod));
        registry.register(CommandSpec.of("autoha").group("Настройки").description("автоматические /hac alerts при заходе").handler(this::cmdAutoha));
        registry.register(CommandSpec.of("autotp").group("Настройки").description("телепорт на /warp logo при проверке").handler(this::cmdAutotp));
        registry.register(CommandSpec.of("autovanish").group("Настройки").description("автоматический /v при заходе").handler(this::cmdAutovanish));
        registry.register(CommandSpec.of("copy").group("Настройки").description("кнопка копирования сообщения").handler(this::cmdCopy));
        registry.register(CommandSpec.of("sounds").group("Настройки").description("звуки уведомлений мода").handler(this::cmdSounds));
        registry.register(CommandSpec.of("textsclear").group("Настройки").description("очистить список текстов").handler(this::cmdTextsclear));
        registry.register(CommandSpec.of("textslist").group("Настройки").description("показать список текстов").handler(this::cmdTextslist));

        registry.register(CommandSpec.of("setcopy", Argument.text("текст")).group("Настройки").description("текст кнопки копирования").handler(this::cmdSetcopy));
        registry.register(CommandSpec.of("setmarker", Argument.text("текст")).group("Настройки").description("метка игрока на проверке").handler(this::cmdSetmarker));
        registry.register(CommandSpec.of("setspydelay", Argument.integer("секунды", 0, 60, List.of())).group("Настройки").description("задержка обновления слежки").handler(this::cmdSetspydelay));
        registry.register(CommandSpec.of("hudscale", Argument.text("размер")).group("Настройки").description("размер худа (0.5 - 2.0)").handler(this::cmdHudscale));
        registry.register(CommandSpec.of("setsoundsvolume", Argument.integer("проценты", 0, 100, List.of())).group("Настройки").description("громкость звуков").handler(this::cmdSetsoundsvolume));
        registry.register(CommandSpec.of("textadd", Argument.text("текст")).group("Настройки").description("добавить текст в список").handler(this::cmdTextadd));
        registry.register(CommandSpec.of("textremove", Argument.integer("номер", 1, Integer.MAX_VALUE, List.of())).group("Настройки").description("удалить текст по номеру").handler(this::cmdTextremove));

        registry.register(CommandSpec.of("textedit", Argument.integer("номер", 1, Integer.MAX_VALUE, List.of()), Argument.text("новый_текст")).group("Настройки").description("изменить текст по номеру").handler(this::cmdTextedit));
    }

    private SettingsConfig cfg() {
        return configManagerService.getSettingsConfig();
    }

    private void cmdAutocopy(CommandContext ctx) {
        SettingsConfig c = cfg();
        c.setAutoAnyDeskEnabled(!c.isAutoAnyDeskEnabled());
        notificationsService.success("Автоматическое копирование айди AnyDesk %s.".formatted(c.isAutoAnyDeskEnabled() ? "включено" : "выключено"));
        configManagerService.saveConfig(c);
    }

    private void cmdAutodupeip(CommandContext ctx) {
        SettingsConfig c = cfg();
        c.setDupeIpEnabled(!c.isDupeIpEnabled());
        notificationsService.success("Автоматический /dupeip %s.".formatted(c.isDupeIpEnabled() ? "включён" : "выключен"));
        configManagerService.saveConfig(c);
    }

    private void cmdAutofly(CommandContext ctx) {
        SettingsConfig c = cfg();
        c.setAutoFlyEnabled(!c.isAutoFlyEnabled());
        notificationsService.success("Автоматический флай %s.".formatted(c.isAutoFlyEnabled() ? "включён" : "выключен"));
        configManagerService.saveConfig(c);
    }

    private void cmdAutogm3(CommandContext ctx) {
        SettingsConfig c = cfg();
        c.setAutoGm3Enabled(!c.isAutoGm3Enabled());
        notificationsService.success("Автоматический гм3 %s.".formatted(c.isAutoGm3Enabled() ? "включён" : "выключен"));
        configManagerService.saveConfig(c);
    }

    private void cmdAutogod(CommandContext ctx) {
        SettingsConfig c = cfg();
        c.setAutoGodEnabled(!c.isAutoGodEnabled());
        notificationsService.success("Автоматический god %s.".formatted(c.isAutoGodEnabled() ? "включён" : "выключен"));
        configManagerService.saveConfig(c);
    }

    private void cmdAutoha(CommandContext ctx) {
        SettingsConfig c = cfg();
        c.setAutoHacAlertsEnabled(!c.isAutoHacAlertsEnabled());
        notificationsService.success("Автоматический hac alerts %s.".formatted(c.isAutoHacAlertsEnabled() ? "включён" : "выключен"));
        configManagerService.saveConfig(c);
    }

    private void cmdAutotp(CommandContext ctx) {
        SettingsConfig c = cfg();
        c.setAutoCheckoutTpEnabled(!c.isAutoCheckoutTpEnabled());
        notificationsService.success("Автоматический телепорт на /warp logo %s.".formatted(c.isAutoCheckoutTpEnabled() ? "включён" : "выключен"));
        configManagerService.saveConfig(c);
    }

    private void cmdAutovanish(CommandContext ctx) {
        SettingsConfig c = cfg();
        c.setAutoVanishEnabled(!c.isAutoVanishEnabled());
        notificationsService.success("Автоматический ваниш %s.".formatted(c.isAutoVanishEnabled() ? "включён" : "выключен"));
        configManagerService.saveConfig(c);
    }

    private void cmdCopy(CommandContext ctx) {
        SettingsConfig c = cfg();
        c.setCopyButtonEnabled(!c.isCopyButtonEnabled());
        notificationsService.success("Кнопка копирования %s.".formatted(c.isCopyButtonEnabled() ? "включена" : "выключена"));
        configManagerService.saveConfig(c);
    }

    private void cmdSounds(CommandContext ctx) {
        SettingsConfig c = cfg();
        c.setSoundsEnabled(!c.isSoundsEnabled());
        notificationsService.success("Звуки мода %s.".formatted(c.isSoundsEnabled() ? "включены" : "выключены"));
        configManagerService.saveConfig(c);
    }

    private void cmdTextsclear(CommandContext ctx) {
        SettingsConfig c = cfg();
        c.getTextsList().clear();
        notificationsService.success("Вы успешно очистили все тексты.");
        configManagerService.saveConfig(c);
    }

    private void cmdTextslist(CommandContext ctx) {
        SettingsConfig c = cfg();
        List<String> textsList = c.getTextsList();
        if (textsList.isEmpty()) {
            notificationsService.error("У вас нет настроенных текстов.");
            return;
        }
        StringBuilder texts = new StringBuilder(StringUtils.EMPTY);
        for (int i = 0; i < textsList.size(); i++) {
            texts.append("%s%s".formatted(AQUA, BOLD)).append(i + 1).append(WHITE).append(". ").append(textsList.get(i));
            if (i < textsList.size() - 1) texts.append("\n");
        }
        notificationsService.addNotification(NotificationType.SUCCESS,
                "%s%sСписок ваших текстов".formatted(GREEN, BOLD), texts.toString(), 10f);
    }

    private void cmdTextadd(CommandContext ctx) {
        SettingsConfig c = cfg();
        if (!ctx.hasArg(0)) {
            notificationsService.error("Вы не указали текст.");
            return;
        }
        String text = ctx.arg(0).replace("&", "§");
        if (text.length() > MAX_TEXT_LENGTH) {
            notificationsService.error("Текст слишком длинный! Длина текста: %s (максимум %s)".formatted(text.length(), MAX_TEXT_LENGTH));
            return;
        }
        c.getTextsList().add(text);
        notificationsService.success("Вы добавили новый текст.");
        configManagerService.saveConfig(c);
    }

    private void cmdTextremove(CommandContext ctx) {
        SettingsConfig c = cfg();
        List<String> textsList = c.getTextsList();
        if (textsList.isEmpty()) {
            notificationsService.error("У вас нет настроенных текстов.");
            return;
        }
        if (!ctx.hasArg(0)) {
            notificationsService.error("Вы не указали номер текста.");
            return;
        }
        String indexText = ctx.arg(0);
        if (!chatService.checkCorrectInt(indexText)) {
            notificationsService.error("Некорректный номер текста.");
            return;
        }
        int intIndex = Integer.parseInt(indexText) - 1;
        if (intIndex >= textsList.size() || intIndex < 0) {
            notificationsService.error("Элемента с таким номером в списке ваших текстов не существует.");
            return;
        }
        textsList.remove(intIndex);
        notificationsService.addNotification(NotificationType.SUCCESS, "%s%sУспех".formatted(GREEN, BOLD),
                "Вы удалили текст номер %s%s%s.".formatted(ctx.arg(0), AQUA, BOLD), 5f);
        configManagerService.saveConfig(c);
    }

    private void cmdSetcopy(CommandContext ctx) {
        SettingsConfig c = cfg();
        if (!ctx.hasArg(0)) {
            c.setCopyButtonText("§f§l[§a§lcopy§f§l]");
            notificationsService.success("Текст кнопки был сброшен.");
        } else {
            c.setCopyButtonText(ctx.arg(0).replace("&", "§"));
            notificationsService.success("Вы установили новый текст кнопки копирования.");
        }
        configManagerService.saveConfig(c);
    }

    private void cmdSetmarker(CommandContext ctx) {
        SettingsConfig c = cfg();
        if (!ctx.hasArg(0)) {
            c.setPlayerMarker("§d§l[CHECK]");
            notificationsService.success("Текст метки был сброшен.");
        } else {
            c.setPlayerMarker(ctx.arg(0).replace("&", "§"));
            notificationsService.success("Вы установили новый текст маркера.");
        }
        configManagerService.saveConfig(c);
    }

    private void cmdSetspydelay(CommandContext ctx) {
        SettingsConfig c = cfg();
        if (!ctx.hasArg(0)) {
            notificationsService.error("Вы не указали число.");
            return;
        }
        String valueText = ctx.arg(0);
        if (!chatService.checkCorrectInt(valueText)) {
            notificationsService.error("Некорректное число.");
            return;
        }
        c.setSpyDelay(Integer.parseInt(valueText));
        notificationsService.success("Вы установили новую задержку в spy: %s.".formatted(c.getSpyDelay()));
        configManagerService.saveConfig(c);
    }

    private void cmdHudscale(CommandContext ctx) {
        SettingsConfig c = cfg();
        if (!ctx.hasArg(0)) {
            notificationsService.error("Вы не указали размер худа.");
            return;
        }
        String valueText = ctx.arg(0).replace(",", ".");
        float value;
        try {
            value = Float.parseFloat(valueText);
        } catch (NumberFormatException e) {
            notificationsService.error("Некорректное число.");
            return;
        }
        if (value < HUD_SCALE_MIN || value > HUD_SCALE_MAX) {
            notificationsService.error("Размер худа должен быть в диапазоне от %s до %s.".formatted(HUD_SCALE_MIN, HUD_SCALE_MAX));
            return;
        }
        c.setHudScale(value);
        notificationsService.success("Размер худа установлен: %s.".formatted(c.getHudScale()));
        configManagerService.saveConfig(c);
    }

    private void cmdSetsoundsvolume(CommandContext ctx) {
        SettingsConfig c = cfg();
        if (!ctx.hasArg(0)) {
            notificationsService.error("Вы не указали число.");
            return;
        }
        String valueText = ctx.arg(0);
        if (!chatService.checkCorrectInt(valueText)) {
            notificationsService.error("Некорректное число.");
            return;
        }
        c.setSoundsVolume(Integer.parseInt(valueText));
        notificationsService.success("Вы установили новую громкость звуков: %s.".formatted(c.getSoundsVolume()));
        configManagerService.saveConfig(c);
    }

    private void cmdTextedit(CommandContext ctx) {
        SettingsConfig c = cfg();
        List<String> textsList = c.getTextsList();
        if (textsList.isEmpty()) {
            notificationsService.error("У вас нет настроенных текстов.");
            return;
        }
        if (!ctx.hasArg(0)) {
            notificationsService.error("Вы не указали номер текста и новый текст.");
            return;
        }
        String indexText = ctx.arg(0);
        if (!chatService.checkCorrectInt(indexText)) {
            notificationsService.error("Некорректный номер текста.");
            return;
        }
        int index = Integer.parseInt(indexText) - 1;
        if (!ctx.hasArg(1)) {
            notificationsService.error("Вы не указали новый текст.");
            return;
        }
        if (index >= textsList.size() || index < 0) {
            notificationsService.error("Элемента с таким номером в списке ваших текстов не существует.");
            return;
        }
        String text = ctx.arg(1).replace("&", "§");
        if (text.contains("%%")) {
            notificationsService.error("Текст не должен содержать '%%'.");
            return;
        }
        textsList.set(index, text);
        notificationsService.success("Вы изменили текст номер %s.".formatted((index + 1)));
        configManagerService.saveConfig(c);
    }
}
