package me.yuugao.holymoderation.client.gui.tabs.impl.main;

import me.yuugao.holymoderation.client.gui.drawable.element.impl.impl.SearchDrawableElement;
import me.yuugao.holymoderation.client.gui.screen.impl.MainGuiScreen;
import me.yuugao.holymoderation.client.gui.tabs.SettingsTab;
import me.yuugao.holymoderation.client.util.factory.DrawableElementFactory;
import me.yuugao.holymoderation.client.util.service.MinecraftService;
import me.yuugao.holymoderation.client.util.service.Render2DService;
import me.yuugao.holymoderation.client.util.service.SoundService;
import me.yuugao.holymoderation.client.util.service.ThemePalette;
import me.yuugao.holymoderation.client.util.service.ThemeService;
import me.yuugao.holymoderation.client.util.service.config.ConfigManagerService;
import me.yuugao.holymoderation.client.util.service.config.impl.SettingsConfig;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class SoundsTab extends SettingsTab {
    private static final String[][] SOUNDS = {
            {"success.wav", "Успех"},
            {"error.wav", "Ошибка"},
            {"warning.wav", "Предупреждение"},
            {"exception.wav", "Исключение"},
            {"update.wav", "Обновление"},
            {"twinksDone.wav", "Твинки готовы"}
    };

    private final SettingsConfig settingsConfig;
    private final SoundService soundService;
    private final List<float[]> playButtons = new ArrayList<>();

    public SoundsTab(MainGuiScreen parent, ThemeService themeService, ConfigManagerService configManagerService,
                     MinecraftService minecraftService, Render2DService render2DService, DrawableElementFactory factory) {
        super(parent, themeService, configManagerService, minecraftService, render2DService, factory);

        this.settingsConfig = configManagerService.getSettingsConfig();
        this.soundService = me.yuugao.holymoderation.client.di.DIAccessor.getDI().get(SoundService.class);

        addToggle("Звуки", settingsConfig.isSoundsEnabled(), v -> {
            settingsConfig.setSoundsEnabled(v);
            configManagerService.saveConfig(settingsConfig);
        });

        addSlider("Громкость", 0f, 100f, 1f, settingsConfig.getSoundsVolume(),
                v -> settingsConfig.setSoundsVolume(Math.round(v)),
                () -> String.valueOf(settingsConfig.getSoundsVolume()),
                () -> configManagerService.saveConfig(settingsConfig));

        for (String[] sound : SOUNDS) {
            String soundName = sound[0];
            addTextField(sound[1], "путь к .wav (пусто = стандарт)",
                    settingsConfig.getCustomSounds().getOrDefault(soundName, ""),
                    value -> {
                        if (value == null || value.isBlank()) {
                            settingsConfig.getCustomSounds().remove(soundName);
                        } else {
                            settingsConfig.getCustomSounds().put(soundName, value);
                        }
                        configManagerService.saveConfig(settingsConfig);
                    });
        }
    }

    @Override
    protected void renderExtra(DrawContext ctx, ThemePalette palette, float pW, float pH, int z, float scroll) {
        playButtons.clear();
        if (!searchFieldIsEmpty()) return;

        List<Row> visible = visibleRows();
        float btnW = 30f;
        float btnX = pW - PAD - btnW;

        for (int i = 0; i < visible.size(); i++) {
            Row row = visible.get(i);
            if (row.element instanceof SearchDrawableElement && i >= 2) {
                int soundIndex = i - 2;
                if (soundIndex >= SOUNDS.length) continue;
                float rowTop = contentStartY() + i * ROW_HEIGHT - scroll;
                float btnY = rowTop + (ROW_HEIGHT - 20f) / 2f;

                render2DService.renderSoftRoundedRectOutline(ctx, btnX, btnY, btnW, 20f, z,
                        9f, palette.primary, palette.primaryBright, 1f, 2f);
                render2DService.renderText(minecraftService.getClient().textRenderer, Text.literal("▶").asOrderedText(),
                        (int) (btnX + (btnW - 8f) / 2f), (int) (btnY + 6f), z, palette.onPrimary.getRGB(), false, ctx);

                playButtons.add(new float[]{btnX, btnY, btnW, 20f, soundIndex});
            }
        }
    }

    private boolean searchFieldIsEmpty() {
        return searchQuery().isBlank();
    }

    @Override
    public boolean onMouseClick(float mouseX, float mouseY) {
        if (super.onMouseClick(mouseX, mouseY)) return true;

        for (float[] b : playButtons) {
            if (mouseX >= b[0] && mouseX <= b[0] + b[2] && mouseY >= b[1] && mouseY <= b[1] + b[3]) {
                int soundIndex = (int) b[4];
                soundService.playSound(SOUNDS[soundIndex][0]);
                return true;
            }
        }
        return false;
    }
}
