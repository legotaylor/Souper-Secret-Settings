package com.nettakrim.souper_secret_settings.gui;

import com.mclegoman.luminance.client.shaders.overrides.OverrideSource;
import com.mclegoman.luminance.client.shaders.uniforms.config.UniformConfig;
import com.nettakrim.souper_secret_settings.shaders.MixOverrideSource;
import com.nettakrim.souper_secret_settings.shaders.ParameterOverrideSource;
import com.nettakrim.souper_secret_settings.shaders.ShaderStack;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ConfigWidget extends ParameterTextWidget {
    protected final List<ConfigValueWidget> children;
    protected final ListScreen<?> listScreen;

    protected Consumer<ConfigWidget> onChange;

    public OverrideSource overrideSource;

    public ConfigWidget(int x, int width, int height, Text message, ShaderStack stack, String defaultValue, ListScreen<?> listScreen) {
        super(x, width, height, message, stack, defaultValue);

        children = new ArrayList<>();
        this.listScreen = listScreen;

        setText(defaultValue);
        setChangedListener(this::setValue);
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        int heightTemp = getHeight();
        setHeight(20);
        super.renderWidget(context, mouseX, mouseY, delta);
        setHeight(heightTemp);

        for (ConfigValueWidget child : children) {
            child.renderWidget(context, mouseX, mouseY, delta);
        }
    }

    protected void setValue(String value) {
        for (ConfigValueWidget child : children) {
            child.removeFromScreen(listScreen);
        }
        children.clear();

        overrideSource = ParameterOverrideSource.parameterSourceFromString(value);
        if (!value.isEmpty() && overrideSource instanceof ParameterOverrideSource) {
            overrideSource = new MixOverrideSource(overrideSource);
            UniformConfig templateConfig = overrideSource.getTemplateConfig();
            for (String name : templateConfig.getNames()) {
                List<Object> objects = templateConfig.getObjects(name);
                if (objects != null) {
                    ConfigValueWidget child = new ConfigValueWidget(getX(), getWidth(), 20, stack, name, objects);
                    child.addToScreen(listScreen);
                    children.add(child);
                }
            }
        }

        onChange();
        setHeight(20*(1+children.size()));
        listScreen.updateSpacing();
    }

    public void onChange(Consumer<ConfigWidget> onChange) {
        this.onChange = onChange;
    }

    protected void onChange() {
        if (onChange != null) {
            onChange.accept(this);
        }
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        setChildrenPos(y);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        for (ConfigValueWidget child : children) {
            child.setVisible(visible);
        }
    }

    protected void setChildrenPos(int y) {
        for (int i = 0; i < children.size(); i++) {
            children.get(i).setY(y+(i+1)*20);
        }
    }
}
