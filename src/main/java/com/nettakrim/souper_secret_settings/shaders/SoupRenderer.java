package com.nettakrim.souper_secret_settings.shaders;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class SoupRenderer implements Runnables.WorldRender {
    public final List<ShaderLayer> shaderLayers;
    public ShaderLayer activeLayer;

    private List<String> validUniforms;

    private Shader.RenderType renderType;

    public final Map<Identifier, Map<String, List<ShaderRegistryEntry>>> shaderGroups;

    public static final Identifier modifierRegistry = Identifier.of(SouperSecretSettingsClient.MODID, "modifiers");

    public SoupRenderer() {
        shaderLayers = new ArrayList<>();
        shaderGroups = new HashMap<>();
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

        Events.OnShaderDataReset.register(Identifier.of(SouperSecretSettingsClient.MODID, "reset"), () -> {
            shaderGroups.clear();
            clearAll();
        });
        Events.OnShaderDataRegistered.register(Identifier.of(SouperSecretSettingsClient.MODID, "add"), (shaderRegistryEntry, registries) -> runForGroups(shaderRegistryEntry, registries, (registry, group) -> shaderGroups.computeIfAbsent(registry, k -> new HashMap<>()).computeIfAbsent(group, k -> new ArrayList<>()).add(shaderRegistryEntry)));
        Events.OnShaderDataRemoved.register(Identifier.of(SouperSecretSettingsClient.MODID, "remove"), (shaderRegistryEntry, registries) -> runForGroups(shaderRegistryEntry, registries, (registry, group) -> {
            Map<String, List<ShaderRegistryEntry>> registryGroups = shaderGroups.get(registry);
            if (registryGroups != null) {
                List<ShaderRegistryEntry> groupShaders = registryGroups.get(group);
                if (groupShaders != null) {
                    groupShaders.remove(shaderRegistryEntry);
                }
            }
        }));
        Events.AfterShaderDataRegistered.register(Identifier.of(SouperSecretSettingsClient.MODID, "reload"), this::loadDefault);

        Events.BeforeShaderRender.register(Identifier.of(SouperSecretSettingsClient.MODID, "before_render"), new OverrideManager.BeforeShaderRender());
        Events.AfterShaderRender.register(Identifier.of(SouperSecretSettingsClient.MODID, "after_render"), new OverrideManager.AfterShaderRender());
    }

    @Override
    public void run(FrameGraphBuilder builder, int textureWidth, int textureHeight, PostEffectProcessor.FramebufferSet framebufferSet) {
        if (shaderLayers == null || SouperSecretSettingsClient.soupData.config.disableState > 0) {
            return;
        }

        for (ShaderLayer layer : shaderLayers) {
            layer.render(builder, textureWidth, textureHeight, framebufferSet);
        }

        ShaderLayer.renderCleanup(builder);
    }

    @Nullable
    public List<ShaderData> getShaderAdditions(Identifier registry, Identifier id, int amount, ShaderLayer layer) {
        if (id.getNamespace().equals("minecraft") && id.getPath().startsWith("random")) {
            int i = id.getPath().indexOf("_");
            return getRandomShaders(layer, registry, i == -1 ? null : id.getPath().substring(i+1), amount);
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
    private List<ShaderData> getRandomShaders(ShaderLayer layer, Identifier registry, String group, int amount) {
        List<ShaderData> shaders = new ArrayList<>();
        ShaderRegistryEntry shaderRegistry = (layer == null || layer.shaders.isEmpty()) ? null : layer.shaders.getLast().shader.getShaderData();

        int i = 0;
        while (i < amount) {
            shaderRegistry = getRandomShader(registry, group, shaderRegistry);
            if (shaderRegistry == null) {
                SouperSecretSettingsClient.say("shader.registry_empty");
                return null;
            }
            shaders.add(new ShaderData(new Shader(shaderRegistry, this::getRenderType)));
            i++;
        }

        return shaders;
    }

    private ShaderRegistryEntry getRandomShader(Identifier registry, String group, ShaderRegistryEntry previous) {
        List<ShaderRegistryEntry> registryEntries = null;
        if (group == null) {
            registryEntries = Shaders.getRegistry(registry);
        } else {
            Map<String, List<ShaderRegistryEntry>> registryGroups = shaderGroups.get(registry);
            if (registryGroups != null) {
                registryEntries = registryGroups.get(group);
            }
        }

        if (registryEntries == null) {
            return null;
        }

        int size = registryEntries.size();
        if (size == 0) {
            return null;
        }
        if (size == 1) {
            return registryEntries.getFirst();
        }

        Random random = Accessors.getGameRenderer().getRandom();
        ShaderRegistryEntry newShader;

        int attempts = 0;
        do {
            newShader = registryEntries.get(random.nextBetween(0, size-1));
            attempts++;
        } while (attempts < 100 && previous == newShader);

        return newShader;
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
        setRenderType(renderType == Shader.RenderType.GAME ? Shader.RenderType.WORLD : Shader.RenderType.GAME);
        buttonWidget.setMessage(getRenderTypeText());
    }

    public void setRenderType(Shader.RenderType renderType) {
        this.renderType = renderType;
    }

    public Shader.RenderType getRenderType() {
        return renderType;
    }

    public Text getRenderTypeText() {
        //icons:                                                    💻               🌎
        return Text.literal(renderType == Shader.RenderType.GAME ? "\uD83D\uDCBB" : "\uD83C\uDF0E");
    }

    private void runForGroups(ShaderRegistryEntry shaderRegistryEntry, List<Identifier> registries, BiConsumer<Identifier, String> consumer) {
        List<String> groups = getGroups(shaderRegistryEntry);
        for (Identifier registry : registries) {
            for (String group : groups) {
                consumer.accept(registry, group);
            }
        }
    }

    public List<String> getGroups(ShaderRegistryEntry shaderRegistryEntry) {
        JsonElement soup = shaderRegistryEntry.getCustom().get(SouperSecretSettingsClient.MODID);
        if (soup != null) {
            try {
                JsonElement element = soup.getAsJsonObject().get("groups");
                if (element != null) {
                    JsonArray jsonGroups = element.getAsJsonArray();
                    List<String> groups = new ArrayList<>(jsonGroups.size());
                    for (JsonElement jsonElement : jsonGroups) {
                        try {
                            groups.add(jsonElement.getAsString());
                        } catch (IllegalStateException ignored) {}
                    }
                    return groups;
                }
            } catch (IllegalStateException ignored) {}
        }

        return List.of("edible");
    }

    private static final Identifier[] modifierPasses = new Identifier[]{
            Identifier.of(SouperSecretSettingsClient.MODID, "before_layer_render"),
            Identifier.of(SouperSecretSettingsClient.MODID, "before_shader_render"),
            Identifier.of(SouperSecretSettingsClient.MODID, "after_shader_render"),
            Identifier.of(SouperSecretSettingsClient.MODID, "after_layer_render")
    };

    public Identifier[] getRegistryPasses(@Nullable Identifier registry) {
        if (modifierRegistry.equals(registry)) {
            return modifierPasses;
        } else {
            return new Identifier[]{null};
        }
    }
}
