package com.nettakrim.souper_secret_settings.shaders;

import com.mclegoman.luminance.client.events.Events;
import com.mclegoman.luminance.client.events.Runnables;
import com.mclegoman.luminance.client.shaders.Shader;
import com.mclegoman.luminance.client.shaders.ShaderRegistry;
import com.mclegoman.luminance.client.shaders.Shaders;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.mixin.GameRendererAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.DefaultFramebufferSet;
import net.minecraft.client.render.FrameGraphBuilder;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;

import java.util.ArrayList;

public class SoupRenderer implements Runnables.WorldRender {
    public ArrayList<ShaderStack> shaderStacks;

    public int activeStack;

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

    public boolean addShader(Identifier id, int amount) {
        if (id.equals(Identifier.ofVanilla("random"))) {
            return addRandomShaders(amount);
        }

        ShaderRegistry shaderRegistry = getShaderRegistry(id);
        if (shaderRegistry == null) {
            SouperSecretSettingsClient.say("shader.missing", id);
            return false;
        }

        Shader shader = new Shader(shaderRegistry, this::getRenderType);
        int i = 0;
        while (i < amount) {
            getActiveStack().addShaderData(new ShaderData(shader));
            i++;
        }

        return true;
    }

    private boolean addRandomShaders(int amount) {
        int i = 0;
        while (i < amount) {
            ShaderRegistry shaderRegistry = getRandomShader();
            if (shaderRegistry == null) {
                SouperSecretSettingsClient.say("shader.registry_empty");
                return false;
            }
            getActiveStack().addShaderData(new ShaderData(new Shader(shaderRegistry, this::getRenderType)));
            i++;
        }
        return true;
    }

    private Shader.RenderType getRenderType() {
        return Shader.RenderType.WORLD;
    }

    public ShaderRegistry getShaderRegistry(Identifier identifier) {
        for (ShaderRegistry shaderRegistry : Shaders.registry) {
            if (shaderRegistry.getID().equals(identifier)) {
                return shaderRegistry;
            }
        }
        return null;
    }

    private ShaderRegistry getRandomShader() {
        int size = Shaders.registry.size();
        if (size == 0) {
            return null;
        }

        Random random = ((GameRendererAccessor)MinecraftClient.getInstance().gameRenderer).getRandom();

        ShaderStack stack = getActiveStack();
        ShaderRegistry previous = stack == null || stack.shaderDatas.isEmpty() ? null : stack.shaderDatas.getLast().shader.getShaderData();
        ShaderRegistry newShader;

        int attempts = 0;
        do {
            newShader = Shaders.registry.get(random.nextBetween(0, size-1));
            attempts++;
        } while (attempts < 100 && previous == newShader);

        return newShader;
    }

    public ShaderStack getActiveStack() {
        return shaderStacks.get(activeStack);
    }

    public void clearAll() {
        shaderStacks = new ArrayList<>();
        shaderStacks.add(new ShaderStack());
        activeStack = 0;
    }
}
