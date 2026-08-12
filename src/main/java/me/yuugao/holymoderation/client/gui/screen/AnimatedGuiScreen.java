package me.yuugao.holymoderation.client.gui.screen;

import me.yuugao.holymoderation.client.util.service.AnimationService;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import lombok.Getter;

public class AnimatedGuiScreen extends GuiScreen {
    private final AnimationService.Value progressAnim;
    @Getter
    private float animValue = 0f;

    private boolean closing = false;

    protected AnimatedGuiScreen(Text title, AnimationService animationService) {
        super(title);

        this.progressAnim = animationService.createValue(0f);
    }

    @Override
    protected void init() {
        progressAnim.reset(0f);
        progressAnim.setTarget(1f).setSpeed(1.2f);
        closing = false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        progressAnim.update();
        animValue = progressAnim.get();

        if (closing && progressAnim.isFinished() && progressAnim.getTarget() == 0f) {
            closing = false;
            ScreenEvents.remove(this);
            super.close();
            return;
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        if (!closing) {
            closing = true;
            progressAnim.setTarget(0f);
        }
    }

    @Override
    public void removed() {
        super.removed();
    }
}