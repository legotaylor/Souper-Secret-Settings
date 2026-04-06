package com.nettakrim.souper_secret_settings.shaders;

import com.mclegoman.luminance.client.shaders.UniformBlock;
import com.mclegoman.luminance.client.shaders.UniformInstance;
import com.mclegoman.luminance.client.shaders.interfaces.CustomPassData;
import com.mclegoman.luminance.client.shaders.interfaces.PostPassInterface;
import com.mclegoman.luminance.client.shaders.overrides.*;
import com.mclegoman.luminance.client.shaders.uniforms.Uniform;
import com.mclegoman.luminance.client.shaders.uniforms.UniformVector;
import com.mclegoman.luminance.client.shaders.uniforms.config.EmptyConfig;
import com.mclegoman.luminance.client.shaders.uniforms.config.MapConfig;
import com.mclegoman.luminance.client.shaders.uniforms.config.UniformConfig;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import net.minecraft.client.gl.PostEffectPass;
import net.minecraft.util.Identifier;

import java.util.*;

public class PassData {
    // TODO: these should use indices, since uniform name isnt entirely reliable
    public final ArrayList<Map<String, UniformData<UniformOverride>>> overrides;
    public final ArrayList<Map<String, UniformData<UniformConfig>>> configs;

    public BitSet expanded;
    public ArrayList<Set<String>> uniformExpanded;

    public static final Identifier overridePath = Identifier.of(SouperSecretSettingsClient.MODID, "uniform_override");
    public static final Identifier configPath = Identifier.of(SouperSecretSettingsClient.MODID, "uniform_config");

    public PassData(List<PostEffectPass> passes) {
        this.overrides = new ArrayList<>(passes.size());
        this.configs = new ArrayList<>(passes.size());
        this.expanded = new BitSet(passes.size());
        this.uniformExpanded = new ArrayList<>(passes.size());

        for (PostEffectPass pass : passes) {
            Map<String, UniformData<UniformOverride>> override = new HashMap<>(0);
            Map<String, UniformData<UniformConfig>> config = new HashMap<>(0);

            initialiseUniforms((PostPassInterface)pass);
            for (String blockName : ((PostPassInterface)pass).luminance$getUniformBlockNames()) {
                UniformBlock block = ((PostPassInterface)pass).luminance$getUniformBlock(blockName);
                for (UniformInstance uniformInstance : block.uniforms) {
                    defaultOverride(((PostPassInterface)pass), uniformInstance, override, config);
                }
            }

            this.overrides.add(override);
            this.configs.add(config);
            this.uniformExpanded.add(new HashSet<>());
        }
    }

    @SuppressWarnings({"OptionalGetWithoutIsPresent", "unchecked"})
    private void defaultOverride(PostPassInterface pass, UniformInstance uniform, Map<String, UniformData<UniformOverride>> overrideMap, Map<String, UniformData<UniformConfig>> configMap) {
        LuminanceUniformOverride defaultOverride = ((Map<String,LuminanceUniformOverride>)pass.luminance$getCustomData(overridePath).get()).get(uniform.name);
        UniformConfig defaultConfig = ((Map<String,UniformConfig>)pass.luminance$getCustomData(configPath).get()).get(uniform.name);

        LuminanceUniformOverride uniformOverride = new LuminanceUniformOverride(defaultOverride.getStrings());
        for (int i = 0; i < uniformOverride.overrideSources.size(); i++) {
            OverrideSource overrideSource = uniformOverride.overrideSources.get(i);
            if (overrideSource instanceof UniformSource uniformSource) {
                uniformOverride.overrideSources.set(i, new ParameterOverrideSource(uniformSource));
            }
        }

        MapConfig configOverride = new MapConfig(Map.of());
        configOverride.mergeWithConfig(defaultConfig);

        overrideMap.put(uniform.name, new UniformData<>(uniformOverride, defaultOverride));
        configMap.put(uniform.name, new UniformData<>(configOverride, defaultConfig));
    }

