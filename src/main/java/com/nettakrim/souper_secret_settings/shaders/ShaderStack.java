package com.nettakrim.souper_secret_settings.shaders;

import com.mclegoman.luminance.client.shaders.interfaces.FramePassInterface;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.shaders.calculations.Calculation;
import net.minecraft.client.render.DefaultFramebufferSet;
import net.minecraft.client.render.FrameGraphBuilder;
import net.minecraft.util.Identifier;

import java.util.*;

public class ShaderStack {
    public ArrayList<ShaderData> shaderDatas;

    public ArrayList<Calculation> calculations;
    public Map<String, Float> parameterValues;

    public ShaderStack() {
        shaderDatas = new ArrayList<>();
        calculations = new ArrayList<>();
        parameterValues = new HashMap<>();
    }

    public void render(FrameGraphBuilder builder, int textureWidth, int textureHeight, DefaultFramebufferSet framebufferSet) {
        renderingStack = this;
        parameterValues.clear();
        for (Calculation calculation : calculations) {
            calculation.update(this);
        }

        FramePassInterface.createForcedPass(builder, Identifier.of(SouperSecretSettingsClient.MODID, "stack"), () -> {
            renderingStack = this;
            OverrideManager.startShaderQueue(new LinkedList<>(shaderDatas));
        });

        for (ShaderData shaderData : shaderDatas) {
            shaderData.render(builder, textureWidth, textureHeight, framebufferSet);
        }
    }

    public void addShaderData(ShaderData shaderData) {
        shaderDatas.add(shaderData);
    }

    public void clear() {
        shaderDatas.clear();
    }

    private static ShaderStack renderingStack;
    public static ShaderStack getRenderingStack() {
        return renderingStack;
    }
}
