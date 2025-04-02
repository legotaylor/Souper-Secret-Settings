package com.nettakrim.souper_secret_settings.gui;

import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public abstract class ScrollScreen extends Screen {
    protected ScrollWidget scrollWidget;

    protected ScrollScreen(Text title) {
        super(title);
    }

    protected void createScrollWidget(int start) {
        scrollWidget = new ScrollWidget(SoupGui.listGap, start, ListScreen.scrollWidth, height-start-SoupGui.listGap, Text.literal("scroll"), this::setScroll);
        addDrawableChild(scrollWidget);
    }

    public abstract void setScroll(int scroll);

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        context.enableScissor(ListScreen.listX, scrollWidget.getY(), width, height);
        renderScrollables(context, mouseX, mouseY, delta);
        context.disableScissor();

        SouperSecretSettingsClient.soupGui.drawCurrentHoverText(context, mouseX, mouseY);
    }

    protected abstract void renderScrollables(DrawContext context, int mouseX, int mouseY, float delta);

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scrollWidget.offsetScroll(verticalAmount*-20);
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    protected void applyBlur() {}

    @Override
    protected void renderDarkening(DrawContext context, int x, int y, int width, int height) {}

    @Override
    public boolean shouldPause() {return false;}

}
