package me.yuugao.holymoderation.client.gui.tabs.impl.main;

import me.yuugao.holymoderation.client.gui.drawable.element.impl.impl.SearchDrawableElement;
import me.yuugao.holymoderation.client.gui.drawable.element.impl.impl.button.impl.TextButtonDrawableElement;
import me.yuugao.holymoderation.client.gui.screen.impl.MainGuiScreen;
import me.yuugao.holymoderation.client.gui.tabs.SettingsTab;
import me.yuugao.holymoderation.client.util.factory.DrawableElementFactory;
import me.yuugao.holymoderation.client.util.service.MinecraftService;
import me.yuugao.holymoderation.client.util.service.Render2DService;
import me.yuugao.holymoderation.client.util.service.ThemePalette;
import me.yuugao.holymoderation.client.util.service.ThemeService;
import me.yuugao.holymoderation.client.util.service.config.ConfigManagerService;
import me.yuugao.holymoderation.client.util.service.config.impl.SettingsConfig;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class ChatTab extends SettingsTab {
    private static final int MAX_TEXT_LENGTH = 200;

    private final SettingsConfig settingsConfig;

    private final SearchDrawableElement addField;
    private final TextButtonDrawableElement addButton;
    private final TextButtonDrawableElement saveButton;
    private final List<float[]> rowHitboxes = new ArrayList<>();
    private int editIndex = -1;

    public ChatTab(MainGuiScreen parent, ThemeService themeService, ConfigManagerService configManagerService,
                   MinecraftService minecraftService, Render2DService render2DService, DrawableElementFactory factory) {
        super(parent, themeService, configManagerService, minecraftService, render2DService, factory);

        this.settingsConfig = configManagerService.getSettingsConfig();

        addToggle("Кнопка копирования", settingsConfig.isCopyButtonEnabled(), v -> {
            settingsConfig.setCopyButtonEnabled(v);
            configManagerService.saveConfig(settingsConfig);
        });

        addTextField("Текст кнопки", "§ = &", toDisplay(settingsConfig.getCopyButtonText()), s -> {
            settingsConfig.setCopyButtonText(toModel(s));
            configManagerService.saveConfig(settingsConfig);
        });

        addTextField("Маркер игрока", "§ = &", toDisplay(settingsConfig.getPlayerMarker()), s -> {
            settingsConfig.setPlayerMarker(toModel(s));
            configManagerService.saveConfig(settingsConfig);
        });

        addTextField("Ссылка на ВК", "vk.com/id... (пусто = из журнала)", settingsConfig.getCustomVk(), s -> {
            settingsConfig.setCustomVk(s == null ? "" : s);
            configManagerService.saveConfig(settingsConfig);
        });

        this.addField = factory.createSearch(null);
        this.addField.setPlaceholder("Новый текст (Ctrl+V — вставить)");
        this.addField.setMaxLength(MAX_TEXT_LENGTH);
        this.addButton = factory.createTextButton(me.yuugao.holymoderation.client.gui.drawable.render.PivotMode.LEFT_UP,
                this::doAdd, true, Text.literal("Добавить"));
        this.saveButton = factory.createTextButton(me.yuugao.holymoderation.client.gui.drawable.render.PivotMode.LEFT_UP,
                this::doSave, true, Text.literal("Сохранить"));
    }

    private void doAdd() {
        String text = addField.getQuery().trim();
        if (text.isEmpty()) return;
        if (text.length() > MAX_TEXT_LENGTH) text = text.substring(0, MAX_TEXT_LENGTH);
        settingsConfig.getTextsList().add(text);
        configManagerService.saveConfig(settingsConfig);
        addField.setQuerySilent("");
    }

    private void doSave() {
        if (editIndex < 0 || editIndex >= settingsConfig.getTextsList().size()) return;
        String text = addField.getQuery().trim();
        if (text.isEmpty()) return;
        if (text.length() > MAX_TEXT_LENGTH) text = text.substring(0, MAX_TEXT_LENGTH);
        settingsConfig.getTextsList().set(editIndex, text);
        configManagerService.saveConfig(settingsConfig);
        editIndex = -1;
        addField.setQuerySilent("");
    }

    @Override
    protected float extraHeight() {
        float h = 30f + (editIndex >= 0 ? 26f : 0f) + settingsConfig.getTextsList().size() * 24f + 12f;
        return h;
    }

    @Override
    protected void renderExtra(DrawContext ctx, ThemePalette palette, float pW, float pH, int z, float scroll) {
        rowHitboxes.clear();
        TextRenderer tr = minecraftService.getClient().textRenderer;

        float y = rowsEndY() - scroll + 8f;
        renderText(ctx, z, "Тексты для проверки", PAD, y, tr, palette.textSecondary);
        y += tr.fontHeight + 6f;

        float addW = pW - PAD * 2f - 110f;
        addField.updateRenderForParent(ctx, PAD / pW, y / pH, addW, 20f, pW, pH, z,
                8f, palette.surface, palette.outline, palette.textPrimary, palette.textMuted, palette.primary, 1.5f, 2f);
        addButton.updateRenderForParent(ctx, (pW - PAD - 100f) / pW, y / pH, 100f, pW, pH, z,
                8f, palette.primary, palette.primaryBright, 1.5f, 2f);

        y += 26f;

        if (editIndex >= 0) {
            saveButton.updateRenderForParent(ctx, (pW - PAD - 100f) / pW, y / pH, 100f, pW, pH, z,
                    8f, palette.primary, palette.primaryBright, 1.5f, 2f);
            y += 26f;
        }

        List<String> texts = settingsConfig.getTextsList();
        float rowH = 20f;
        float editW = 26f;
        float delW = 26f;
        float gap = 4f;
        float bodyW = pW - PAD * 2f - editW - delW - gap * 2f;

        for (int i = 0; i < texts.size(); i++) {
            String full = texts.get(i);
            String shown = full.length() > 60 ? full.substring(0, 60) + "…" : full;

            boolean editing = editIndex == i;

            render2DService.renderSoftRoundedRect(ctx, PAD, y, bodyW, rowH, z,
                    6f, editing ? palette.primaryDark : palette.surface, 0);
            renderText(ctx, z, (i + 1) + ". " + shown, PAD + 8f, y + 4f, tr, palette.textPrimary);

            float editX = PAD + bodyW + gap;
            float delX = editX + editW + gap;

            render2DService.renderSoftRoundedRectOutline(ctx, editX, y, editW, rowH, z,
                    6f, palette.surface, palette.outline, 1f, 1f);
            renderText(ctx, z, "✎", editX + 8f, y + 4f, tr, palette.textSecondary);

            render2DService.renderSoftRoundedRectOutline(ctx, delX, y, delW, rowH, z,
                    6f, palette.surface, palette.outline, 1f, 1f);
            renderText(ctx, z, "✕", delX + 9f, y + 4f, tr, palette.textMuted);

            rowHitboxes.add(new float[]{PAD, y, bodyW, rowH, i, editX, y, editW, rowH, delX, y, delW, rowH});
            y += 24f;
        }
    }

    private void renderText(DrawContext ctx, int z, String text, float x, float y, TextRenderer tr, java.awt.Color color) {
        render2DService.renderText(tr, Text.literal(text).asOrderedText(),
                (int) x, (int) y, z, color.getRGB(), false, ctx);
    }

    @Override
    public boolean onMouseClick(float mouseX, float mouseY) {
        float pW = parent.getWidth();
        float pH = parent.getHeight();

        if (super.onMouseClick(mouseX, mouseY)) return true;

        boolean overAdd = addField.isMouseOver(pW, pH, mouseX, mouseY);
        addField.setFocused(overAdd);
        if (overAdd) {
            addField.handleClick(pW, pH, mouseX, mouseY);
            return true;
        }
        if (addButton.hitInParent(pW, pH, mouseX, mouseY)) return true;
        if (editIndex >= 0 && saveButton.hitInParent(pW, pH, mouseX, mouseY)) return true;

        for (float[] b : rowHitboxes) {
            int index = (int) b[4];
            boolean inBody = mouseX >= b[0] && mouseX <= b[0] + b[2] && mouseY >= b[1] && mouseY <= b[1] + b[3];
            boolean inEdit = mouseX >= b[5] && mouseX <= b[5] + b[7] && mouseY >= b[6] && mouseY <= b[6] + b[8];
            boolean inDel = mouseX >= b[9] && mouseX <= b[9] + b[11] && mouseY >= b[10] && mouseY <= b[10] + b[12];

            if (inDel) {
                if (index >= 0 && index < settingsConfig.getTextsList().size()) {
                    settingsConfig.getTextsList().remove(index);
                    configManagerService.saveConfig(settingsConfig);
                    if (editIndex == index) {
                        editIndex = -1;
                        addField.setQuerySilent("");
                    } else if (editIndex > index) {
                        editIndex--;
                    }
                }
                return true;
            }
            if (inEdit || inBody) {
                if (index >= 0 && index < settingsConfig.getTextsList().size()) {
                    editIndex = index;
                    addField.setQuerySilent(settingsConfig.getTextsList().get(index));
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public void onMouseDrag(float mouseX, float mouseY) {
        super.onMouseDrag(mouseX, mouseY);
        addField.handleDrag(parent.getWidth(), parent.getHeight(), mouseX, mouseY);
    }

    @Override
    public void onMouseRelease() {
        super.onMouseRelease();
        addField.handleRelease();
    }

    @Override
    public boolean onCharTyped(char chr) {
        if (super.onCharTyped(chr)) return true;
        return addField.onCharTyped(chr);
    }

    @Override
    public boolean onKeyPress(int key, int scancode, int action, int modifiers) {
        if (super.onKeyPress(key, scancode, action, modifiers)) return true;
        return addField.onKeyPress(key, scancode, action, modifiers);
    }

    private static String toDisplay(String value) {
        return value == null ? "" : value.replace('§', '&');
    }

    private static String toModel(String value) {
        return value == null ? "" : value.replace('&', '§');
    }
}
