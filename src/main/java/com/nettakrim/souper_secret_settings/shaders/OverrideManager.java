package com.nettakrim.souper_secret_settings.shaders;

import com.mclegoman.luminance.client.events.Runnables;
import com.mclegoman.luminance.client.shaders.interfaces.PostEffectPassInterface;
import com.mclegoman.luminance.client.shaders.overrides.UniformOverride;
import com.mclegoman.luminance.mixin.client.shaders.PostEffectProcessorAccessor;
import net.minecraft.client.gl.PostEffectPass;

import java.util.*;

public class OverrideManager {
    private static Queue<ShaderData> currentShaders;
    private static int currentPassIndex;

    private static final Map<String, UniformOverride> replacedOverrides = new HashMap<>();
    private static final List<String> nullOverrides = new ArrayList<>();

    public static void startShaderQueue(Queue<ShaderData> shaderQueue) {
        if (!shaderQueue.isEmpty()) {
            currentShaders = shaderQueue;
            currentPassIndex = -1;
        }
    }

    private static void searchFor(PostEffectPass postEffectPass) {
        if (currentShaders == null) return;

        while (!currentShaders.isEmpty()) {
            currentPassIndex++;
            List<PostEffectPass> currentPasses = ((PostEffectProcessorAccessor) currentShaders.peek().shader.getPostProcessor()).getPasses();

            while (currentPassIndex < currentPasses.size()) {
                if (currentPasses.get(currentPassIndex) == postEffectPass) {
                    return;
                }
                currentPassIndex++;
            }

            currentShaders.remove();
            currentPassIndex = -1;
        }
    }

    public static class BeforeShaderRender implements Runnables.Shader {
        @Override
        public void run(PostEffectPass postEffectPass) {
            searchFor(postEffectPass);
            if (currentShaders == null || currentShaders.isEmpty()) {
                return;
            }

            PostEffectPassInterface pass = ((PostEffectPassInterface)postEffectPass);

            currentShaders.peek().overrides.get(currentPassIndex).forEach((uniform, override) -> {
                UniformOverride previous = pass.luminance$addUniformOverride(uniform, override);
                if (previous != null) {
                    replacedOverrides.put(uniform, previous);
                } else {
                    nullOverrides.add(uniform);
                }
            });
        }
    }

    public static class AfterShaderRender implements Runnables.Shader {
        @Override
        public void run(PostEffectPass postEffectPass) {
            if (currentShaders == null || currentShaders.isEmpty()) {
                return;
            }

            PostEffectPassInterface pass = ((PostEffectPassInterface)postEffectPass);

            replacedOverrides.forEach(pass::luminance$addUniformOverride);
            replacedOverrides.clear();

            nullOverrides.forEach(pass::luminance$removeUniformOverride);
            nullOverrides.clear();
        }
    }
}
