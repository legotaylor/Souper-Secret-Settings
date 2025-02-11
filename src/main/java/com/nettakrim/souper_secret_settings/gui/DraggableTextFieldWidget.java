package com.nettakrim.souper_secret_settings.gui;

import com.mclegoman.luminance.client.data.ClientData;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class DraggableTextFieldWidget extends TextFieldWidget implements ListChild {
    public boolean disableDrag = false;
    public Float dragValue = null;

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
            setText(Float.toString(f + (float)(deltaX/50.0 * Math.max(Math.abs(f), 0.5f))));
            this.setCursorToStart(false);
        } catch (Exception ignored) {}
    }

    @Override
    public int getCollapseHeight() {
        return getHeight();
    }

    @Override
    public void onRemove() {

    }
}
