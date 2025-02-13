package com.nettakrim.souper_secret_settings.data;

import com.mclegoman.luminance.client.shaders.Shaders;
import com.mclegoman.luminance.client.shaders.overrides.LuminanceUniformOverride;
import com.mclegoman.luminance.client.shaders.overrides.OverrideSource;
import com.mclegoman.luminance.client.shaders.overrides.UniformOverride;
import com.mclegoman.luminance.client.shaders.uniforms.config.MapConfig;
import com.mclegoman.luminance.client.shaders.uniforms.config.UniformConfig;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.shaders.*;
import com.nettakrim.souper_secret_settings.shaders.calculations.Calculation;
import com.nettakrim.souper_secret_settings.shaders.calculations.Calculations;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;

import java.util.*;

public record LayerCodecs(List<Shader> shaders, List<Shader> effects, List<CalculationData> calculations) {
    public static final Codec<LayerCodecs> CODEC = RecordCodecBuilder.create((instance) -> instance.group(Shader.CODEC.listOf().fieldOf("shaders").forGetter(LayerCodecs::shaders), Shader.CODEC.listOf().fieldOf("effects").forGetter(LayerCodecs::effects), CalculationData.CODEC.listOf().fieldOf("calculations").forGetter(LayerCodecs::calculations)).apply(instance, LayerCodecs::new));

    public static LayerCodecs from(ShaderLayer layer) {
        return new LayerCodecs(Shader.fromList(layer.shaders), Shader.fromList(layer.effects), CalculationData.fromList(layer.calculations));
    }

    public void apply(ShaderLayer layer) {
        for (Shader shader : shaders) {
            shader.apply(layer, Shaders.getMainRegistryId());
        }
        for (Shader shader : effects) {
            shader.apply(layer, SoupRenderer.effectRegistry);
        }
        for (CalculationData calculationData : calculations) {
            calculationData.apply(layer);
        }
    }

    protected record Shader(String id, Map<String, List<Pass>> passes) {
        public static final Codec<Shader> CODEC = RecordCodecBuilder.create((instance) -> instance.group(Codec.STRING.fieldOf("id").forGetter(Shader::id), Codec.unboundedMap(Codec.STRING, Pass.CODEC.listOf()).fieldOf("passes").forGetter(Shader::passes)).apply(instance, Shader::new));

        public static List<Shader> fromList(List<ShaderData> shaderDatas) {
            List<Shader> shaders = new ArrayList<>(shaderDatas.size());
            for (ShaderData shaderData : shaderDatas) {
                shaders.add(from(shaderData));
            }
            return shaders;
        }

        public static Shader from(ShaderData shader) {
            Map<String, List<Pass>> passes = new HashMap<>(shader.passDatas.size());
            shader.passDatas.forEach((identifier, passData) -> passes.put(identifier == null ? "default" : identifier.toString(), Pass.from(passData)));
            return new Shader(shader.shader.getShaderId().toString(), passes);
        }

        public void apply(ShaderLayer layer, Identifier registry) {
            List<ShaderData> shaderDatas = SouperSecretSettingsClient.soupRenderer.getShaderAdditions(registry, Identifier.of(id), 1, layer);
            if (shaderDatas == null || shaderDatas.isEmpty()) {
                SouperSecretSettingsClient.log("couldn't find shader", id, registry);
                return;
            }
            ShaderData shaderData = shaderDatas.getFirst();
            shaderData.passDatas.forEach((identifier, passData) -> {
                List<Pass> passList = passes.get(identifier == null ? "default" : identifier.toString());
                if (passList == null) {
                    SouperSecretSettingsClient.log("couldn't find pass list", identifier, "in shader", id);
                    return;
                }

                for (int i = 0; i < passList.size(); i++) {
                    passList.get(i).apply(passData.overrides.get(i), passData.configs.get(i));
                }
            });
            layer.getList(registry).add(shaderData);
        }
    }

