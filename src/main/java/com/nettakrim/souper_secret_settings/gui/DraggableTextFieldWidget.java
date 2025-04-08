package com.nettakrim.souper_secret_settings.gui;

import com.mclegoman.luminance.client.data.ClientData;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public class DraggableTextFieldWidget extends TextFieldWidget implements ListChild {
    public boolean disableDrag = false;
    public Float dragValue = null;
    public boolean round = false;
    public float min = -Float.MAX_VALUE;
    public float max = Float.MAX_VALUE;

    public DraggableTextFieldWidget(int x, int width, int height, Text message) {
        super(ClientData.minecraft.textRenderer, x, 0, width, height, message);
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
        if (disableDrag) {
            return;
        }

        try {
            float f;
            if (dragValue == null) {
                f = Float.parseFloat(getText());
            } else {
                f = dragValue;
                dragValue = null;
            }
            f += (float)(deltaX/50.0 * Math.max(Math.abs(f), round ? 4 : 0.5f));
            setValue(f);
        } catch (Exception ignored) {}
    }

    @Override
    public int getCollapseHeight() {
        return getHeight();
    }

    @Override
    public void onRemove() {

    }

    @Override
    public void setText(String text) {
        super.setText(text);
        this.setCursorToStart(false);
    }

    @Override
    public void write(String text) {
        super.write(text);
        fixOutOfBounds();
    }

    @Override
    public void eraseCharactersTo(int position) {
        super.eraseCharactersTo(position);
        fixOutOfBounds();
    }

    public void fixOutOfBounds() {
        try {
            float f = Float.parseFloat(getText());

            if (f < min || f > max) {
                setValue(f);
            } else if (round) {
                dragValue = f;
            }
        } catch (Exception ignored) {}
    }

    public void setValue(float value) {
        value = MathHelper.clamp(value, min, max);
        if (round) {
            dragValue = value;
            setText(Integer.toString(Math.round(value)));
        } else {
            setText(Float.toString(value));
        }
    }
}
