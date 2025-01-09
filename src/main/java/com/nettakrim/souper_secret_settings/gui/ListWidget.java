package com.nettakrim.souper_secret_settings.gui;

import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;

public abstract class ListWidget extends CollapseWidget {
    private static final ButtonTextures TEXTURES = new ButtonTextures(Identifier.ofVanilla("widget/button"), Identifier.ofVanilla("widget/button_disabled"), Identifier.ofVanilla("widget/button_highlighted"));

    protected int dragState;

    public ListWidget(int x, int width, Text message, ListScreen<?> listScreen) {
        super(x, width, message, listScreen);
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        int endY = getY()+baseHeight;
        float buttonColor = isActive() ? 1f : 0.5f;
        float dragColor = dragState > 0 ? 1f : 0f;

        context.drawGuiTexture(RenderLayer::getGuiTextured, TEXTURES.get(this.active, this.isSelected()), this.getX(), this.getY(), this.getWidth(), this.getHeight(), ColorHelper.fromFloats(this.alpha, buttonColor, buttonColor, buttonColor));
        drawScrollableText(context, SouperSecretSettingsClient.client.textRenderer, this.getMessage(), this.getX()+2, this.getY(), this.getX()+this.getWidth()-2, endY, (this.active ? 16777215 : 10526880) | MathHelper.ceil(this.alpha * 255.0F) << 24);
        context.fill(getDragStart(), getY(), getX()+getWidth(), endY, ColorHelper.fromFloats(0.5f, dragColor, dragColor, dragColor));

        super.renderWidget(context, mouseX, mouseY, delta);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        dragState = 0;
        if (mouseX < getDragStart()) {
            super.onClick(mouseX, mouseY);
        } else {
            dragState = 1;
        }
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        if (dragState == 1) {
            setActive(!isActive());
        }
        dragState = 0;
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
        if (dragState == 0 || deltaY == 0) return;

        double offset = mouseY-getY();
        if (offset < -baseHeight*0.25 && deltaY < 0) {
            listScreen.swapEntry(this, -1);
            dragState = 2;
        } else if (offset > baseHeight*1.25 && deltaY > 0) {
            listScreen.swapEntry(this, 1);
            dragState = 2;
        }
    }

    protected int getDragStart() {
        return getX()+getWidth()-10;
    }

    public abstract boolean isActive();

    public abstract void setActive(boolean to);
}
