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
import org.jetbrains.annotations.NotNull;

import java.util.*;

public record LayerCodecs(Optional<List<Shader>> shaders, Optional<List<Shader>> modifiers, Optional<List<CalculationData>> calculations) implements DeletableCodec {
    public static final Codec<LayerCodecs> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            Shader.CODEC.listOf().optionalFieldOf("shaders").forGetter(LayerCodecs::shaders),
            Shader.CODEC.listOf().optionalFieldOf("modifiers").forGetter(LayerCodecs::modifiers),
            CalculationData.CODEC.listOf().optionalFieldOf("calculations").forGetter(LayerCodecs::calculations)
    ).apply(instance, LayerCodecs::new));

    public static LayerCodecs from(ShaderLayer layer) {
        List<Shader> shaders = Shader.fromList(layer.shaders);
        List<Shader> modifiers = Shader.fromList(layer.modifiers);
        List<CalculationData> calculations = CalculationData.fromList(layer.calculations);

        return new LayerCodecs(
                shaders.isEmpty() ? Optional.empty() : Optional.of(shaders),
                modifiers.isEmpty() ? Optional.empty() : Optional.of(modifiers),
                calculations.isEmpty() ? Optional.empty() : Optional.of(calculations)
        );
    }

    public void apply(ShaderLayer layer) {
        if (shaders.isPresent()) {
            for (Shader shader : shaders.get()) {
                shader.apply(layer, Shaders.getMainRegistryId());
            }
        }
        if (modifiers.isPresent()) {
            for (Shader shader : modifiers.get()) {
                shader.apply(layer, SoupRenderer.modifierRegistry);
            }
        }
        if (calculations.isPresent()) {
            for (CalculationData calculationData : calculations.get()) {
                calculationData.apply(layer);
            }
        }
    }

    public boolean isEmpty() {
        return shaders.isEmpty() && modifiers.isEmpty() && calculations.isEmpty();
    }

    protected record Shader(String id, Optional<Map<String, List<Pass>>> passes, Boolean active) {
        public static final Codec<Shader> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
                Codec.STRING.fieldOf("id").forGetter(Shader::id),
                Codec.unboundedMap(Codec.STRING, Pass.CODEC.listOf()).optionalFieldOf("passes").forGetter(Shader::passes),
                Codec.BOOL.optionalFieldOf("active", true).forGetter(Shader::active)
        ).apply(instance, Shader::new));

        public static List<Shader> fromList(List<ShaderData> shaderDatas) {
            List<Shader> shaders = new ArrayList<>(shaderDatas.size());
            for (ShaderData shaderData : shaderDatas) {
                shaders.add(from(shaderData));
            }
            return shaders;
        }

        public static Shader from(ShaderData shader) {
            Map<String, List<Pass>> passes = new HashMap<>(shader.passDatas.size());
            shader.passDatas.forEach((identifier, passData) -> {
                List<Pass> pass = Pass.from(passData);
                if (!pass.isEmpty()) {
                    passes.put(identifier == null ? "default" : identifier.toString(), pass);
                }
            });
            return new Shader(shader.shader.getShaderId().toString(), passes.isEmpty() ? Optional.empty() : Optional.of(passes), shader.active);
        }

        public void apply(ShaderLayer layer, Identifier registry) {
            List<ShaderData> shaderDatas = SouperSecretSettingsClient.soupRenderer.getShaderAdditions(registry, Identifier.of(id), 1, layer);
            if (shaderDatas == null || shaderDatas.isEmpty()) {
                SouperSecretSettingsClient.log("couldn't find shader", id, registry);
                return;
            }
            ShaderData shaderData = shaderDatas.getFirst();
            passes.ifPresent(stringListMap -> shaderData.passDatas.forEach((identifier, passData) -> {
                List<Pass> passList = stringListMap.get(identifier == null ? "default" : identifier.toString());
                if (passList == null) {
                    return;
                }

                for (int i = 0; i < passList.size() && i < passData.overrides.size(); i++) {
                    passList.get(i).apply(passData.overrides.get(i), passData.configs.get(i));
                }
            }));
            shaderData.active = active;
            layer.getList(registry).add(shaderData);
        }
    }

    protected record Pass(Optional<Map<String,Uniform>> uniforms) {
        public static final Codec<Pass> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
                Codec.unboundedMap(Codec.STRING, Uniform.CODEC).optionalFieldOf("uniforms").forGetter(Pass::uniforms)
        ).apply(instance, Pass::new));

        public static List<Pass> from(PassData passData) {
            List<Pass> passes = new ArrayList<>(passData.overrides.size());

            for (int passIndex = 0; passIndex < passData.overrides.size(); passIndex++) {
                Map<String,Uniform> uniforms = getUniforms(passData, passIndex);
                passes.add(new Pass(uniforms.isEmpty() ? Optional.empty() : Optional.of(uniforms)));
            }

            for (int passIndex = passes.size()-1; passIndex >= 0; passIndex--) {
                Pass pass = passes.get(passIndex);
                if (pass.uniforms.isEmpty()) {
                    passes.removeLast();
                } else {
                    break;
                }
            }

            return passes;
        }

        private static @NotNull Map<String, Uniform> getUniforms(PassData passData, int passIndex) {
            Map<String, UniformData<UniformOverride>> overrides = passData.overrides.get(passIndex);
            Map<String, UniformData<UniformConfig>> configs = passData.configs.get(passIndex);

            Map<String, Uniform> uniforms = new HashMap<>();
            overrides.forEach((uniform, override) -> {
                UniformData<UniformConfig> config = configs.get(uniform);
                if (PassData.isChanged(override, config)) {
                    uniforms.put(uniform, Uniform.from((LuminanceUniformOverride) override.value, (MapConfig) config.value));
                }
            });
            return uniforms;
        }

        public void apply(Map<String, UniformData<UniformOverride>> overrides, Map<String, UniformData<UniformConfig>> configs) {
            if (uniforms.isEmpty()) {
                return;
            }

            overrides.forEach((uniform, override) -> {
                Uniform uniformData = uniforms.get().get(uniform);
                if (uniformData == null) {
                    return;
                }

                uniformData.apply((LuminanceUniformOverride)override.value, (MapConfig)configs.get(uniform).value);
            });
        }
    }

    protected record Uniform(List<String> values, Optional<Map<String, List<Object>>> config) {
        public static final Codec<Uniform> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
                Codec.STRING.listOf().fieldOf("values").forGetter(Uniform::values),
                Codec.unboundedMap(Codec.STRING, Codecs.BASIC_OBJECT.listOf()).optionalFieldOf("config").forGetter(Uniform::config)
        ).apply(instance, Uniform::new));

        public static Uniform from(LuminanceUniformOverride override, MapConfig config) {
            return new Uniform(override.getStrings(), config.config.isEmpty() ? Optional.empty() : Optional.of(config.config));
        }

        public void apply(LuminanceUniformOverride override, MapConfig config) {
            for (int i = 0; i < override.overrideSources.size() && i < values().size(); i++) {
                override.overrideSources.set(i, ParameterOverrideSource.parameterSourceFromString(values.get(i)));
            }

            this.config.ifPresent(c -> {
                config.config.clear();
                config.config.putAll(c);
            });
        }
    }

    protected record CalculationData(String id, List<String> outputs, List<String> inputs, Boolean active) {
        public static final Codec<CalculationData> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
                Codec.STRING.fieldOf("id").forGetter(CalculationData::id),
                Codec.STRING.listOf().fieldOf("outputs").forGetter(CalculationData::outputs),
                Codec.STRING.listOf().fieldOf("inputs").forGetter(CalculationData::inputs),
                Codec.BOOL.optionalFieldOf("active", true).forGetter(CalculationData::active)
        ).apply(instance, CalculationData::new));

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
            return new CalculationData(calculation.getID(), Arrays.stream(calculation.outputs).toList(), inputs, calculation.active);
        }

        public void apply(ShaderLayer layer) {
            Calculation calculation = Calculations.createCalculation(id);
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

            calculation.active = active;

            layer.calculations.add(calculation);
        }
    }
}
