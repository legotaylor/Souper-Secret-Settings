package com.nettakrim.souper_secret_settings.gui;

import com.mclegoman.luminance.client.shaders.overrides.OverrideConfig;
import com.mclegoman.luminance.client.shaders.overrides.OverrideSource;
import com.mclegoman.luminance.client.shaders.uniforms.config.MapConfig;
import com.mclegoman.luminance.client.shaders.uniforms.config.UniformConfig;
import com.nettakrim.souper_secret_settings.shaders.MixOverrideSource;
import com.nettakrim.souper_secret_settings.shaders.ParameterOverrideSource;
import com.nettakrim.souper_secret_settings.shaders.ShaderStack;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;

public class ConfigWidget extends ParameterTextWidget {
    protected final List<ConfigValueWidget> children;
    protected final ListScreen<?> listScreen;
    protected final Map<String, List<Object>> previousValues;
    protected final int index;

    protected Consumer<ConfigWidget> onChange;

    public OverrideSource overrideSource;


    public ConfigWidget(int x, int width, int height, Text message, ShaderStack stack, String defaultValue, ListScreen<?> listScreen, String initialValue, UniformConfig initialConfig, int index) {
        super(x, width, height, message, stack, defaultValue);

        children = new ArrayList<>();
        this.listScreen = listScreen;
        previousValues = new HashMap<>();
        this.index = index;

        setText(initialValue);
        createChildren(new OverrideConfig(initialConfig));

        setChangedListener(this::setValue);
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderWidget(context, mouseX, mouseY, delta);

        for (ConfigValueWidget child : children) {
            child.renderWidget(context, mouseX, mouseY, delta);
        }
    }

    protected void setValue(String value) {
        for (ConfigValueWidget child : children) {
            child.removeFromScreen(listScreen);
        }
        children.clear();

        if (updateOverrideSource()) {
            createChildren(new OverrideConfig(overrideSource.getTemplateConfig(), index));
        }
        onChange();
    }

    protected boolean updateOverrideSource() {
        String value = getText();
        overrideSource = ParameterOverrideSource.parameterSourceFromString(value);
        if (!value.isEmpty() && overrideSource instanceof ParameterOverrideSource) {
            overrideSource = new MixOverrideSource(overrideSource);
            return true;
        }
        return false;
    }

    protected void createChildren(OverrideConfig templateConfig) {
        templateConfig.setIndex(index);
        for (String name : getFilteredNames(templateConfig)) {
            List<Object> objects = previousValues.get(name);

            if (objects == null) {
                objects = templateConfig.getObjects(name);
                if (objects != null) {
                    previousValues.put(name, objects);
                }
            }

            if (objects != null) {
                ConfigValueWidget child = new ConfigValueWidget(getX(), getWidth(), 20, stack, name, objects);
                child.setChangedListener(this::onChangeChild);
                children.add(child);
                child.addToScreen(listScreen);
            }
        }
    }

    private @NotNull List<String> getFilteredNames(UniformConfig templateConfig) {
        List<String> names = new ArrayList<>(templateConfig.getNames());
        int soupValues = 0;
        String prefix = index+"_";
        for (int i = 0; i < names.size(); i++) {
            String name = names.get(i);
            if (name.startsWith(prefix)) {
                name = name.substring(prefix.length());
                names.set(i, name);
            } else {
                names.remove(i);
                i--;
                continue;
            }

            if (i == soupValues) continue;

            if (name.startsWith("soup_")) {
                names.remove(i);
                names.add(soupValues, name);
                soupValues++;
            }
        }
        return names;
    }

    protected void onChangeChild(ConfigValueWidget configValueWidget) {
        previousValues.put(configValueWidget.name, configValueWidget.objects);
        updateOverrideSource();
        onChange();
    }

    public void onChangeListener(Consumer<ConfigWidget> onChange) {
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

    @Override
    public int getCollapseHeight() {
        return getHeight()*(1+children.size());
    }

    @Override
    public void onRemove() {
        for (ConfigValueWidget child : children) {
            child.removeFromScreen(listScreen);
        }
        listScreen.removeSelectable(this);
    }

    public UniformConfig getConfig(String prefix) {
        MapConfig mapConfig = new MapConfig(List.of());
        for (ConfigValueWidget child : children) {
            mapConfig.config.put(prefix+child.name, child.objects);
        }
        return mapConfig;
    }
}
