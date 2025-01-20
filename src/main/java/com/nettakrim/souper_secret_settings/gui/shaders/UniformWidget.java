package com.nettakrim.souper_secret_settings.gui.shaders;

import com.mclegoman.luminance.client.shaders.Uniforms;
import com.mclegoman.luminance.client.shaders.interfaces.PostEffectPassInterface;
import com.mclegoman.luminance.client.shaders.overrides.LuminanceUniformOverride;
import com.mclegoman.luminance.client.shaders.overrides.UniformOverride;
import com.mclegoman.luminance.client.shaders.overrides.UniformSource;
import com.mclegoman.luminance.client.shaders.uniforms.config.EmptyConfig;
import com.mclegoman.luminance.client.shaders.uniforms.config.MapConfig;
import com.mclegoman.luminance.client.shaders.uniforms.config.UniformConfig;
import com.mclegoman.luminance.common.util.Couple;
import com.nettakrim.souper_secret_settings.gui.ConfigWidget;
import com.nettakrim.souper_secret_settings.gui.ListScreen;
import com.nettakrim.souper_secret_settings.gui.DisplayWidget;
import com.nettakrim.souper_secret_settings.shaders.ShaderData;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.PostEffectPipeline;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class UniformWidget extends DisplayWidget<Couple<String,String>> {
    public PassWidget pass;

    public GlUniform uniform;

    public LuminanceUniformOverride override;

    public UniformWidget(PassWidget pass, GlUniform uniform, Text name, int x, int width, ListScreen<?> listScreen) {
        super(uniform.getCount(), name, x, width, listScreen);
        this.pass = pass;
        this.uniform = uniform;
        initValues();
    }

    @Override
    protected List<Couple<String,String>> getChildData() {
        String[] values = new String[uniform.getCount()];

        List<Float> baseValues = getBaseValues();
        for (int i = 0; i < values.length; i++) {
            values[i] = Float.toString(baseValues.get(i));
        }

        String[] defaults = values.clone();

        if (defaults.length == 1) {
            if (((PostEffectPassInterface)pass.postEffectPass).luminance$getUniformOverride(uniform.getName()) instanceof LuminanceUniformOverride o && o.overrideSources.getFirst() instanceof UniformSource uniformSource && uniformSource.getUniform() != null) {
                defaults[0] = uniformSource.getString();
            } else if (LuminanceUniformOverride.sourceFromString(uniform.getName()) instanceof UniformSource uniformSource && uniformSource.getUniform() != null) {
                defaults[0] = uniformSource.getString();
            }
        }

        UniformOverride uniformOverride = pass.shader.shaderData.overrides.get(pass.passIndex).get(uniform.getName());
        if (uniformOverride == null) {
            uniformOverride = ((PostEffectPassInterface)pass.postEffectPass).luminance$getUniformOverride(uniform.getName());
        }

        if (uniformOverride instanceof LuminanceUniformOverride luminanceUniformOverride) {
            for (int i = 0; i < values.length; i++) {
                String s = luminanceUniformOverride.getStrings().get(i);
                if (s != null) {
                    values[i] = s;
                }
            }
            override = luminanceUniformOverride;
        } else {
            List<String> overrideStrings = new ArrayList<>(values.length);
            for (int i = 0; i < values.length; i++) {
                overrideStrings.add(null);
            }
            override = new LuminanceUniformOverride(overrideStrings);
        }

        List<Couple<String, String>> data = new ArrayList<>(values.length);
        for (int i = 0; i < values.length; i++) {
            data.add(new Couple<>(values[i], defaults[i]));
        }
        return data;
    }

    protected List<Float> getBaseValues() {
        for (PostEffectPipeline.Uniform u : ((PostEffectPassInterface)pass.postEffectPass).luminance$getUniforms()) {
            if (u.name().equals(uniform.getName())) {
                return u.values();
            }
        }
        return Objects.requireNonNull(pass.postEffectPass.getProgram().getUniformDefinition(uniform.getName())).values();
    }

    @Override
    protected ClickableWidget createChildWidget(Couple<String,String> data, int i) {
        //TODO: make default values correct for config values, instead of whatever they were at the time the gui is opened
        UniformConfig uniformConfig = pass.shader.shaderData.configs.get(pass.passIndex).get(uniform.getName());
        if (uniformConfig == null) {
            uniformConfig = EmptyConfig.INSTANCE;
        }

        ConfigWidget configWidget = new ConfigWidget(getX(), getWidth(), 20, Text.literal(""), pass.shader.stack, data.getSecond(), listScreen, data.getFirst(), uniformConfig, i);
        configWidget.onChangeListener((w) -> onValueChanged(i, w));
        return configWidget;
    }

    protected void onValueChanged(int i, ConfigWidget widget) {
        override.overrideSources.set(i, widget.overrideSource);
        pass.shader.shaderData.overrides.get(pass.passIndex).put(uniform.getName(), override);

        String prefix = i+"_";
        Map<String, UniformConfig> configs = pass.shader.shaderData.configs.get(pass.passIndex);
        MapConfig mapConfig = (MapConfig)configs.get(uniform.getName());
        if (mapConfig == null) {
            configs.put(uniform.getName(), widget.getConfig(prefix));
        } else {
            mapConfig.config.keySet().removeIf((s) -> s.startsWith(prefix));
            ShaderData.mergeConfig(mapConfig, widget.getConfig(prefix));
        }

        listScreen.updateSpacing();
    }

    @Override
    protected List<Float> getDisplayFloats() {
        List<Float> display = override.getOverride(EmptyConfig.INSTANCE, Uniforms.shaderTime);
        List<Float> base = getBaseValues();
        for (int i = 0; i < display.size(); i++) {
            if (display.get(i) == null) {
                display.set(i, base.get(i));
            }
        }
        return display;
    }
}
