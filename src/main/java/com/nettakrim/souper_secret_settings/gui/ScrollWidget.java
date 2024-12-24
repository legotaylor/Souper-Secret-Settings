package com.nettakrim.souper_secret_settings.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;

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

        double barHeight = getBarHeight();

        double fraction = scrollY/scrollHeight * (1-barHeight);

        context.drawGuiTexture(RenderLayer::getGuiTextured, SCROLLER_TEXTURE, getX(), (int)(fraction*getHeight()) + getY(), getWidth(), (int)(barHeight*getHeight()), ColorHelper.getWhite(alpha));
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

    protected void setScroll(double mouseY) {
        double barHeight = getBarHeight();
        if (barHeight == 1) {
            scrollY = 0;
        } else {
            double fraction = (mouseY - getY()) / getHeight();
            fraction = Math.clamp((barHeight - 2 * fraction) / (2 * barHeight - 2), 0, 1);
            scrollY = fraction * scrollHeight;
        }

        onSetScroll.accept((int)scrollY);
    }

    public void setContentHeight(int height) {
        scrollHeight = Math.max(height - getHeight(), 0);
        setScroll(Math.min(scrollY, scrollHeight));
    }

    protected double getBarHeight() {
        return getHeight()/((double)scrollHeight+getHeight());
    }
}
