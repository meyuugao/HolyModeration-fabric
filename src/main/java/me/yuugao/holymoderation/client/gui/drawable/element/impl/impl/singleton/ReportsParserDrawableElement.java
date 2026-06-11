package me.yuugao.holymoderation.client.gui.drawable.element.impl.impl.singleton;

import me.yuugao.holymoderation.client.di.annotations.Inject;
import me.yuugao.holymoderation.client.di.annotations.Singleton;
import me.yuugao.holymoderation.client.gui.drawable.element.impl.StatefulDrawableElement;
import me.yuugao.holymoderation.client.gui.drawable.element.impl.impl.button.impl.ImageButtonDrawableElement;
import me.yuugao.holymoderation.client.gui.drawable.element.impl.impl.button.impl.TextButtonDrawableElement;
import me.yuugao.holymoderation.client.gui.drawable.element.state.impl.ReportsParserRenderState;
import me.yuugao.holymoderation.client.gui.drawable.element.state.provider.impl.ReportsParserRenderStateProvider;
import me.yuugao.holymoderation.client.gui.drawable.render.PivotMode;
import me.yuugao.holymoderation.client.util.factory.DrawableElementFactory;
import me.yuugao.holymoderation.client.util.service.AnimationService;
import me.yuugao.holymoderation.client.util.service.MinecraftService;
import me.yuugao.holymoderation.client.util.service.Render2DService;
import me.yuugao.holymoderation.client.util.service.config.ConfigManagerService;
import me.yuugao.holymoderation.client.util.service.config.impl.GuiConfig;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

@Singleton
public class ReportsParserDrawableElement extends StatefulDrawableElement<ReportsParserRenderState> {
    private final float ITEM_HEIGHT = 24f;
    private final float ITEM_SPACING = 4f;
    private final float LIST_HEIGHT_FACTOR = 0.81f;
    private final float EDGE_PADDING = 5f;
    private final float BUTTON_WIDTH_FACTOR = 0.84f;
    private final float SCROLL_SPEED = 12f;
    private final TextButtonDrawableElement startButton;
    private final ImageButtonDrawableElement clearButton;
    private final List<TextButtonDrawableElement> playerButtons = new ArrayList<>();
    private final Render2DService render2DService;
    private final MinecraftService minecraftService;
    private final ConfigManagerService configManagerService;
    private final DrawableElementFactory drawableElementFactory;
    private float scrollOffset = 0f;
    private float maxScroll = 0f;

    @Inject
    public ReportsParserDrawableElement(AnimationService animationService, MinecraftService minecraftService,
                                        Render2DService render2DService, ConfigManagerService configManagerService,
                                        DrawableElementFactory drawableElementFactory,
                                        ReportsParserRenderStateProvider reportsParserRenderStateProvider) {
        super(animationService, PivotMode.CENTER, reportsParserRenderStateProvider);

        this.drawableElementFactory = drawableElementFactory;
        this.render2DService = render2DService;
        this.minecraftService = minecraftService;
        this.configManagerService = configManagerService;

        this.startButton = drawableElementFactory.createTextButton(PivotMode.LEFT_DOWN,
                () -> System.out.println("button1 clicked"), true, Text.literal("пропарсить"));
        this.clearButton = drawableElementFactory.createImageButton(PivotMode.RIGHT_DOWN, this::clearPlayers, true);
    }

    @Override
    protected void initPosition(DrawContext ctx) {
        setRelativePos(0.3f, 0.4f);
        this.scale.reset(0f);
        setWidth(150f);
        setHeight(180f);
    }

    public void addButton(String text) {
        playerButtons.add(drawableElementFactory.createTextButton(PivotMode.CENTER,
                () -> System.out.println("Clicked " + text), true, Text.literal(text)));
    }

    public void clearPlayers() {
        playerButtons.clear();
    }

    @Override
    public void onMouseScroll(double dx, double dy, int x, int y) {
        if (!isMouseOverList(x, y)) return;
        scrollOffset -= (float) (dy * SCROLL_SPEED);
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
    }

