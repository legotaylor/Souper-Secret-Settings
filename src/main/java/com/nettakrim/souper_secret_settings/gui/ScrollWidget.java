package com.nettakrim.souper_secret_settings.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;

import java.util.function.IntConsumer;

public class ScrollWidget extends ClickableWidget {
    private static final Identifier SCROLLER_TEXTURE = Identifier.ofVanilla("widget/scroller");
    private static final Identifier SCROLLER_BACKGROUND_TEXTURE = Identifier.ofVanilla("widget/scroller_background");

    protected double scrollY;

    protected int scrollHeight;

    protected final IntConsumer onSetScroll;

    public ScrollWidget(int x, int y, int width, int height, Text message, IntConsumer onSetScroll) {
        super(x, y, width, height, message);
        this.onSetScroll = onSetScroll;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        context.drawGuiTexture(RenderLayer::getGuiTextured, SCROLLER_BACKGROUND_TEXTURE, getX(), getY(), getWidth(), getHeight(), ColorHelper.getWhite(alpha));

        int barHeight = (int)(getBarHeight()*getHeight());
        int y = scrollHeight > 0 ? (int)Math.round(scrollY/scrollHeight * (getHeight()-barHeight)) : 0;

        context.drawGuiTexture(RenderLayer::getGuiTextured, SCROLLER_TEXTURE, getX(), y + getY(), getWidth(), barHeight, ColorHelper.getWhite(alpha));
        context.drawGuiTexture(RenderLayer::getGuiTextured, SCROLLER_TEXTURE, getX(), y + getY() + MathHelper.floor(barHeight/2f), getWidth(), 1, ColorHelper.getArgb(255, 128, 128, 128));
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {

    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        setScroll(mouseY);
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
        setScroll(mouseY);
    }

    public void offsetScroll(double offset) {
        setScrollY(scrollY + offset);
    }

    protected void setScroll(double mouseY) {
        double barHeight = getBarHeight();
        if (barHeight == 1) {
            setScrollY(0);
        } else {
            double fraction = (mouseY - getY()) / getHeight();
            setScrollY((barHeight - 2 * fraction) / (2 * barHeight - 2) * scrollHeight);
        }
    }

    protected void setScrollY(double to) {
        scrollY = Math.clamp(to, 0, scrollHeight);
        onSetScroll.accept((int)scrollY);
    }

    public void setContentHeight(int height) {
        scrollHeight = Math.max(height - getHeight(), 0);
        setScrollY(Math.min(scrollY, scrollHeight));
    }

    protected double getBarHeight() {
        return getHeight()/((double)scrollHeight+getHeight());
    }
}
