package com.nettakrim.souper_secret_settings.gui;

import com.mclegoman.luminance.client.shaders.overrides.OverrideConfig;
import com.mclegoman.luminance.client.shaders.overrides.OverrideSource;
import com.mclegoman.luminance.client.shaders.uniforms.config.MapConfig;
import com.mclegoman.luminance.client.shaders.uniforms.config.UniformConfig;
import com.nettakrim.souper_secret_settings.shaders.ParameterOverrideSource;
import com.nettakrim.souper_secret_settings.shaders.ShaderLayer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;

public class ConfigWidget extends ParameterTextWidget {
    protected final List<ConfigValueWidget> children;
    protected final ListScreen<?> listScreen;
    protected final int index;
    protected final OverrideConfig defaultConfig;
    protected final Map<String, List<Object>> previousValues;

    protected Consumer<ConfigWidget> onChange;

    public OverrideSource overrideSource;


    public ConfigWidget(int x, int width, int height, Text message, ShaderLayer layer, ListScreen<?> listScreen, String initialValue, String defaultValue, UniformConfig initialConfig, UniformConfig defaultConfig, int index) {
        super(x, width, height, message, layer, defaultValue);

        children = new ArrayList<>();
        this.listScreen = listScreen;
        this.index = index;
        this.defaultConfig = new OverrideConfig(defaultConfig);

        OverrideConfig initial = new OverrideConfig(initialConfig);
        initial.setIndex(index);

        previousValues = new HashMap<>();
        for (String name : getFilteredNames(initial)) {
            previousValues.put(name, initial.getObjects(name));
        }

        setText(initialValue);
        updateOverrideSource();
        createChildren(initial);

        setChangedListener(this::setValue);
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderWidget(context, mouseX, mouseY, delta);

        if (!children.isEmpty()) {
            context.fill(getX(), getY() + getHeight(), getX() + getWidth() / 3, getY() + getHeight() + 20 * children.size(), ColorHelper.fromFloats(0.4f, 0, 0, 0));

            for (ConfigValueWidget child : children) {
                child.renderWidget(context, mouseX, mouseY, delta);
            }
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
        overrideSource = ParameterOverrideSource.parameterSourceFromString(getText());
        return overrideSource instanceof ParameterOverrideSource;
    }

    protected void createChildren(OverrideConfig templateConfig) {
        templateConfig.setIndex(index);
        defaultConfig.setIndex(index);
        for (String name : getFilteredNames(templateConfig)) {
            List<Object> objects = previousValues.get(name);
            List<Object> defaultObjects = defaultConfig.getObjects(name);

            if (objects == null) {
                if (defaultObjects != null) {
                    objects = defaultObjects;
                    previousValues.put(name, defaultObjects);
                } else {
                    objects = templateConfig.getObjects(name);
                    if (objects != null) {
                        previousValues.put(name, objects);
                    }
                }
            }

            if (objects != null) {
                if (defaultObjects == null) {
                    defaultObjects = overrideSource.getTemplateConfig().getObjects(name);
                    if (defaultObjects == null) {
                        defaultObjects = objects;
                    }
                }
                ConfigValueWidget child = new ConfigValueWidget(getX(), getWidth(), 20, name, objects, defaultObjects);
                child.setChangedListener(this::onChangeChild);
                children.add(child);
                child.addToScreen(listScreen);
            }
        }
    }

    private @NotNull List<String> getFilteredNames(UniformConfig templateConfig) {
        List<String> names = new ArrayList<>(templateConfig.getNames());
        String prefix = index+"_";
        for (int i = 0; i < names.size(); i++) {
            String name = names.get(i);
            if (name.startsWith(prefix)) {
                name = name.substring(prefix.length());
                names.set(i, name);
            } else {
                names.remove(i);
                i--;
            }
        }

        if (names.remove("range")) {
            names.addFirst("range");
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