    private boolean isMouseOverList(int mouseX, int mouseY) {
        Window window = minecraftService.getClient().getWindow();

        float ss = Math.min(window.getScaledWidth() / 1440f, window.getScaledHeight() / 810f);
        float totalScale = scale.get() * ss;

        float absX = getAnchorX(window.getScaledWidth()) - pivotMode.getXFactor() * width * totalScale;
        float absY = getAnchorY(window.getScaledHeight()) - pivotMode.getYFactor() * height * totalScale;
        float listHeight = height * totalScale * LIST_HEIGHT_FACTOR;

        return mouseX >= absX && mouseX <= absX + width * totalScale &&
                mouseY >= absY && mouseY <= absY + listHeight;
    }

    @Override
    protected void render(DrawContext ctx, int z, ReportsParserRenderState state) {
        GuiConfig guiConfig = configManagerService.getGuiConfig();
        MatrixStack ms = ctx.getMatrices();

        this.scale.setTarget(state.animTarget());
        this.scale.update();

        if (scale.get() < 0.01f) return;

        float itemTotal = ITEM_HEIGHT + ITEM_SPACING;
        float listHeight = getHeight() * LIST_HEIGHT_FACTOR;
        float buttonWidth = getWidth() * BUTTON_WIDTH_FACTOR;

        float contentHeight = playerButtons.size() * itemTotal - ITEM_SPACING;
        maxScroll = Math.max(0, contentHeight - listHeight + EDGE_PADDING);

        render2DService.setupRender();

        render2DService.renderSoftRoundedRectOutline(ms, 0f, 0f, getWidth(), getHeight(), z,
                getHeight() / 8f, guiConfig.getMainColor(), guiConfig.getSecondColor(), 2f, 3f);

        Identifier clearIcon = Identifier.of("minecraft", "textures/gui/clear.png");
        clearButton.updateRenderForParent(ctx, clearIcon, 0.92f, 0.95f, 5f, 0.13f,
                getWidth(), getHeight(), z, 6f,
                guiConfig.getMainColor().brighter(), guiConfig.getSecondColor().brighter(), 2f, 3f);

        startButton.updateRenderForParent(ctx, 0.08f, 0.95f, 85f,
                getWidth(), getHeight(), z, 6f,
                guiConfig.getMainColor().brighter(), guiConfig.getSecondColor().brighter(), 2f, 3f);

        float baseAbsX = getAbsoluteX(ctx.getScaledWindowWidth());
        float baseAbsY = getAbsoluteY(ctx.getScaledWindowHeight());
        float scaledW = getScaledWidth();

        int clipX = (int) baseAbsX;
        int clipY = (int) (baseAbsY + EDGE_PADDING * screenScale);
        int clipX2 = (int) (baseAbsX + scaledW);
        int clipY2 = (int) Math.ceil(baseAbsY + listHeight * screenScale);
        ctx.enableScissor(clipX, clipY, clipX2, clipY2);

        renderVisibleButtons(ctx, z, guiConfig, listHeight, buttonWidth, itemTotal);

        ctx.disableScissor();

        render2DService.endRender();
    }

    private void renderVisibleButtons(DrawContext ctx, int z, GuiConfig guiConfig,
                                      float listHeight, float buttonWidth, float itemTotal) {
        int firstIdx = Math.max(0, (int) Math.floor(scrollOffset / itemTotal));
        int lastIdx = Math.min(playerButtons.size() - 1,
                (int) Math.ceil((scrollOffset + listHeight) / itemTotal));

        for (int i = firstIdx; i <= lastIdx; i++) {
            float y = i * itemTotal - scrollOffset + EDGE_PADDING;

            if (y + ITEM_HEIGHT < EDGE_PADDING || y > listHeight) continue;

            playerButtons.get(i).updateRenderForParent(ctx, 0.5f, (y + ITEM_HEIGHT / 2f) / getHeight(),
                    buttonWidth, getWidth(), getHeight(), z, 6f,
                    guiConfig.getMainColor().brighter(), guiConfig.getSecondColor().brighter(), 2f, 3f);
        }
    }
}