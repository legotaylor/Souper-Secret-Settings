package com.nettakrim.souper_secret_settings.gui;

import com.mclegoman.luminance.common.util.Couple;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;

import java.util.function.Consumer;

public class AdditionButton extends HoverButtonWidget {
    protected String addition;
    protected Consumer<AdditionButton> onRemove;

    protected boolean deleting;

    public AdditionButton(String addition, Couple<Text,Text> message, int x, int width, int height, Consumer<String> onPress) {
        super(x, 0, width, height, message.getFirst(), message.getSecond(), (widget) -> onPress.accept(addition));
        this.addition = addition;
        this.onRemove = null;
    }

    public void addRemoveListener(Consumer<AdditionButton> onRemove) {
        this.onRemove = onRemove;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderWidget(context, mouseX, mouseY, delta);

        if (onRemove == null) {
            return;
        }

        float deleteColor = deleting ? 1f : 0f;
        context.drawTexture(RenderLayer::getGuiTextured, ListWidget.ICON_TEXTURE, getX(), getY(), 0, 0, 10, 20, 20, 20, ColorHelper.fromFloats(0.5f, deleteColor, deleteColor, deleteColor));
    }

    @Override
    public void drawMessage(DrawContext context, TextRenderer textRenderer, int color) {
        int i = this.getX() + (onRemove == null ? 4 : 12);
        int j = this.getX() + this.getWidth() - 2;
        drawScrollableText(context, textRenderer, this.getMessage(), i, i, getY(), j, this.getY() + this.getHeight(), color);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        deleting = (mouseX < getX() + 10) && (onRemove != null);
        if (!deleting) {
            super.onClick(mouseX, mouseY);
        }
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        if (deleting && mouseX < getX()+10 && mouseY > getY() && mouseY < getY()+getHeight() && onRemove != null) {
            onRemove.accept(this);
        }
        deleting = false;
    }

    @Override
    protected boolean passesRangeCheck(int mouseX, int mouseY) {
        return onRemove == null || mouseX > getX() + 10;
    }
}
