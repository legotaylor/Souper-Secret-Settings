package com.nettakrim.souper_secret_settings.gui;

import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.shaders.ShaderStack;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ConfigValueWidget extends TextWidget {
    protected boolean visible;

    protected final List<ParameterTextWidget> children;

    public ConfigValueWidget(int x, int width, int height, ShaderStack shaderStack, String name, @NotNull List<Object> objects) {
        super(x, 0, width, height, Text.literal(name), SouperSecretSettingsClient.client.textRenderer);

        children = new ArrayList<>();
        if (!objects.isEmpty()) {
            int childStart = width / 2;
            setWidth(childStart);
            int childWidth = (width - childStart) / objects.size();
            for (int i = 0; i < objects.size(); i++) {
                Object object = objects.get(i);
                String text = String.valueOf(object);
                ParameterTextWidget parameterTextWidget = new ParameterTextWidget(x + childStart + (childWidth * i), childWidth, height, Text.literal(String.valueOf(i)), shaderStack, text);
                //TODO: needs to effect the ShaderData
                //parameterTextWidget.onChange(...);
                parameterTextWidget.setText(text);
                children.add(parameterTextWidget);
            }
        }
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        if (visible) {
            super.renderWidget(context, mouseX, mouseY, delta);
            for (ParameterTextWidget parameterTextWidget : children) {
                parameterTextWidget.renderWidget(context, mouseX, mouseY, delta);
            }
        }
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        for (ParameterTextWidget parameterTextWidget : children) {
            parameterTextWidget.setVisible(visible);
        }
    }

    public void addToScreen(ListScreen<?> listScreen) {
        listScreen.addSelectable(this);
        for (ParameterTextWidget parameterTextWidget : children) {
            listScreen.addSelectable(parameterTextWidget);
        }
    }

    public void removeFromScreen(ListScreen<?> listScreen) {
        listScreen.removeSelectable(this);
        for (ParameterTextWidget parameterTextWidget : children) {
            listScreen.removeSelectable(parameterTextWidget);
        }
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        for (ParameterTextWidget parameterTextWidget : children) {
            parameterTextWidget.setY(y);
        }
    }
}
