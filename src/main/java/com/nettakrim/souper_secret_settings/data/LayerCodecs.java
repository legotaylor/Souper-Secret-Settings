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

public record LayerCodecs(Optional<List<Shader>> shaders, Optional<List<Shader>> modifiers, Optional<List<CalculationData>> parameters) implements DeletableCodec {
    public static final Codec<LayerCodecs> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            Shader.CODEC.listOf().optionalFieldOf("shaders").forGetter(LayerCodecs::shaders),
            Shader.CODEC.listOf().optionalFieldOf("modifiers").forGetter(LayerCodecs::modifiers),
            CalculationData.CODEC.listOf().optionalFieldOf("parameters").forGetter(LayerCodecs::parameters)
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
            loadingError = "load.error.shader";
            loadingIndex = 0;
            for (Shader shader : shaders.get()) {
                shader.apply(layer, Shaders.getMainRegistryId());
                loadingIndex++;
            }
        }
        if (modifiers.isPresent()) {
            loadingError = "load.error.modifier";
            loadingIndex = 0;
            for (Shader shader : modifiers.get()) {
                shader.apply(layer, SoupRenderer.modifierRegistry);
                loadingIndex++;
            }
        }
        if (parameters.isPresent()) {
            loadingError = "load.error.parameter";
            loadingIndex = 0;
            for (CalculationData calculationData : parameters.get()) {
                calculationData.apply(layer);
                loadingIndex++;
            }
        }
    }

    public boolean isEmpty() {
        return shaders.isEmpty() && modifiers.isEmpty() && parameters.isEmpty();
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
            List<ShaderData> shaderDatas = SouperSecretSettingsClient.soupRenderer.getShaderAdditions(layer, registry, Identifier.of(id), 1, -1, false);
            if (shaderDatas == null || shaderDatas.isEmpty()) {
                sayError("shader.missing", id);
                return;
            }
            ShaderData shaderData = shaderDatas.getFirst();
            passes.ifPresent(stringListMap -> stringListMap.forEach((name, passes) -> {
                PassData passData = null;
                Identifier identifier = Identifier.tryParse(name);
                if (identifier != null) {
                    passData = shaderData.passDatas.get(name.equals("default") ? null : identifier);
                }
                if (passData == null) {
                    sayError("shader.error.pass_id", name);
                    return;
                }

                for (int i = 0; i < passes.size(); i++) {
                    if (i >= passData.overrides.size()) {
                        sayError("shader.error.pass", i, passData.overrides.size()-1);
                        break;
                    }
                    passes.get(i).apply(passData.overrides.get(i), passData.configs.get(i));
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

            uniforms.get().forEach((uniform, uniformData) -> {
                UniformData<UniformOverride> override = overrides.get(uniform);
                if (override != null) {
                    uniformData.apply((LuminanceUniformOverride)override.value, (MapConfig)configs.get(uniform).value);
                } else {
                    sayError("shader.error.uniform", uniform);
                }
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
            for (int i = 0; i < values().size(); i++) {
                if (i >= override.overrideSources.size()) {
                    sayError("shader.error.value", i, override.overrideSources.size()-1);
                    break;
                }
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
                sayError("parameter.missing", id);
                return;
            }

            for (int i = 0; i < outputs.size(); i++) {
                if (i >= calculation.outputs.length) {
                    sayError("parameter.error.output", i, calculation.outputs.length-1);
                    break;
                }
                calculation.outputs[i] = outputs().get(i);
            }

            for (int i = 0; i < inputs.size(); i++) {
                if (i >= calculation.inputs.length) {
                    sayError("parameter.error.input", i, calculation.inputs.length-1);
                    break;
                }
                calculation.inputs[i] = ParameterOverrideSource.parameterSourceFromString(inputs.get(i));
            }

            calculation.active = active;

            layer.calculations.add(calculation);
        }
    }

    private static int loadingIndex;
    private static String loadingError;

    private static void sayError(String key, Object... args) {
        SouperSecretSettingsClient.sayStyled(SouperSecretSettingsClient.translate(loadingError, loadingIndex).append(SouperSecretSettingsClient.translate(key, args)), 1);
    }
}
