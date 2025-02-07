package com.nettakrim.souper_secret_settings.gui.parameters;

import com.mclegoman.luminance.client.data.ClientData;
import com.nettakrim.souper_secret_settings.gui.ParameterTextWidget;
import com.nettakrim.souper_secret_settings.shaders.ShaderLayer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public class CalculationInputWidget extends ParameterTextWidget {
    private final int textWidth;

    public CalculationInputWidget(int x, int width, int height, Text message, ShaderLayer layer, String defaultValue) {
        super(x+width/3, width - width/3, height, message, layer, defaultValue);
        this.textWidth = width/3;
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderWidget(context, mouseX, mouseY, delta);
        drawScrollableText(context, ClientData.minecraft.textRenderer, getMessage(), this.getX()-textWidth, this.getY(), this.getX(), this.getY()+20, (this.active ? 16777215 : 10526880) | MathHelper.ceil(this.alpha * 255.0F) << 24);
    }
}
