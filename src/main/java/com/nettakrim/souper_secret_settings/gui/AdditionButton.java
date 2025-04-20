package com.nettakrim.souper_secret_settings.gui;

import com.mclegoman.luminance.common.util.Couple;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class AdditionButton extends HoverButtonWidget {
    public String addition;
    protected Consumer<AdditionButton> onRemove;
    protected Consumer<AdditionButton> onEdit;

    protected int dragState;

    public AdditionButton(String addition, Couple<Text,Text> message, int x, int width, int height, Consumer<String> onPress) {
        super(x, 0, width, height, message.getFirst(), message.getSecond(), (widget) -> onPress.accept(addition));
        this.addition = addition;
        this.onRemove = null;
        this.onEdit = null;
    }

    public void addRemoveListener(Consumer<AdditionButton> onRemove) {
        this.onRemove = onRemove;
    }

    public void addEditListener(Consumer<AdditionButton> onEdit) {
        this.onEdit = onEdit;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderWidget(context, mouseX, mouseY, delta);

        if (onRemove != null) {
            context.drawTexture(RenderLayer::getGuiTextured, ListWidget.ICON_TEXTURE, getX(), getY(), 0, 0, 10, 20, 40, 20, dragState < 0 ? ListWidget.texColWhite : ListWidget.texColBlack);
        }

        if (onEdit != null) {
            context.drawTexture(RenderLayer::getGuiTextured, ListWidget.ICON_TEXTURE, getX()+getWidth()-12, getY(), 20, 0, 10, 20, 40, 20, dragState > 0 ? ListWidget.texColWhite : ListWidget.texColBlack);
        }
    }

    @Override
    public void drawMessage(DrawContext context, TextRenderer textRenderer, int color) {
        int i = this.getX() + (onRemove == null ? 4 : 12);
        int j = this.getX() + this.getWidth() - 2;
        drawScrollableText(context, textRenderer, this.getMessage(), i, i, getY(), j, this.getY() + this.getHeight(), color);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        dragState = 0;
        if (mouseX < getX()+getWidth()-14 || onEdit == null) {
            if (mouseX > getX()+10 || onRemove == null) {
                super.onClick(mouseX, mouseY);
            } else {
                dragState = -1;
            }
        } else {
            dragState = 1;
        }
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        if (dragState == 1 && onEdit != null) {
            onEdit.accept(this);
        }
        if (dragState == -1 && mouseX < getX()+10 && mouseY > getY() && mouseY < getY()+getHeight() && onRemove != null) {
            onRemove.accept(this);
        }
        dragState = 0;
    }

    @Override
    protected boolean passesRangeCheck(int mouseX, int mouseY) {
        return onRemove == null || mouseX > getX() + 10;
    }
}
