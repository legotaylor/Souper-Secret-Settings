package com.nettakrim.souper_secret_settings.shaders;

import com.mclegoman.luminance.client.events.Runnables;
import com.mclegoman.luminance.client.shaders.interfaces.PostEffectPassInterface;
import com.mclegoman.luminance.client.shaders.interfaces.PostEffectProcessorInterface;
import com.mclegoman.luminance.client.shaders.overrides.UniformOverride;
import com.mclegoman.luminance.client.shaders.uniforms.config.UniformConfig;
import com.mclegoman.luminance.common.util.Couple;
import net.minecraft.client.gl.PostEffectPass;
import net.minecraft.util.Identifier;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class OverrideManager {
    private static Queue<Couple<ShaderData, Identifier>> currentShaders;
    private static int currentPassIndex;

    public static int currentShaderIndex;

    private static final Replacement<UniformOverride> overrideReplacement = new Replacement<>();
    private static final Replacement<UniformConfig> configReplacement = new Replacement<>();

    public static void startShaderQueue(Queue<Couple<ShaderData, Identifier>> shaderQueue) {
        if (!shaderQueue.isEmpty()) {
            currentShaders = shaderQueue;
            currentPassIndex = -1;
            currentShaderIndex = 0;
        }
    }

    private static void searchFor(PostEffectPass postEffectPass) {
        if (currentShaders == null) return;

        while (!currentShaders.isEmpty()) {
            currentPassIndex++;
            Couple<ShaderData, Identifier> shaderData = currentShaders.peek();
            if (shaderData == null) {
                currentShaders.remove();
                continue;
            }

            if (shaderData.getFirst().active) {
                List<PostEffectPass> currentPasses = ((PostEffectProcessorInterface) shaderData.getFirst().shader.getPostProcessor()).luminance$getPasses(shaderData.getSecond());
                // render queue is only added to when the passes *do* exist
                assert currentPasses != null;

                while (currentPassIndex < currentPasses.size()) {
                    if (currentPasses.get(currentPassIndex) == postEffectPass) {
                        return;
                    }
                    currentPassIndex++;
                }
            }

            currentShaders.remove();
            currentPassIndex = -1;

            if (!currentShaders.isEmpty()) {
                shaderData = currentShaders.peek();
                if (shaderData == null) {
                    currentShaderIndex++;
                    currentShaders.remove();
                }
            }
        }
    }

    public static class BeforeShaderRender implements Runnables.Shader {
        @Override
        public void run(PostEffectPass postEffectPass) {
            searchFor(postEffectPass);
            if (currentShaders == null || currentShaders.isEmpty()) {
                return;
            }

            assert currentShaders.peek() != null;
            Couple<ShaderData, Identifier> shaderData = currentShaders.peek();

            PostEffectPassInterface pass = ((PostEffectPassInterface)postEffectPass);
            Map<String, UniformConfig> configs = pass.luminance$getUniformConfigs();

            overrideReplacement.replace(shaderData.getFirst().getPassData(shaderData.getSecond()).overrides.get(currentPassIndex), pass::luminance$addUniformOverride);
            configReplacement.replace(shaderData.getFirst().getPassData(shaderData.getSecond()).configs.get(currentPassIndex), configs::put);
        }
    }

    public static class AfterShaderRender implements Runnables.Shader {
        @Override
        public void run(PostEffectPass postEffectPass) {
            if (currentShaders == null || currentShaders.isEmpty()) {
                return;
            }

            PostEffectPassInterface pass = ((PostEffectPassInterface)postEffectPass);
            Map<String, UniformConfig> configs = pass.luminance$getUniformConfigs();

            overrideReplacement.reset(pass::luminance$addUniformOverride, pass::luminance$removeUniformOverride);
            configReplacement.reset(configs::put, configs::remove);
        }
    }

    private static class Replacement<T> {
        Map<String, T> replacedValues = new HashMap<>();
        List<String> nullValues = new ArrayList<>();

        public void replace(Map<String, UniformData<T>> replacement, BiFunction<String, T, T> replaceFunction) {
            replacement.forEach((key, value) -> {
                T previous = replaceFunction.apply(key, value.value);
                if (previous == null) {
                    nullValues.add(key);
                } else {
                    replacedValues.put(key, previous);
                }
            });
        }

        public void reset(BiConsumer<String, T> addFunction, Consumer<String> removeFunction) {
            replacedValues.forEach(addFunction);
            replacedValues.clear();

            nullValues.forEach(removeFunction);
            nullValues.clear();
        }
    }
}
