package com.nettakrim.souper_secret_settings.gui;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class CycleWidget extends ButtonWidget {
    protected final Consumer<Integer> advance;
    protected final Supplier<Text> getText;

    public CycleWidget(int x, int width, Consumer<Integer> advance, Supplier<Text> getText) {
        super(x, 0, width, 20, getText.get(), null, ButtonWidget.DEFAULT_NARRATION_SUPPLIER);
        this.advance = advance;
        this.getText = getText;
    }

    @Override
    public void onPress() {
        advance.accept(Screen.hasShiftDown() ? -1 : 1);
        setMessage(getText.get());
    }

    public static int cycleInt(int value, int max) {
        if (value < 0) return max;
        if (value > max) return 0;
        return value;
    }
}
