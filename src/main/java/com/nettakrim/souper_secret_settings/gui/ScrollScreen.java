package com.nettakrim.souper_secret_settings.gui;

import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public abstract class ScrollScreen extends Screen {
    protected ScrollWidget scrollWidget;

    protected ScrollScreen(Component title) {
        super(title);
    }

    protected void createScrollWidget(int start) {
        scrollWidget = new ScrollWidget(SoupGui.listGap, start, SoupGui.scrollWidth, height-start-SoupGui.listGap, Component.literal("scroll"), this::setScroll);
        addRenderableWidget(scrollWidget);
    }

    public abstract void setScroll(int scroll);

    @Override
    public void render(@NotNull GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        context.enableScissor(SoupGui.listX, scrollWidget.getY(), width, height);
        renderScrollables(context, mouseX, mouseY, delta);
        context.disableScissor();

        SouperSecretSettingsClient.soupGui.drawCurrentHoverText(context, mouseX, mouseY);
    }

    protected abstract void renderScrollables(GuiGraphics context, int mouseX, int mouseY, float delta);

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scrollWidget.offsetScroll(verticalAmount*-20);
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    protected void renderBlurredBackground(@NotNull GuiGraphics context) {}

    @Override
    protected void renderMenuBackground(@NotNull GuiGraphics context, int x, int y, int width, int height) {}

    @Override
    public boolean isPauseScreen() {return false;}

}
