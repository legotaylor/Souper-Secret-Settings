package com.nettakrim.souper_secret_settings.gui;

import com.mclegoman.luminance.client.data.ClientData;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.actions.ToggleAction;
import com.nettakrim.souper_secret_settings.shaders.Toggleable;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;

public abstract class ListWidget extends CollapseWidget {
    private static final ButtonTextures TEXTURES = new ButtonTextures(Identifier.ofVanilla("widget/button"), Identifier.ofVanilla("widget/button_disabled"), Identifier.ofVanilla("widget/button_highlighted"));

    private static final Identifier ICON_TEXTURE = Identifier.of(SouperSecretSettingsClient.MODID, "textures/gui/icons.png");

    protected int dragState;

    public ListWidget(int x, int width, Text message, ListScreen<?> listScreen) {
        super(x, width, message, listScreen);
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        float buttonColor = getToggleable().isActive() ? 1f : 0.5f;
        float dragColor = dragState > 0 ? 1f : 0f;
        float deleteColor = dragState < 0 ? 1f : 0f;

        context.drawGuiTexture(RenderLayer::getGuiTextured, TEXTURES.get(this.active, this.isSelected()), this.getX(), this.getY(), this.getWidth(), getCollapseHeight(), ColorHelper.fromFloats(this.alpha, buttonColor, buttonColor, buttonColor));
        drawScrollableText(context, ClientData.minecraft.textRenderer, this.getMessage(), this.getX()+2, this.getY(), this.getX()+this.getWidth()-2, getY()+getHeight(), (this.active ? 16777215 : 10526880) | MathHelper.ceil(this.alpha * 255.0F) << 24);
        context.drawTexture(RenderLayer::getGuiTextured, ICON_TEXTURE, getX(), getY(), 0, 0, 10, 20, 20, 20, ColorHelper.fromFloats(0.5f, deleteColor, deleteColor, deleteColor));
        context.drawTexture(RenderLayer::getGuiTextured, ICON_TEXTURE, getX()+getWidth()-10, getY(), 10, 0, 10, 20, 20, 20, ColorHelper.fromFloats(0.5f, dragColor, dragColor, dragColor));

        super.renderWidget(context, mouseX, mouseY, delta);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        dragState = 0;
        if (mouseX < getX()+getWidth()-10) {
            if (mouseX > getX()+10) {
                setExpanded(!expanded);
            } else {
                dragState = -1;
            }
        } else {
            dragState = 1;
        }
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        if (dragState == 1) {
            Toggleable toggleable = getToggleable();
            new ToggleAction(toggleable).addToHistory();
            toggleable.toggle();
        }
        if (dragState == -1 && mouseX < getX()+10 && mouseY > getY() && mouseY < getY()+getHeight()) {
            listScreen.removeEntry(this);
        }
        dragState = 0;
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
        if (dragState <= 0 || deltaY == 0) return;

        double offset = mouseY-getY();
        if (offset < -getHeight()*0.25 && deltaY < 0) {
            listScreen.swapEntry(this, -1);
            dragState = 2;
        } else if (offset > getHeight()*1.25 && deltaY > 0) {
            listScreen.swapEntry(this, 1);
            dragState = 2;
        }
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {

    }

    protected abstract Toggleable getToggleable();
}
