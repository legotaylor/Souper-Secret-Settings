package com.nettakrim.souper_secret_settings.shaders;

import com.mclegoman.luminance.client.shaders.Shader;
import com.mclegoman.luminance.client.shaders.Shaders;
import com.mclegoman.luminance.client.shaders.interfaces.PostChainInterface;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class ShaderData implements Toggleable {
    public Shader shader;

    public Map<Identifier, PassData> passDatas;

    public boolean active = true;
    public boolean expanded = false;

    static long uuidCounter = 0;
    private final Identifier uuid;

    public ShaderData(Shader shader) {
        this.shader = shader;
        if (this.shader.getPostProcessor() == null) {
            this.shader.setPostProcessor();
        }

        PostChainInterface processor = (PostChainInterface)this.shader.getPostProcessor();
        Set<Identifier> customPasses = processor.luminance$getCustomPassNames();

        List<PostPass> defaultPasses = processor.luminance$getPasses(null);

        if (defaultPasses.isEmpty()) {
            passDatas = new HashMap<>(customPasses.size());
        } else {
            passDatas = new HashMap<>(customPasses.size() + 1);
            passDatas.put(null, new PassData(defaultPasses));
        }

        for (Identifier customPass : customPasses) {
            List<PostPass> passes = processor.luminance$getPasses(customPass);
            assert passes != null;
            passDatas.put(customPass, new PassData(passes));
        }

        uuid = Identifier.fromNamespaceAndPath(SouperSecretSettingsClient.MODID, String.valueOf(uuidCounter++));
    }

    public boolean render(FrameGraphBuilder builder, int textureWidth, int textureHeight, PostChain.TargetBundle framebufferSet, @Nullable Identifier customPasses) {
        if (!active) return false;
        PostChainInterface processor = (PostChainInterface)shader.getPostProcessor();
        if (customPasses != null && !processor.luminance$getCustomPassNames().contains(customPasses)) {
            return false;
        }

        processor.luminance$setPersistentBufferSource(uuid);
        Shaders.renderProcessorUsingTargetBundle(shader, builder, textureWidth, textureHeight, framebufferSet, customPasses);
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

    public Component getTranslatedName() {
        String s = shader.getShaderId().toString();
        return Component.translatableWithFallback("gui.luminance.shader."+s.replace(':','.'), s);
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void setActive(boolean to) {
        active = to;
    }
}
