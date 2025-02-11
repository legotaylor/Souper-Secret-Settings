package com.nettakrim.souper_secret_settings.gui.shaders;

import com.mclegoman.luminance.client.shaders.Uniforms;
import com.mclegoman.luminance.client.shaders.interfaces.PostEffectPassInterface;
import com.mclegoman.luminance.client.shaders.overrides.LuminanceUniformOverride;
import com.mclegoman.luminance.client.shaders.overrides.UniformOverride;
import com.mclegoman.luminance.client.shaders.uniforms.config.MapConfig;
import com.mclegoman.luminance.client.shaders.uniforms.config.UniformConfig;
import com.mclegoman.luminance.common.util.Couple;
import com.nettakrim.souper_secret_settings.gui.ConfigWidget;
import com.nettakrim.souper_secret_settings.gui.ListScreen;
import com.nettakrim.souper_secret_settings.gui.DisplayWidget;
import com.nettakrim.souper_secret_settings.shaders.PassData;
import com.nettakrim.souper_secret_settings.shaders.UniformData;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

import java.util.*;

public class UniformWidget extends DisplayWidget<Couple<UniformData<String>,UniformData<UniformConfig>>> {
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
    protected List<Couple<UniformData<String>,UniformData<UniformConfig>>> getChildData() {
        List<Couple<UniformData<String>,UniformData<UniformConfig>>> list = new ArrayList<>();

        UniformData<UniformOverride> uniformOverride = pass.shader.shaderData.getPassData(pass.customPass).overrides.get(pass.passIndex).get(uniform.getName());
        UniformData<UniformConfig> uniformConfig = pass.shader.shaderData.getPassData(pass.customPass).configs.get(pass.passIndex).get(uniform.getName());

        List<String> valueStrings = ((LuminanceUniformOverride)uniformOverride.value).getStrings();
        List<String> defaultStrings = ((LuminanceUniformOverride)uniformOverride.defaultValue).getStrings();

        for (int i = 0; i < uniform.getCount(); i++) {
            list.add(new Couple<>(new UniformData<>(valueStrings.get(i), defaultStrings.get(i)), uniformConfig));
        }

        override = (LuminanceUniformOverride)uniformOverride.value;
        return list;
    }

    @Override
    protected ClickableWidget createChildWidget(Couple<UniformData<String>,UniformData<UniformConfig>> data, int i) {
        UniformData<String> uniformOverride = data.getFirst();
        UniformData<UniformConfig> uniformConfig = data.getSecond();

        ConfigWidget configWidget = new ConfigWidget(getX(), getWidth(), 20, Text.literal(""), pass.shader.layer, listScreen, uniformOverride.value, uniformOverride.defaultValue, uniformConfig.value, uniformConfig.defaultValue, i);
        configWidget.onChangeListener((w) -> onValueChanged(i, w));
        return configWidget;
    }

    protected void onValueChanged(int i, ConfigWidget widget) {
        override.overrideSources.set(i, widget.overrideSource);

        UniformData<UniformOverride> uniforms = pass.shader.shaderData.getPassData(pass.customPass).overrides.get(pass.passIndex).get(uniform.getName());
        uniforms.value = override;

        UniformData<UniformConfig> configs = pass.shader.shaderData.getPassData(pass.customPass).configs.get(pass.passIndex).get(uniform.getName());
        String prefix = i+"_";
        if (configs.value instanceof MapConfig mapConfig) {
            mapConfig.config.keySet().removeIf((s) -> s.startsWith(prefix));
            mapConfig.mergeWithConfig(widget.getConfig(prefix));
        } else {
            configs.value = widget.getConfig(prefix);
        }

        widget.dragValue = null;

        listScreen.updateSpacing();
    }

    @Override
    protected List<Float> getDisplayFloats() {
        List<Float> display = override.getOverride(pass.shader.shaderData.getPassData(pass.customPass).configs.get(pass.passIndex).get(uniform.getName()).value, Uniforms.shaderTime);
        List<Float> base = PassData.getDefaultValues((PostEffectPassInterface)pass.postEffectPass, uniform.getName(), Uniforms.shaderTime);
        for (int i = 0; i < display.size(); i++) {
            if (display.get(i) == null) {
                display.set(i, base.get(i));
            }

            //allow dragging luminance_alpha_smooth to replace it with its current value
            if (expanded) {
                ConfigWidget configWidget = ((ConfigWidget)children.get(i));
                if (configWidget.getText().equals("luminance_alpha_smooth")) {
                    configWidget.dragValue = display.get(i);
                }
            }
        }
        return display;
    }

    @Override
    protected boolean getStoredExpanded() {
        return pass.shader.shaderData.getPassData(pass.customPass).uniformExpanded.get(pass.passIndex).contains(uniform.getName());
    }

    @Override
    protected void setStoredExpanded(boolean to) {
        Set<String> expanded = pass.shader.shaderData.getPassData(pass.customPass).uniformExpanded.get(pass.passIndex);
        if (to) {
            expanded.add(uniform.getName());
        } else {
            expanded.remove(uniform.getName());
        }
    }
}
