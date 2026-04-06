package com.nettakrim.souper_secret_settings.gui;

import com.mclegoman.luminance.client.data.ClientData;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class DraggableTextFieldWidget extends EditBox implements ListChild {
    public boolean disableDrag = false;
    public Float dragValue = null;

    public DraggableTextFieldWidget(int x, int width, int height, Component message) {
        super(ClientData.minecraft.font, x, 0, width, height, message);
    }

    @Override
    protected void onDrag(@NotNull MouseButtonEvent click, double deltaX, double deltaY) {
        if (disableDrag) {
            return;
        }

        try {
            float f;
            if (dragValue == null) {
                f = Float.parseFloat(getValue());
            } else {
                f = dragValue;
                dragValue = null;
            }
            f += (float)(deltaX/50.0 * Math.max(Math.abs(f), 0.5f));
            setValue(String.valueOf(f));
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
    public void setValue(@NotNull String text) {
        super.setValue(text);
        this.moveCursorToStart(false);
    }
}
