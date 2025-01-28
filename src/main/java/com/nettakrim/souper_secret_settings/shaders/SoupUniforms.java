package com.nettakrim.souper_secret_settings.shaders;

import com.mclegoman.luminance.client.shaders.ShaderTime;
import com.mclegoman.luminance.client.shaders.Uniforms;
import com.mclegoman.luminance.client.shaders.uniforms.UniformValue;
import com.mclegoman.luminance.client.shaders.uniforms.config.ConfigData;
import com.mclegoman.luminance.client.shaders.uniforms.config.EmptyConfig;
import com.mclegoman.luminance.client.shaders.uniforms.config.MapConfig;
import com.mclegoman.luminance.client.shaders.uniforms.config.UniformConfig;

import java.util.List;
import java.util.Objects;

public class SoupUniforms {
    public static void register() {
        Uniforms.registerStandardTree("soup", "byte", SoupUniforms::getBinary, 0f, 255f, 1, new MapConfig(List.of(new ConfigData("value", List.of("0")))));
        Uniforms.registerStandardTree("soup", "shader_index", SoupUniforms::getShaderIndex, 0f, null, 1, EmptyConfig.INSTANCE);
        Uniforms.registerStandardTree("soup", "stack_size", SoupUniforms::getStackSize, 0f, null, 1, EmptyConfig.INSTANCE);
    }

    public static void getBinary(UniformConfig config, ShaderTime shaderTime, UniformValue uniformValue) {
        String s = (String)Objects.requireNonNull(config.getObjects("value")).getFirst();
        int v = 0;
        if (!s.isEmpty()) {
            for (int i = 0; i < s.length(); i++) {
                v *= 2;
                if (s.charAt(i) == '1') {
                    v += 1;
                }
            }
        }
        uniformValue.values.set(0, (float)v);
    }

    public static void getStackSize(UniformConfig config, ShaderTime shaderTime, UniformValue uniformValue) {
        ShaderStack stack = ShaderStack.getRenderingStack();
        float count = 0;
        if (stack != null) {
            for (ShaderData shaderData : stack.shaderDatas) {
                if (shaderData.active) {
                    count++;
                }
            }
        }
        uniformValue.values.set(0, count);
    }

    public static void getShaderIndex(UniformConfig config, ShaderTime shaderTime, UniformValue uniformValue) {
        uniformValue.values.set(0, (float)OverrideManager.getCurrentShaderIndex());
    }
}
