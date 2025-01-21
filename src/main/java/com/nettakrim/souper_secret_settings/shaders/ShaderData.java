package com.nettakrim.souper_secret_settings.shaders;

import com.mclegoman.luminance.client.shaders.Shader;
import com.mclegoman.luminance.client.shaders.Shaders;
import com.mclegoman.luminance.client.shaders.interfaces.PostEffectPassInterface;
import com.mclegoman.luminance.client.shaders.interfaces.ShaderProgramInterface;
import com.mclegoman.luminance.client.shaders.overrides.*;
import com.mclegoman.luminance.client.shaders.uniforms.UniformValue;
import com.mclegoman.luminance.client.shaders.uniforms.config.ConfigData;
import com.mclegoman.luminance.client.shaders.uniforms.config.EmptyConfig;
import com.mclegoman.luminance.client.shaders.uniforms.config.MapConfig;
import com.mclegoman.luminance.client.shaders.uniforms.config.UniformConfig;
import com.mclegoman.luminance.mixin.client.shaders.PostEffectProcessorAccessor;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.PostEffectPass;
import net.minecraft.client.gl.PostEffectPipeline;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.DefaultFramebufferSet;
import net.minecraft.client.render.FrameGraphBuilder;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ShaderData {
    public Shader shader;

    public final ArrayList<Map<String, UniformData<UniformOverride>>> overrides;
    public final ArrayList<Map<String, UniformData<UniformConfig>>> configs;

    public boolean active = true;

    public static final Identifier overridePath = Identifier.of(SouperSecretSettingsClient.MODID, "uniform_override");
    public static final Identifier configPath = Identifier.of(SouperSecretSettingsClient.MODID, "uniform_config");

    public ShaderData(Shader shader) {
        this.shader = shader;
        this.shader.setPostProcessor();

        List<PostEffectPass> passes = ((PostEffectProcessorAccessor)this.shader.getPostProcessor()).getPasses();
        this.overrides = new ArrayList<>(passes.size());
        this.configs = new ArrayList<>(passes.size());

        for (PostEffectPass pass : passes) {
            Map<String, UniformData<UniformOverride>> override = new HashMap<>(0);
            Map<String, UniformData<UniformConfig>> config = new HashMap<>(0);

            ShaderProgram program = pass.getProgram();
            List<String> names = ((ShaderProgramInterface)program).luminance$getUniformNames();
            initialise((PostEffectPassInterface)pass, names, program);
            for (String name : names) {
                defaultOverride(pass, program.getUniform(name), override, config);
            }

            this.overrides.add(override);
            this.configs.add(config);
        }
    }

    @SuppressWarnings({"OptionalGetWithoutIsPresent", "unchecked"})
    private void defaultOverride(PostEffectPass pass, @Nullable GlUniform uniform, Map<String, UniformData<UniformOverride>> overrideMap, Map<String, UniformData<UniformConfig>> configMap) {
        assert uniform != null;
        if (!allowUniform(uniform.getName())) {
            return;
        }

        LuminanceUniformOverride defaultOverride = ((Map<String,LuminanceUniformOverride>)((PostEffectPassInterface)pass).luminance$getCustomData(overridePath).get()).get(uniform.getName());
        UniformConfig defaultConfig = ((Map<String,UniformConfig>)((PostEffectPassInterface)pass).luminance$getCustomData(configPath).get()).get(uniform.getName());

        LuminanceUniformOverride uniformOverride = new LuminanceUniformOverride(defaultOverride.getStrings());

        MapConfig configOverride = new MapConfig(List.of());
        configOverride.mergeWithConfig(defaultConfig);

        overrideMap.put(uniform.getName(), new UniformData<>(uniformOverride, defaultOverride));
        configMap.put(uniform.getName(), new UniformData<>(configOverride, defaultConfig));
    }

    private void initialise(PostEffectPassInterface pass, List<String> names, ShaderProgram program) {
        if ((pass).luminance$getCustomData(overridePath).isPresent()) {
            return;
        }

        Map<String,LuminanceUniformOverride> overrideMap = new HashMap<>(names.size());
        Map<String,UniformConfig> configMap = new HashMap<>(names.size());

        for (String name : names) {
            setDefaults(pass, program.getUniform(name), overrideMap, configMap);
        }

        pass.luminance$putCustomData(overridePath, overrideMap);
        pass.luminance$putCustomData(configPath, configMap);
    }

    private void setDefaults(PostEffectPassInterface pass, @Nullable GlUniform uniform, Map<String,LuminanceUniformOverride> overrideMap, Map<String,UniformConfig> configMap) {
        assert uniform != null;
        if (!allowUniform(uniform.getName())) {
            return;
        }

        String[] defaults = new String[uniform.getCount()];

        List<Float> baseValues = getBaseValues(pass, uniform.getName());
        for (int i = 0; i < defaults.length; i++) {
            defaults[i] = Float.toString(baseValues.get(i));
        }

        LuminanceUniformOverride defaultOverride = null;
        if (pass.luminance$getUniformOverride(uniform.getName()) instanceof LuminanceUniformOverride luminanceUniformOverride) {
            defaultOverride = luminanceUniformOverride;
        }
        if (defaultOverride == null) {
            defaultOverride = LuminanceUniformOverride.overrideFromUniform(uniform.getName());
        }
        if (defaultOverride == null) {
            defaultOverride = new LuminanceUniformOverride(List.of());
            for (String s : defaults) {
                defaultOverride.overrideSources.add(ParameterOverrideSource.parameterSourceFromString(s));
            }
        }

        MapConfig configOverride = new MapConfig(List.of());
        configOverride.mergeWithConfig(pass.luminance$getUniformConfigs().get(uniform.getName()));

        for (int i = 0; i < uniform.getCount(); i++) {
            if (i >= defaultOverride.overrideSources.size()) {
                defaultOverride.overrideSources.add(null);
                continue;
            }

            OverrideSource overrideSource = defaultOverride.overrideSources.get(i);
            if (overrideSource instanceof UniformSource uniformSource) {
                defaultOverride.overrideSources.set(i, new MixOverrideSource(new ParameterOverrideSource(uniformSource)));
                configOverride.mergeWithConfig(getMapConfig(uniformSource, i));
                OverrideConfig overrideConfig = new OverrideConfig(overrideSource.getTemplateConfig(), i);
                overrideConfig.setIndex(-1);
                configOverride.mergeWithConfig(overrideConfig);
            }
        }

        overrideMap.put(uniform.getName(), defaultOverride);
        configMap.put(uniform.getName(), configOverride.config.isEmpty() ? EmptyConfig.INSTANCE : configOverride);
    }

    public static List<Float> getBaseValues(PostEffectPassInterface pass, String uniform) {
        List<Float> values = null;
        for (PostEffectPipeline.Uniform u : pass.luminance$getUniforms()) {
            if (u.name().equals(uniform)) {
                values = u.values();
                break;
            }
        }

        List<Float> definitionValues = Objects.requireNonNull(((PostEffectPass)pass).getProgram().getUniformDefinition(uniform)).values();
        if (values == null) {
            return definitionValues;
        }

        if (values.size() < definitionValues.size()) {
            List<Float> combined = new ArrayList<>(values);
            for (int i = values.size(); i < definitionValues.size(); i++) {
                combined.add(definitionValues.get(i));
            }
            return combined;
        }

        return values;
    }

    private static @NotNull MapConfig getMapConfig(UniformSource uniformSource, int i) {
        float a = 0;
        float b = 1;
        Optional<UniformValue> min = uniformSource.getUniform().getMin();
        Optional<UniformValue> max = uniformSource.getUniform().getMax();
        if (min.isPresent() && max.isPresent()) {
            a = min.get().values.getFirst();
            b = max.get().values.getFirst();
        }

        return new MapConfig(List.of(new ConfigData(i +"_soup_range", List.of(a,b))));
    }

    public void render(FrameGraphBuilder builder, int textureWidth, int textureHeight, DefaultFramebufferSet framebufferSet) {
        if (!active) return;

        Shaders.renderUsingFramebufferSet(shader.getPostProcessor(), builder, textureWidth, textureHeight, framebufferSet);
    }

    public static final List<String> uniformsToIgnore = List.of("ProjMat", "InSize", "OutSize");
    public static boolean allowUniform(String uniform) {
        return !uniformsToIgnore.contains(uniform);
    }
}
