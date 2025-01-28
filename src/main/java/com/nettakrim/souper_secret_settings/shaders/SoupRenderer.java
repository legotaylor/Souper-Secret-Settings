package com.nettakrim.souper_secret_settings.shaders;

import com.mclegoman.luminance.client.events.Events;
import com.mclegoman.luminance.client.events.Runnables;
import com.mclegoman.luminance.client.shaders.Shader;
import com.mclegoman.luminance.client.shaders.ShaderRegistryEntry;
import com.mclegoman.luminance.client.shaders.Shaders;
import com.mclegoman.luminance.client.shaders.uniforms.Uniform;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.mixin.GameRendererAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.DefaultFramebufferSet;
import net.minecraft.client.render.FrameGraphBuilder;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class SoupRenderer implements Runnables.WorldRender {
    public ArrayList<ShaderStack> shaderStacks;

    public int activeStack;

    private List<String> validUniforms;

    public static final Identifier layerEffectRegistry = Identifier.of(SouperSecretSettingsClient.MODID, "layer_effects");

    public SoupRenderer() {
        clearAll();

        Events.AfterWeatherRender.register(Identifier.of(SouperSecretSettingsClient.MODID, "rendering"), this);

        Events.OnShaderDataReset.register(Identifier.of(SouperSecretSettingsClient.MODID, "reload"), this::clearAll);

        Events.BeforeShaderRender.register(Identifier.of(SouperSecretSettingsClient.MODID, "before_render"), new OverrideManager.BeforeShaderRender());
        Events.AfterShaderRender.register(Identifier.of(SouperSecretSettingsClient.MODID, "after_render"), new OverrideManager.AfterShaderRender());
    }

    @Override
    public void run(FrameGraphBuilder builder, int textureWidth, int textureHeight, DefaultFramebufferSet framebufferSet) {
        for (ShaderStack stack : shaderStacks) {
            stack.render(builder, textureWidth, textureHeight, framebufferSet);
        }
    }

    public boolean clearShader() {
        getActiveStack().clear();
        return true;
    }

    public boolean removeTop() {
        if (getActiveStack().shaderDatas.isEmpty()) {
            return false;
        }
        getActiveStack().shaderDatas.removeLast();
        return true;
    }

    public boolean addShaders(Identifier registry, Identifier id, int amount, ShaderStack stack, Consumer<ShaderData> addFunction) {
        List<ShaderData> shaders = getShaderAdditions(registry, id, amount, stack);
        if (shaders == null || shaders.isEmpty()) {
            return false;
        }

        for (ShaderData shaderData : shaders) {
            addFunction.accept(shaderData);
        }
        return true;
    }

    @Nullable
    private List<ShaderData> getShaderAdditions(Identifier registry, Identifier id, int amount, ShaderStack stack) {
        if (id.equals(Identifier.ofVanilla("random"))) {
            return getRandomShaders(stack, registry, amount);
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
    private List<ShaderData> getRandomShaders(ShaderStack stack, Identifier registry, int amount) {
        List<ShaderData> shaders = new ArrayList<>();
        ShaderRegistryEntry shaderRegistry = (stack == null || stack.shaderDatas.isEmpty()) ? null : stack.shaderDatas.getLast().shader.getShaderData();

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

        Random random = ((GameRendererAccessor)MinecraftClient.getInstance().gameRenderer).getRandom();
        ShaderRegistryEntry newShader;

        int attempts = 0;
        do {
            newShader = Shaders.getRegistry(registry).get(random.nextBetween(0, size-1));
            attempts++;
        } while (attempts < 100 && previous == newShader);

        return newShader;
    }

    private Shader.RenderType getRenderType() {
        return Shader.RenderType.WORLD;
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

    public ShaderStack getActiveStack() {
        return shaderStacks.get(activeStack);
    }

    public void clearAll() {
        shaderStacks = new ArrayList<>();
        shaderStacks.add(new ShaderStack());
        activeStack = 0;
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
}
