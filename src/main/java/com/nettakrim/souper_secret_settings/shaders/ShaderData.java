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

    public Map<Identifier, PassData> passData;

    public boolean active = true;

    public ShaderData(Shader shader) {
        this.shader = shader;
        this.shader.setPostProcessor();

        PostEffectProcessorInterface processor = (PostEffectProcessorInterface)this.shader.getPostProcessor();
        Set<Identifier> customPasses = processor.luminance$getCustomPassNames();

        List<PostEffectPass> defaultPasses = processor.luminance$getPasses(null);

        if (defaultPasses.isEmpty()) {
            passData = new HashMap<>(customPasses.size());
        } else {
            passData = new HashMap<>(customPasses.size() + 1);
            passData.put(null, new PassData(defaultPasses));
        }

        for (Identifier customPass : customPasses) {
            List<PostEffectPass> passes = processor.luminance$getPasses(customPass);
            assert passes != null;
            passData.put(customPass, new PassData(passes));
        }
    }

    public boolean render(FrameGraphBuilder builder, int textureWidth, int textureHeight, DefaultFramebufferSet framebufferSet, @Nullable Identifier customPasses) {
        if (!active) return false;
        if (customPasses != null && !((PostEffectProcessorInterface)shader.getPostProcessor()).luminance$getCustomPassNames().contains(customPasses)) {
            return false;
        }

        Shaders.renderProcessorUsingFramebufferSet(shader.getPostProcessor(), builder, textureWidth, textureHeight, framebufferSet, customPasses);
        return true;
    }

    public PassData getPassData(@Nullable Identifier customPasses) {
        return passData.get(customPasses);
    }
}
