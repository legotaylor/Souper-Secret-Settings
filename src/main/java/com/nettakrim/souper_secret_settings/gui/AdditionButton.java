package com.nettakrim.souper_secret_settings.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;

import java.util.function.Consumer;

public class AdditionButton extends ButtonWidget {
    protected String addition;
    protected Consumer<AdditionButton> onRemove;

    protected boolean deleting;

    public AdditionButton(String addition, int x, int width, int height, Consumer<String> onPress) {
        super(x, 0, width, height, Text.literal(addition), (widget) -> onPress.accept(addition), ButtonWidget.DEFAULT_NARRATION_SUPPLIER);
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
    public void onClick(double mouseX, double mouseY) {
        deleting = mouseX < getX() + 10;
        if (!deleting) {
            super.onClick(mouseX, mouseY);
        }
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        if (deleting && mouseX < getX()+10 && mouseY > getY() && mouseY < getY()+getHeight()) {
            onRemove.accept(this);
        }
        deleting = false;
    }
}
