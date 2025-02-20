package com.nettakrim.souper_secret_settings.shaders;

import com.mclegoman.luminance.client.events.Events;
import com.mclegoman.luminance.client.events.Runnables;
import com.mclegoman.luminance.client.shaders.Shader;
import com.mclegoman.luminance.client.shaders.ShaderRegistryEntry;
import com.mclegoman.luminance.client.shaders.Shaders;
import com.mclegoman.luminance.client.shaders.uniforms.Uniform;
import com.mclegoman.luminance.client.util.Accessors;
import com.mclegoman.luminance.client.util.CompatHelper;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.FrameGraphBuilder;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SoupRenderer implements Runnables.WorldRender {
    public final List<ShaderLayer> shaderLayers;
    public ShaderLayer activeLayer;

    private List<String> validUniforms;

    public static final Identifier effectRegistry = Identifier.of(SouperSecretSettingsClient.MODID, "effects");

    private Shader.RenderType renderType;

    public SoupRenderer() {
        shaderLayers = new ArrayList<>();
        renderType = Shader.RenderType.WORLD;

        Events.AfterWeatherRender.register(Identifier.of(SouperSecretSettingsClient.MODID, "rendering"), ((builder, width, height, framebufferSet) -> {
            if (renderType == Shader.RenderType.WORLD && !CompatHelper.isIrisShadersEnabled()) {
                this.run(builder, width, height, framebufferSet);
            }
        }));
        Events.AfterHandRender.register(Identifier.of(SouperSecretSettingsClient.MODID, "rendering"), (framebuffer, objectAllocator) -> {
            if (renderType == Shader.RenderType.WORLD && CompatHelper.isIrisShadersEnabled()) {
                Runnables.WorldRender.fromGameRender(this, framebuffer, objectAllocator);
            }
        });
        Events.AfterGameRender.register(Identifier.of(SouperSecretSettingsClient.MODID, "rendering"), (framebuffer, objectAllocator) -> {
            if (renderType == Shader.RenderType.GAME) {
                Runnables.WorldRender.fromGameRender(this, framebuffer, objectAllocator);
            }
        });

        Events.OnShaderDataReset.register(Identifier.of(SouperSecretSettingsClient.MODID, "reset"), this::clearAll);
        Events.AfterShaderDataRegistered.register(Identifier.of(SouperSecretSettingsClient.MODID, "reload"), this::loadDefault);

        Events.BeforeShaderRender.register(Identifier.of(SouperSecretSettingsClient.MODID, "before_render"), new OverrideManager.BeforeShaderRender());
        Events.AfterShaderRender.register(Identifier.of(SouperSecretSettingsClient.MODID, "after_render"), new OverrideManager.AfterShaderRender());
    }

    @Override
    public void run(FrameGraphBuilder builder, int textureWidth, int textureHeight, PostEffectProcessor.FramebufferSet framebufferSet) {
        if (shaderLayers == null) {
            return;
        }

        for (ShaderLayer layer : shaderLayers) {
            layer.render(builder, textureWidth, textureHeight, framebufferSet);
        }

        ShaderLayer.renderCleanup(builder);
    }

    @Nullable
    public List<ShaderData> getShaderAdditions(Identifier registry, Identifier id, int amount, ShaderLayer layer) {
        if (id.equals(Identifier.ofVanilla("random"))) {
            return getRandomShaders(layer, registry, amount);
        }

        ShaderRegistryEntry shaderRegistry = getRegistryEntry(registry, id);
        if (shaderRegistry == null) {
            SouperSecretSettingsClient.say("shader.missing", id);
            return null;
        }

        List<ShaderData> shaders = new ArrayList<>();

        Shader shader = new Shader(shaderRegistry, this::getRenderType);
        int i = 0;
        while (i < amount) {
            shaders.add(new ShaderData(shader));
            i++;
        }

        return shaders;
    }

    @Nullable
    private List<ShaderData> getRandomShaders(ShaderLayer layer, Identifier registry, int amount) {
        List<ShaderData> shaders = new ArrayList<>();
        ShaderRegistryEntry shaderRegistry = (layer == null || layer.shaders.isEmpty()) ? null : layer.shaders.getLast().shader.getShaderData();

        int i = 0;
        while (i < amount) {
            shaderRegistry = getRandomShader(registry, shaderRegistry);
            if (shaderRegistry == null) {
                SouperSecretSettingsClient.say("shader.registry_empty");
                return null;
            }
            shaders.add(new ShaderData(new Shader(shaderRegistry, this::getRenderType)));
            i++;
        }

        return shaders;
    }

    private ShaderRegistryEntry getRandomShader(Identifier registry, ShaderRegistryEntry previous) {
        int size = Shaders.getRegistry(registry).size();
        if (size == 0) {
            return null;
        }

        Random random = Accessors.getGameRenderer().getRandom();
        ShaderRegistryEntry newShader;

        int attempts = 0;
        do {
            newShader = Shaders.getRegistry(registry).get(random.nextBetween(0, size-1));
            attempts++;
        } while (attempts < 100 && previous == newShader);

        return newShader;
    }

    private Shader.RenderType getRenderType() {
        return renderType;
    }

    public static ShaderRegistryEntry getRegistryEntry(Identifier registry, Identifier identifier) {
        for (ShaderRegistryEntry shaderRegistry : Shaders.getRegistry(registry)) {
            if (shaderRegistry.getID().equals(identifier)) {
                return shaderRegistry;
            }
        }

        if (identifier.getNamespace().equals("minecraft")) {
            Identifier guessed = Shaders.guessPostShader(registry, identifier.getPath());
            assert guessed != null;
            if (!guessed.getNamespace().equals("minecraft")) {
                return getRegistryEntry(registry, guessed);
            }
        }

        return null;
    }

    public void clearAll() {
        shaderLayers.clear();
    }

    public void loadDefault() {
        activeLayer = new ShaderLayer("default");
        shaderLayers.add(activeLayer);
    }

    public List<String> getValidUniforms() {
        if (validUniforms == null) {
            validUniforms = new ArrayList<>();
            for (Map.Entry<String, Uniform> entry : Events.ShaderUniform.registry.entrySet()) {
                if (entry.getValue().getLength() == 1) {
                    validUniforms.add(entry.getKey());
                }
            }
        }

        return validUniforms;
    }

    public void cycleRenderType(ButtonWidget buttonWidget) {
        renderType = Shader.RenderType.values()[(renderType.ordinal()+1)%Shader.RenderType.values().length];
        buttonWidget.setMessage(getRenderTypeText());
    }

    public Text getRenderTypeText() {
        return Text.literal(renderType.toString());
    }
}
