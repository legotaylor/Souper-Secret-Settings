package com.nettakrim.souper_secret_settings.gui;

import com.mclegoman.luminance.client.data.ClientData;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.function.Consumer;

public class DraggableIntWidget extends TextFieldWidget {
    protected final int min;
    protected final int max;
    protected final String defaultValue;
    protected final Consumer<Integer> onChange;

    float value;

    public DraggableIntWidget(int x, int width, int height, Text message, int min, int max, int defaultValue, Consumer<Integer> onChange) {
        super(ClientData.minecraft.textRenderer, x, 0, width, height, message);
        setChangedListener(this::onChange);
        this.min = min;
        this.max = max;
        this.onChange = onChange;
        value = defaultValue;
        this.defaultValue = String.valueOf(defaultValue);
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
        try {
            value += (float)(deltaX/50.0 * Math.max(Math.abs(value), 4));
            setText(String.valueOf(MathHelper.clamp(Math.round(value), min, max)));
        } catch (Exception ignored) {}
    }

    void onChange(String text) {
        if (defaultValue.startsWith(text)) {
            setSuggestion(defaultValue.substring(text.length()));
        } else {
            setSuggestion(null);
        }

        try {
            float f = Float.parseFloat(text);
            int i = Math.round(f);

            if (i != Math.round(value)) {
                value = f;
            }

            if (i >= min && i <= max) {
                onChange.accept(i);
            }
        } catch (Exception ignored) {}
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.isFocused()) {
            if (keyCode == 258) {
                String text = getText();
                if (defaultValue.startsWith(text)) {
                    setText(defaultValue);
                } else {
                    try {
                        float f = Float.parseFloat(text);
                        setText(Integer.toString(MathHelper.clamp(Math.round(f), min, max)));
                    } catch (Exception ignored) {
                        setText(defaultValue);
                    }
                }
                setCursorToEnd(false);
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void setText(String text) {
        super.setText(text);
        this.setCursorToStart(false);
    }
}
