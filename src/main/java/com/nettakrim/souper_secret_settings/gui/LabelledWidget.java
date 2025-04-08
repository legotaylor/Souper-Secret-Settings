package com.nettakrim.souper_secret_settings.gui;

import com.mclegoman.luminance.client.data.ClientData;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.function.BiFunction;

public class LabelledWidget extends ClickableWidget {
    protected static final ButtonTextures TEXTURES = new ButtonTextures(Identifier.ofVanilla("widget/button"), Identifier.ofVanilla("widget/button_disabled"), Identifier.ofVanilla("widget/button_highlighted"));

    public final ClickableWidget widget;

    public LabelledWidget(int x, int width, Text message, BiFunction<Integer, Integer, ClickableWidget> widgetFunction) {
        super(x, 0, width/3, 20, message);
        widget = widgetFunction.apply(x + width/3, width - width/3);
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        context.drawGuiTexture(RenderLayer::getGuiTextured, TEXTURES.get(true, false), getX(), getY(), getWidth()*2, getHeight(), -1);
        drawScrollableText(context, ClientData.minecraft.textRenderer, this.getMessage(), getX()+2, getY(), getX()+getWidth(), getY()+getHeight(), -1);

        widget.render(context, mouseX, mouseY, delta);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {

    }

    @Override
    public void setY(int y) {
        super.setY(y);
        widget.setY(y);
    }
}
