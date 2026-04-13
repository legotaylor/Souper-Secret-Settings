package com.nettakrim.souper_secret_settings.gui;


import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.InputWithModifiers;
import org.jetbrains.annotations.NotNull;

public class CycleButton extends Button {
    protected final Consumer<Integer> advance;
    protected final Supplier<net.minecraft.network.chat.Component> getText;

    public CycleButton(int x, int width, Consumer<Integer> advance, Supplier<net.minecraft.network.chat.Component> getText) {
        super(x, 0, width, 20, getText.get(), (button) -> {}, Button.DEFAULT_NARRATION);
        this.advance = advance;
        this.getText = getText;
    }

    @Override
    public void onPress(InputWithModifiers input) {
        advance.accept(input.hasShiftDown() ? -1 : 1);
        setMessage(getText.get());
    }

    @Override
    protected void renderContents(@NotNull GuiGraphics context, int mouseX, int mouseY, float deltaTicks) {
        renderDefaultSprite(context);
    }

    public static int cycleInt(int value, int max) {
        if (value < 0) return max;
        if (value > max) return 0;
        return value;
    }
}
