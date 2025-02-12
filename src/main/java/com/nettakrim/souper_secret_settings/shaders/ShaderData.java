package com.nettakrim.souper_secret_settings.shaders;

import com.mclegoman.luminance.client.shaders.Shader;
import com.mclegoman.luminance.client.shaders.Shaders;
import com.mclegoman.luminance.client.shaders.interfaces.PostEffectProcessorInterface;
import net.minecraft.client.gl.PostEffectPass;
import net.minecraft.client.render.DefaultFramebufferSet;
import net.minecraft.client.render.FrameGraphBuilder;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ShaderData {
    public Shader shader;

    public Map<Identifier, PassData> passDatas;

    public boolean active = true;
    public boolean expanded = false;

    public ShaderData(Shader shader) {
        this.shader = shader;
        if (this.shader.getPostProcessor() == null) {
            this.shader.setPostProcessor();
        }

        PostEffectProcessorInterface processor = (PostEffectProcessorInterface)this.shader.getPostProcessor();
        Set<Identifier> customPasses = processor.luminance$getCustomPassNames();

        List<PostEffectPass> defaultPasses = processor.luminance$getPasses(null);

        if (defaultPasses.isEmpty()) {
            passDatas = new HashMap<>(customPasses.size());
        } else {
            passDatas = new HashMap<>(customPasses.size() + 1);
            passDatas.put(null, new PassData(defaultPasses));
        }

        for (Identifier customPass : customPasses) {
            List<PostEffectPass> passes = processor.luminance$getPasses(customPass);
            assert passes != null;
            passDatas.put(customPass, new PassData(passes));
        }
    }

    public boolean render(FrameGraphBuilder builder, int textureWidth, int textureHeight, DefaultFramebufferSet framebufferSet, @Nullable Identifier customPasses) {
        if (!active) return false;
        PostEffectProcessorInterface processor = (PostEffectProcessorInterface)shader.getPostProcessor();
        if (customPasses != null && !processor.luminance$getCustomPassNames().contains(customPasses)) {
            return false;
        }

        processor.luminance$setPersistentBufferSource(this);
        Shaders.renderProcessorUsingFramebufferSet(shader, builder, textureWidth, textureHeight, framebufferSet, customPasses);
        processor.luminance$setPersistentBufferSource(null);
        return true;
    }

    public PassData getPassData(@Nullable Identifier customPasses) {
        return passDatas.get(customPasses);
    }

    public int getRenderPassCount(@Nullable Identifier customPasses) {
        PassData passData = passDatas.get(customPasses);
        if (passData == null) {
            return 0;
        }

        return passData.configs.size();
    }
}