    protected record Pass(Map<String,Uniform> uniforms) {
        public static final Codec<Pass> CODEC = RecordCodecBuilder.create((instance) -> instance.group(Codec.unboundedMap(Codec.STRING, Uniform.CODEC).fieldOf("uniforms").forGetter(Pass::uniforms)).apply(instance, Pass::new));

        public static List<Pass> from(PassData passData) {
            List<Pass> passes = new ArrayList<>(passData.overrides.size());

            for (int passIndex = 0; passIndex < passData.overrides.size(); passIndex++) {
                Map<String, UniformData<UniformOverride>> overrides = passData.overrides.get(passIndex);
                Map<String, UniformData<UniformConfig>> configs = passData.configs.get(passIndex);

                Map<String, Uniform> uniforms = new HashMap<>();
                overrides.forEach((uniform, override) -> uniforms.put(uniform, Uniform.from((LuminanceUniformOverride)override.value, (MapConfig)configs.get(uniform).value)));
                passes.add(new Pass(uniforms));
            }
            return passes;
        }

        public void apply(Map<String, UniformData<UniformOverride>> overrides, Map<String, UniformData<UniformConfig>> configs) {
            overrides.forEach((uniform, override) -> {
                Uniform uniformData = uniforms.get(uniform);
                if (uniformData == null) {
                    SouperSecretSettingsClient.log("couldn't find uniform", uniform);
                    return;
                }

                uniformData.apply((LuminanceUniformOverride)override.value, (MapConfig)configs.get(uniform).value);
            });
        }
    }

    protected record Uniform(List<String> values, Map<String, List<Object>> config) {
        public static final Codec<Uniform> CODEC = RecordCodecBuilder.create((instance) -> instance.group(Codec.STRING.listOf().fieldOf("values").forGetter(Uniform::values), Codec.unboundedMap(Codec.STRING, Codecs.BASIC_OBJECT.listOf()).fieldOf("config").forGetter(Uniform::config)).apply(instance, Uniform::new));

        public static Uniform from(LuminanceUniformOverride override, MapConfig config) {
            return new Uniform(override.getStrings(), config.config);
        }

        public void apply(LuminanceUniformOverride override, MapConfig config) {
            for (int i = 0; i < override.overrideSources.size() && i < values().size(); i++) {
                override.overrideSources.set(i, ParameterOverrideSource.parameterSourceFromString(values().get(i)));
            }
            config.config.clear();
            config.config.putAll(this.config);
        }
    }

    protected record CalculationData(String id, List<String> outputs, List<String> inputs) {
        public static final Codec<CalculationData> CODEC = RecordCodecBuilder.create((instance) -> instance.group(Codec.STRING.fieldOf("id").forGetter(CalculationData::id), Codec.STRING.listOf().fieldOf("outputs").forGetter(CalculationData::outputs), Codec.STRING.listOf().fieldOf("inputs").forGetter(CalculationData::inputs)).apply(instance, CalculationData::new));

        public static List<CalculationData> fromList(List<Calculation> calculations) {
            List<CalculationData> calculationDatas = new ArrayList<>(calculations.size());
            for (Calculation calculation : calculations) {
                calculationDatas.add(from(calculation));
            }
            return calculationDatas;
        }

        public static CalculationData from(Calculation calculation) {
            List<String> inputs = new ArrayList<>(calculation.inputs.length);
            for (OverrideSource overrideSource : calculation.inputs) {
                inputs.add(overrideSource.getString());
            }
            return new CalculationData(calculation.getID(), Arrays.stream(calculation.outputs).toList(), inputs);
        }

        public void apply(ShaderLayer layer) {
            Calculation calculation = Calculations.createCalcultion(id);
            if (calculation == null) {
                SouperSecretSettingsClient.log("couldn't find calculation", id);
                return;
            }

            for (int i = 0; i < outputs.size() && i < calculation.outputs.length; i++) {
                calculation.outputs[i] = outputs().get(i);
            }

            for (int i = 0; i < inputs.size() && i < calculation.inputs.length; i++) {
                calculation.inputs[i] = ParameterOverrideSource.parameterSourceFromString(inputs.get(i));
            }

            layer.calculations.add(calculation);
        }
    }
}
