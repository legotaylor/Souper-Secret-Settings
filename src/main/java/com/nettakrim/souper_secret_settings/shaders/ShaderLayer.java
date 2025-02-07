package com.nettakrim.souper_secret_settings.shaders;

import com.mclegoman.luminance.client.shaders.Shaders;
import com.mclegoman.luminance.client.shaders.interfaces.FramePassInterface;
import com.mclegoman.luminance.common.util.Couple;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.shaders.calculations.Calculation;
import net.minecraft.client.render.DefaultFramebufferSet;
import net.minecraft.client.render.FrameGraphBuilder;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ShaderLayer {
    public List<ShaderData> shaderDatas;
    public List<ShaderData> layerEffects;

    public List<Calculation> calculations;
    public Map<String, Float> parameterValues;

    public ShaderLayer() {
        shaderDatas = new ArrayList<>();
        layerEffects = new ArrayList<>();

        calculations = new ArrayList<>();
        parameterValues = new HashMap<>();
    }

    public void render(FrameGraphBuilder builder, int textureWidth, int textureHeight, DefaultFramebufferSet framebufferSet) {
        renderingLayer = this;
        parameterValues.clear();
        for (Calculation calculation : calculations) {
            calculation.update(this);
        }

        Queue<Couple<ShaderData, Identifier>> shaderQueue = new LinkedList<>();
        FramePassInterface.createForcedPass(builder, Identifier.of(SouperSecretSettingsClient.MODID, "layer"), () -> {
            renderingLayer = this;
            OverrideManager.startShaderQueue(shaderQueue);
        });

        renderList(layerEffects, shaderQueue, builder, textureWidth, textureHeight, framebufferSet, Identifier.of(SouperSecretSettingsClient.MODID, "before_layer_render"));

        for (ShaderData shaderData : shaderDatas) {
            if (shaderData.active) {
                renderList(layerEffects, shaderQueue, builder, textureWidth, textureHeight, framebufferSet, Identifier.of(SouperSecretSettingsClient.MODID, "before_shader_render"));
                renderShader(shaderData, shaderQueue, builder, textureWidth, textureHeight, framebufferSet, null);
                renderList(layerEffects.reversed(), shaderQueue, builder, textureWidth, textureHeight, framebufferSet, Identifier.of(SouperSecretSettingsClient.MODID, "after_shader_render"));
                shaderQueue.add(null);
            }
        }

        renderList(layerEffects.reversed(), shaderQueue, builder, textureWidth, textureHeight, framebufferSet, Identifier.of(SouperSecretSettingsClient.MODID, "after_layer_render"));
    }

    public void renderList(List<ShaderData> shaders, Queue<Couple<ShaderData, Identifier>> shaderQueue, FrameGraphBuilder builder, int textureWidth, int textureHeight, DefaultFramebufferSet framebufferSet, @Nullable Identifier customPasses) {
        for (ShaderData shaderData : shaders) {
            renderShader(shaderData, shaderQueue, builder, textureWidth, textureHeight, framebufferSet, customPasses);
        }
    }

    public void renderShader(ShaderData shaderData, Queue<Couple<ShaderData, Identifier>> shaderQueue, FrameGraphBuilder builder, int textureWidth, int textureHeight, DefaultFramebufferSet framebufferSet, @Nullable Identifier customPasses) {
        if (shaderData.render(builder, textureWidth, textureHeight, framebufferSet, customPasses)) {
            shaderQueue.add(new Couple<>(shaderData, customPasses));
        }
    }

    public List<ShaderData> getList(Identifier registry) {
        if (Shaders.getMainRegistryId().equals(registry)) {
            return shaderDatas;
        }

        if (SoupRenderer.layerEffectRegistry.equals(registry)) {
            return layerEffects;
        }

        return List.of();
    }

    private static ShaderLayer renderingLayer;
    public static ShaderLayer getRenderingLayer() {
        return renderingLayer;
    }
}