    private void initialiseUniforms(PostPassInterface pass) {
        if ((pass).luminance$getCustomData(overridePath).isPresent()) {
            return;
        }

        int count = 0;
        for (String blockName : pass.luminance$getUniformBlockNames()) {
            count += pass.luminance$getUniformBlock(blockName).uniforms.size();
        }

        CustomPassData.CustomPassDataMap<String,LuminanceUniformOverride> overrideMap = new CustomPassData.CustomPassDataMap<>(count);
        CustomPassData.CustomPassDataMap<String,UniformConfig> configMap = new CustomPassData.CustomPassDataMap<>(count);

        for (String blockName : pass.luminance$getUniformBlockNames()) {
            UniformBlock block = pass.luminance$getUniformBlock(blockName);
            for (UniformInstance uniformInstance : block.uniforms) {
                // create new config instance
                MapConfig configOverride = new MapConfig(Map.of());
                LuminanceUniformOverride defaultOverride = null;

                if (uniformInstance.override instanceof LuminanceUniformOverride luminanceUniformOverride) {
                    // TODO: should this create a new instance? it seemingly didnt before
                    defaultOverride = luminanceUniformOverride;
                }
                if (defaultOverride == null) {
                    defaultOverride = new LuminanceUniformOverride(List.of());
                    for (Number number : uniformInstance.defaultValue) {
                        defaultOverride.overrideSources.add(ParameterOverrideSource.parameterSourceFromString(number.toString()));
                    }
                }

                for (int i = 0; i < uniformInstance.length(); i++) {
                    if (i >= defaultOverride.overrideSources.size()) {
                        defaultOverride.overrideSources.add(null);
                        continue;
                    }

                    OverrideSource overrideSource = defaultOverride.overrideSources.get(i);
                    if (overrideSource instanceof UniformSource uniformSource) {
                        defaultOverride.overrideSources.set(i, new ParameterOverrideSource(uniformSource));
                        configOverride.config().putIfAbsent(i + "_range", getRange(uniformSource));
                        OverrideConfig overrideConfig = new OverrideConfig(overrideSource.getTemplateConfig(), i);
                        overrideConfig.setIndex(-1);
                        configOverride.mergeWithConfig(overrideConfig);
                    }
                }

                overrideMap.put(uniformInstance.name, defaultOverride);

                configOverride.mergeWithConfig(uniformInstance.config);
                configMap.put(uniformInstance.name, configOverride);
            }
        }

        pass.luminance$putCustomData(overridePath, overrideMap);
        pass.luminance$putCustomData(configPath, configMap);
    }

    private static List<Object> getRange(UniformSource uniformSource) {
        Uniform uniform = uniformSource.getUniform();
        if (uniform == null || uniform.rangeCanChange()) {
            return new ArrayList<>(Collections.nCopies(2, null));
        }

        float a = 0;
        float b = 1;
        Optional<UniformVector> min = uniformSource.getUniform().getMin(null, null);
        Optional<UniformVector> max = uniformSource.getUniform().getMax(null, null);
        if (min.isPresent() && max.isPresent()) {
            a = min.get().values.getFirst();
            b = max.get().values.getFirst();
        }

        return List.of(a,b);
    }

    public static boolean isChanged(UniformData<UniformOverride> override, UniformData<UniformConfig> config) {
        if (!((LuminanceUniformOverride)override.value).getStrings().equals(((LuminanceUniformOverride)override.defaultValue).getStrings())) {
            return true;
        }

        MapConfig mapConfig = (MapConfig)config.value;

        if (mapConfig.config().isEmpty() && config.defaultValue == EmptyConfig.INSTANCE) {
            return false;
        }

        if (config.defaultValue instanceof MapConfig defaultMap) {
            if (!defaultMap.config().keySet().equals(mapConfig.config().keySet())) {
                return true;
            }

            for (String s : defaultMap.config().keySet()) {
                List<Object> defaultObjects = defaultMap.config().get(s);
                List<Object> currentObjects = mapConfig.config().get(s);
                if (defaultObjects.size() != currentObjects.size()) {
                    return true;
                }

                for (int i = 0; i < defaultObjects.size(); i++) {
                    Object defaultObject = defaultObjects.get(i);
                    Object currentObject = currentObjects.get(i);

                    if (defaultObject.equals(currentObject)) {
                        continue;
                    }

                    if (defaultObject instanceof Number defaultNumber && currentObject instanceof Number currentNumber) {
                        if (defaultNumber.doubleValue() != currentNumber.doubleValue()) {
                            return true;
                        }
                    }
                }
            }

            return false;
        }

        return true;
    }
}
