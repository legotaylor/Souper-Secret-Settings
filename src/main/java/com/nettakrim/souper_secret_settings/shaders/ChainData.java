package com.nettakrim.souper_secret_settings.shaders;

import com.mclegoman.luminance.client.shaders.UniformBlock;
import com.mclegoman.luminance.client.shaders.interfaces.PostPassInterface;
import com.mclegoman.luminance.client.shaders.overrides.*;
import com.mclegoman.luminance.client.shaders.uniforms.config.EmptyConfig;
import com.mclegoman.luminance.client.shaders.uniforms.config.MapConfig;
import com.mclegoman.luminance.client.shaders.uniforms.config.UniformConfig;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import java.util.*;

import net.minecraft.client.renderer.PostPass;
import net.minecraft.resources.Identifier;

public class ChainData {
    public final ArrayList<Map<String, BlockData>> passBlocks;
    public BitSet passesExpanded;

    private static final Identifier overridePath = Identifier.fromNamespaceAndPath(SouperSecretSettingsClient.MODID, "uniform_override");
    private static final Identifier configPath = Identifier.fromNamespaceAndPath(SouperSecretSettingsClient.MODID, "uniform_config");

    public ChainData(List<PostPass> passes) {
        this.passBlocks = new ArrayList<>(passes.size());
        this.passesExpanded = new BitSet(passes.size());

        for (PostPass pass : passes) {
            PostPassInterface postPass = (PostPassInterface)pass;
            Map<String, BlockData> blocks = new HashMap<>(postPass.luminance$getUniformBlocks().size());

            int i = 0;
            for (Map.Entry<String, UniformBlock> entry : postPass.luminance$getUniformBlocks().entrySet()) {
                String path = String.valueOf(i++);
                blocks.put(entry.getKey(), new BlockData(entry.getValue(), postPass, overridePath.withPath(path)));
            }

            this.passBlocks.add(blocks);
        }
    }

    public static boolean isChanged(UniformDataOld<UniformOverride> override, UniformDataOld<UniformConfig> config) {
        if (!((PerValueOverride)override.value).getStrings().equals(((PerValueOverride)override.defaultValue).getStrings())) {
            return true;
        }

        MapConfig mapConfig = (MapConfig)config.value;

        if (mapConfig.config().isEmpty() && config.defaultValue == EmptyConfig.INSTANCE) {
            return false;
        }

        if (config.defaultValue instanceof MapConfig defaultMap) {
            if (!defaultMap.config().keySet().equals(mapConfig.config().keySet())) {
                return true;
            }

            for (String s : defaultMap.config().keySet()) {
                List<Object> defaultObjects = defaultMap.config().get(s);
                List<Object> currentObjects = mapConfig.config().get(s);
                if (defaultObjects.size() != currentObjects.size()) {
                    return true;
                }

                for (int i = 0; i < defaultObjects.size(); i++) {
                    Object defaultObject = defaultObjects.get(i);
                    Object currentObject = currentObjects.get(i);

                    if (defaultObject.equals(currentObject)) {
                        continue;
                    }

                    if (defaultObject instanceof Number defaultNumber && currentObject instanceof Number currentNumber) {
                        if (defaultNumber.doubleValue() != currentNumber.doubleValue()) {
                            return true;
                        }
                    }
                }
            }

            return false;
        }

        return true;
    }

    public static String getName(PostPassInterface pass) {
        // if the vertex shader isnt core/screenquad, then its probably important
        RenderPipeline pipeline = pass.luminance$getPipeline();
        Identifier identifier = pipeline.getVertexShader().equals(Identifier.withDefaultNamespace("core/screenquad")) ? pipeline.getFragmentShader() : pipeline.getVertexShader();
        return identifier.toString().replace(":post/",":");
    }
}
