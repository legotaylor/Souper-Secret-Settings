package com.nettakrim.souper_secret_settings.shaders;

import com.mclegoman.luminance.client.shaders.Shaders;
import com.mclegoman.luminance.client.shaders.interfaces.FramePassInterface;
import com.mclegoman.luminance.common.util.Couple;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.shaders.calculations.Calculation;
import net.minecraft.client.render.DefaultFramebufferSet;
import net.minecraft.client.render.FrameGraphBuilder;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ShaderLayer {
    @NotNull
    public String name;

    public final List<ShaderData> shaderDatas;
    public final List<ShaderData> layerEffects;

    public final List<Calculation> calculations;
    public final Map<String, Float> parameterValues;

    public boolean active = true;
    public boolean expanded = false;

    private static final Identifier beforeLayerRender = Identifier.of(SouperSecretSettingsClient.MODID, "before_layer_render");
    private static final Identifier beforeShaderRender = Identifier.of(SouperSecretSettingsClient.MODID, "before_shader_render");
    private static final Identifier afterShaderRender = Identifier.of(SouperSecretSettingsClient.MODID, "after_shader_render");
    private static final Identifier afterLayerRender = Identifier.of(SouperSecretSettingsClient.MODID, "after_layer_render");

    private static ShaderLayer renderingLayer;

    public ShaderLayer(@NotNull String name) {
        this.name = name;

        shaderDatas = new ArrayList<>();
        layerEffects = new ArrayList<>();

        calculations = new ArrayList<>();
        parameterValues = new HashMap<>();
    }

    public void render(FrameGraphBuilder builder, int textureWidth, int textureHeight, DefaultFramebufferSet framebufferSet) {
        if (!active) {
            return;
        }

        renderingLayer = this;
        parameterValues.clear();
        for (Calculation calculation : calculations) {
            calculation.update(this);
        }

        Queue<Couple<ShaderData, Identifier>> shaderQueue = new LinkedList<>();
        FramePassInterface.createForcedPass(builder, Identifier.of(SouperSecretSettingsClient.MODID, "layer_start"), () -> {
            renderingLayer = this;
            OverrideManager.startShaderQueue(shaderQueue);
        });

        renderList(layerEffects, shaderQueue, builder, textureWidth, textureHeight, framebufferSet, beforeLayerRender);

        for (ShaderData shaderData : shaderDatas) {
            if (shaderData.active) {
                renderList(layerEffects, shaderQueue, builder, textureWidth, textureHeight, framebufferSet, beforeShaderRender);
                renderShader(shaderData, shaderQueue, builder, textureWidth, textureHeight, framebufferSet, null);
                renderList(layerEffects.reversed(), shaderQueue, builder, textureWidth, textureHeight, framebufferSet, afterShaderRender);
                shaderQueue.add(null);
            }
        }

        renderList(layerEffects.reversed(), shaderQueue, builder, textureWidth, textureHeight, framebufferSet, afterLayerRender);
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

    public Text[] getInfo() {
        int shaders = 0;
        int effects = 0;
        int passes = 0;

        for (ShaderData shaderData : shaderDatas) {
            if (!shaderData.active) continue;
            passes += shaderData.getRenderPassCount(null);
            shaders++;
        }

        for (ShaderData effect : layerEffects) {
            if (!effect.active) continue;
            passes += effect.getRenderPassCount(beforeLayerRender);
            passes += effect.getRenderPassCount(beforeShaderRender)*shaders;
            passes += effect.getRenderPassCount(afterShaderRender)*shaders;
            passes += effect.getRenderPassCount(afterLayerRender);
            effects++;
        }

        return new Text[]{
                Text.literal("shaders: "+shaders),
                Text.literal("effects: "+effects),
                Text.literal("render passes: "+passes)
        };
    }

    public static ShaderLayer getRenderingLayer() {
        return renderingLayer;
    }

    public static void renderCleanup(FrameGraphBuilder builder) {
        renderingLayer = null;
        FramePassInterface.createForcedPass(builder, Identifier.of(SouperSecretSettingsClient.MODID, "layer_cleanup"), () -> renderingLayer = null);
    }
}
