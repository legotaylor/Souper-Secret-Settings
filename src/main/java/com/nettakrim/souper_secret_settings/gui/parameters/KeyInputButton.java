package com.nettakrim.souper_secret_settings.gui.parameters;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class KeyInputButton extends AbstractButton {
    private final int textWidth;
    private InputConstants.Key key;

    private boolean waiting;

    private Consumer<String> responder;

    public KeyInputButton(int x, int width, int height, Component message) {
        super(x + width/3, 0, width - width/3, height, message);
        this.textWidth = width/3;
    }

    public void setResponder(@NotNull Consumer<String> responder) {
        this.responder = responder;
    }

    public void setValue(String value) {
        try {
            key = InputConstants.getKey(value);
        } catch (Exception ignored) {
            key = InputConstants.UNKNOWN;
        }
    }

    @Override
    public boolean keyPressed(@NotNull KeyEvent keyEvent) {
        if (waiting) {
            key = InputConstants.getKey(keyEvent);
            waiting = false;
            responder.accept(key.getName());
            return true;
        }
        return super.keyPressed(keyEvent);
    }

    @Override
    public void onPress(@NotNull InputWithModifiers inputWithModifiers) {
        waiting = true;
    }

    @Override
    protected void renderContents(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        renderDefaultSprite(guiGraphics);

        Component text = key.getDisplayName();
        if (waiting) {
            text = Component.literal("> ").append(text.copy().withStyle(ChatFormatting.WHITE, ChatFormatting.UNDERLINE)).append(" <").withStyle(ChatFormatting.YELLOW);
        }

        renderScrollingStringOverContents(guiGraphics.textRenderer(), text, 2);
        guiGraphics.textRenderer().acceptScrollingWithDefaultCenter(getMessage().copy().setStyle(Style.EMPTY.withColor((this.active ? 16777215 : 10526880) | Mth.ceil(this.alpha * 255.0F) << 24)), this.getX()-textWidth, this.getX(), this.getY(), this.getY()+20);
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {

    }
}
