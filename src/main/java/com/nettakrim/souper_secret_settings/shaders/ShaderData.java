package com.nettakrim.souper_secret_settings.shaders;

import com.mclegoman.luminance.client.shaders.Shader;
import com.mclegoman.luminance.client.shaders.Shaders;
import com.mclegoman.luminance.client.shaders.interfaces.PostEffectPassInterface;
import com.mclegoman.luminance.client.shaders.interfaces.ShaderProgramInterface;
import com.mclegoman.luminance.client.shaders.overrides.LuminanceUniformOverride;
import com.mclegoman.luminance.client.shaders.overrides.OverrideSource;
import com.mclegoman.luminance.client.shaders.overrides.UniformOverride;
import com.mclegoman.luminance.client.shaders.overrides.UniformSource;
import com.mclegoman.luminance.client.shaders.uniforms.Uniform;
import com.mclegoman.luminance.client.shaders.uniforms.UniformValue;
import com.mclegoman.luminance.client.shaders.uniforms.config.ConfigData;
import com.mclegoman.luminance.client.shaders.uniforms.config.MapConfig;
import com.mclegoman.luminance.client.shaders.uniforms.config.UniformConfig;
import com.mclegoman.luminance.mixin.client.shaders.PostEffectProcessorAccessor;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.PostEffectPass;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.DefaultFramebufferSet;
import net.minecraft.client.render.FrameGraphBuilder;

import java.util.*;

public class ShaderData {
    public Shader shader;

    public final List<Map<String, UniformOverride>> overrides;
    public final List<Map<String, UniformConfig>> configs;

    public boolean active = true;

    public ShaderData(Shader shader) {
        this.shader = shader;
        this.shader.setPostProcessor();

        List<PostEffectPass> passes = ((PostEffectProcessorAccessor)this.shader.getPostProcessor()).getPasses();
        this.overrides = new ArrayList<>(passes.size());
        this.configs = new ArrayList<>(passes.size());

        for (PostEffectPass pass : passes) {
            Map<String, UniformOverride> override = new HashMap<>(0);
            Map<String, UniformConfig> config = new HashMap<>(0);

            ShaderProgram program = ((PostEffectPassInterface)pass).luminance$getProgram();
            for (String name : ((ShaderProgramInterface)program).luminance$getUniformNames()) {
                defaultOverride(pass, program.getUniform(name), name, override, config);
            }

            this.overrides.add(override);
            this.configs.add(config);
        }
    }

    private void defaultOverride(PostEffectPass pass, GlUniform uniform, String name, Map<String, UniformOverride> overrideMap, Map<String, UniformConfig> configMap) {
        if (uniform == null || uniform.getCount() != 1 || !allowUniform(name)) {
            return;
        }

        Uniform override = null;
        if (((PostEffectPassInterface)pass).luminance$getUniformOverride(name) instanceof LuminanceUniformOverride o && o.overrideSources.getFirst() instanceof UniformSource uniformSource) {
            override = uniformSource.getUniform();
        }
        if (override == null && LuminanceUniformOverride.sourceFromString(name) instanceof UniformSource uniformSource) {
            override = uniformSource.getUniform();
        }
        if (override == null) return;

        List<String> list = new ArrayList<>();
        list.add(null);
        LuminanceUniformOverride uniformOverride = new LuminanceUniformOverride(list);
        OverrideSource overrideSource = new MixOverrideSource(ParameterOverrideSource.parameterSourceFromString(name));
        uniformOverride.overrideSources.set(0, overrideSource);

        overrideMap.put(name, uniformOverride);

        float a = 0;
        float b = 1;
        Optional<UniformValue> min = override.getMin();
        Optional<UniformValue> max = override.getMax();
        if (min.isPresent() && max.isPresent()) {
            a = min.get().values.getFirst();
            b = max.get().values.getFirst();
        }

        MapConfig configOverride = new MapConfig(List.of(new ConfigData("soup_range", List.of(a,b))));
        mergeConfig(configOverride, ((PostEffectPassInterface)pass).luminance$getUniformConfigs().get(name));
        mergeConfig(configOverride, overrideSource.getTemplateConfig());
        configMap.put(name, configOverride);
    }

    private void mergeConfig(MapConfig mapConfig, UniformConfig newConfig) {
        if (newConfig == null) return;

        for (String s : newConfig.getNames()) {
            List<Object> objects = newConfig.getObjects(s);
            if (objects != null) {
                mapConfig.config.putIfAbsent(s, objects);
            }
        }
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
