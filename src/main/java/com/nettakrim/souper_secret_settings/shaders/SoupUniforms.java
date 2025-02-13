package com.nettakrim.souper_secret_settings.shaders;

import com.mclegoman.luminance.client.shaders.ShaderTime;
import com.mclegoman.luminance.client.shaders.Uniforms;
import com.mclegoman.luminance.client.shaders.uniforms.UniformValue;
import com.mclegoman.luminance.client.shaders.uniforms.config.ConfigData;
import com.mclegoman.luminance.client.shaders.uniforms.config.EmptyConfig;
import com.mclegoman.luminance.client.shaders.uniforms.config.MapConfig;
import com.mclegoman.luminance.client.shaders.uniforms.config.UniformConfig;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;

import java.util.List;
import java.util.Objects;

public class SoupUniforms {
    public static void register() {
        Uniforms.registerStandardTree("soup", "byte", SoupUniforms::getBinary, 0f, 255f, 1, new MapConfig(List.of(new ConfigData("value", List.of("0")))));
        Uniforms.registerStandardTree("soup", "shader_index", SoupUniforms::getShaderIndex, 0f, null, 1, EmptyConfig.INSTANCE);
        Uniforms.registerStandardTree("soup", "layer_size", SoupUniforms::getLayerSize, 0f, null, 1, EmptyConfig.INSTANCE);
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

    public static void getLayerSize(UniformConfig config, ShaderTime shaderTime, UniformValue uniformValue) {
        ShaderLayer layer = ShaderLayer.getRenderingLayer();
        float count = 0;
        if (layer == null) {
            layer = SouperSecretSettingsClient.soupRenderer.activeLayer;
        }

        for (ShaderData shaderData : layer.shaders) {
            if (shaderData.active) {
                count++;
            }
        }
        uniformValue.values.set(0, count);
    }

    public static void getShaderIndex(UniformConfig config, ShaderTime shaderTime, UniformValue uniformValue) {
        uniformValue.values.set(0, (float)OverrideManager.currentShaderIndex);
    }
}
