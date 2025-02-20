package com.nettakrim.souper_secret_settings.shaders;

import com.mclegoman.luminance.client.events.Events;
import com.mclegoman.luminance.client.events.Runnables;
import com.mclegoman.luminance.client.shaders.Shader;
import com.mclegoman.luminance.client.shaders.ShaderRegistryEntry;
import com.mclegoman.luminance.client.shaders.Shaders;
import com.mclegoman.luminance.client.shaders.uniforms.Uniform;
import com.mclegoman.luminance.client.util.Accessors;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.DefaultFramebufferSet;
import net.minecraft.client.render.FrameGraphBuilder;
import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SoupRenderer {
    public final List<ShaderLayer> shaderLayers;
    public ShaderLayer activeLayer;

    private List<String> validUniforms;

    public static final Identifier effectRegistry = Identifier.of(SouperSecretSettingsClient.MODID, "effects");

    private RenderType renderType;

    public SoupRenderer() {
        shaderLayers = new ArrayList<>();
        renderType = RenderType.WORLD;

        Events.AfterWeatherRender.register(Identifier.of(SouperSecretSettingsClient.MODID, "rendering"), ((builder, width, height, defaultFramebufferSet) -> this.render(RenderType.WORLD, builder, width, height, defaultFramebufferSet)));
        Events.AfterHandRender.register(Identifier.of(SouperSecretSettingsClient.MODID, "rendering"), (framebuffer, objectAllocator) -> this.renderWithAllocator(RenderType.HAND, framebuffer, objectAllocator));
        Events.AfterGameRender.register(Identifier.of(SouperSecretSettingsClient.MODID, "rendering"), (framebuffer, objectAllocator) -> this.renderWithAllocator(RenderType.GAME, framebuffer, objectAllocator));

        Events.OnShaderDataReset.register(Identifier.of(SouperSecretSettingsClient.MODID, "reset"), this::clearAll);
        Events.AfterShaderDataRegistered.register(Identifier.of(SouperSecretSettingsClient.MODID, "reload"), this::loadDefault);

        Events.BeforeShaderRender.register(Identifier.of(SouperSecretSettingsClient.MODID, "before_render"), new OverrideManager.BeforeShaderRender());
        Events.AfterShaderRender.register(Identifier.of(SouperSecretSettingsClient.MODID, "after_render"), new OverrideManager.AfterShaderRender());
    }

    private void renderWithAllocator(RenderType renderType, Framebuffer framebuffer, ObjectAllocator objectAllocator) {
        FrameGraphBuilder frameGraphBuilder = new FrameGraphBuilder();
        PostEffectProcessor.FramebufferSet framebufferSet = PostEffectProcessor.FramebufferSet.singleton(PostEffectProcessor.MAIN, frameGraphBuilder.createObjectNode("main", framebuffer));
        render(renderType, frameGraphBuilder, framebuffer.textureWidth, framebuffer.textureHeight, framebufferSet);
        frameGraphBuilder.run(objectAllocator);
    }

    private void render(RenderType renderType, FrameGraphBuilder builder, int textureWidth, int textureHeight, PostEffectProcessor.FramebufferSet framebufferSet) {
        if (this.renderType != renderType || shaderLayers == null) {
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
        return renderType.renderType;
    }

    public static ShaderRegistryEntry getRegistryEntry(Identifier registry, Identifier identifier) {
        for (ShaderRegistryEntry shaderRegistry : Shaders.getRegistry(registry)) {
            if (shaderRegistry.getID().equals(identifier)) {
                return shaderRegistry;
            }
        }

        if (identifier.getNamespace().equals("minecraft")) {
            Identifier guessed = Shaders.guessPostShader(registry, identifier.getPath());
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
        renderType = RenderType.values()[(renderType.ordinal()+1)%RenderType.values().length];
        buttonWidget.setMessage(getRenderTypeText());
    }

    public Text getRenderTypeText() {
        return Text.literal(renderType.toString());
    }

    private enum RenderType {
        WORLD(Shader.RenderType.WORLD),
        HAND(Shader.RenderType.GAME),
        GAME(Shader.RenderType.GAME);

        public final Shader.RenderType renderType;

        RenderType(Shader.RenderType renderType) {
            this.renderType = renderType;
        }
    }
}
