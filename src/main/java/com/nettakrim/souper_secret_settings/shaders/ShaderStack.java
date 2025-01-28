package com.nettakrim.souper_secret_settings.shaders;

import com.mclegoman.luminance.client.shaders.interfaces.FramePassInterface;
import com.mclegoman.luminance.common.util.Couple;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.shaders.calculations.Calculation;
import net.minecraft.client.render.DefaultFramebufferSet;
import net.minecraft.client.render.FrameGraphBuilder;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ShaderStack {
    public List<ShaderData> shaderDatas;
    public List<ShaderData> layerEffects;

    public List<Calculation> calculations;
    public Map<String, Float> parameterValues;

    public ShaderStack() {
        shaderDatas = new ArrayList<>();
        layerEffects = new ArrayList<>();

        calculations = new ArrayList<>();
        parameterValues = new HashMap<>();
    }

    public void render(FrameGraphBuilder builder, int textureWidth, int textureHeight, DefaultFramebufferSet framebufferSet) {
        renderingStack = this;
        parameterValues.clear();
        for (Calculation calculation : calculations) {
            calculation.update(this);
        }

        Queue<Couple<ShaderData, Identifier>> shaderQueue = new LinkedList<>();
        FramePassInterface.createForcedPass(builder, Identifier.of(SouperSecretSettingsClient.MODID, "stack"), () -> {
            renderingStack = this;
            OverrideManager.startShaderQueue(shaderQueue);
        });

        renderList(layerEffects, shaderQueue, builder, textureWidth, textureHeight, framebufferSet, Identifier.of(SouperSecretSettingsClient.MODID, "before_stack_render"));

        for (ShaderData shaderData : shaderDatas) {
            if (shaderData.active) {
                renderList(layerEffects, shaderQueue, builder, textureWidth, textureHeight, framebufferSet, Identifier.of(SouperSecretSettingsClient.MODID, "before_shader_render"));
                renderShader(shaderData, shaderQueue, builder, textureWidth, textureHeight, framebufferSet, null);
                renderList(layerEffects, shaderQueue, builder, textureWidth, textureHeight, framebufferSet, Identifier.of(SouperSecretSettingsClient.MODID, "after_shader_render"));
            }
        }

        renderList(layerEffects, shaderQueue, builder, textureWidth, textureHeight, framebufferSet, Identifier.of(SouperSecretSettingsClient.MODID, "after_stack_render"));
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

    public void addShaderData(ShaderData shaderData) {
        //TODO: this, properly
        if (Objects.equals(shaderData.shader.getShaderId().getNamespace(), SouperSecretSettingsClient.MODID)) {
            layerEffects.add(shaderData);
        } else {
            shaderDatas.add(shaderData);
        }
    }

    public void clear() {
        shaderDatas.clear();
    }

    private static ShaderStack renderingStack;
    public static ShaderStack getRenderingStack() {
        return renderingStack;
    }
}
