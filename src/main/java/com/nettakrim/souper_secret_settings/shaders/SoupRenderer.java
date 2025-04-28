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
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.commands.SouperSecretSettingsCommands;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.FrameGraphBuilder;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class SoupRenderer implements Runnables.WorldRender {
    public final List<ShaderLayer> shaderLayers;
    public ShaderLayer activeLayer;

    private final SoupSpectateHandler spectateHandler;

    private List<String> validUniforms;

    private Shader.RenderType renderType;

    public final Map<Identifier, Map<String, Group>> shaderGroups;
    public final Map<Identifier, Map<String, List<ShaderRegistryEntry>>> shaderGroupRegistries;

    public static final Identifier modifierRegistry = Identifier.of(SouperSecretSettingsClient.MODID, "modifiers");

    public int randomTimer;

    public SoupRenderer() {
        shaderLayers = new ArrayList<>();
        shaderGroups = new HashMap<>();
        shaderGroupRegistries = new HashMap<>();
        renderType = Shader.RenderType.WORLD;

        spectateHandler = new SoupSpectateHandler();
        Events.SpectatorHandlers.register(Identifier.of(SouperSecretSettingsClient.MODID, "spectate_handler"), spectateHandler);
        Events.AfterHandRender.register(Identifier.of(SouperSecretSettingsClient.MODID, "rendering"), (framebuffer, objectAllocator) -> {
            if (SouperSecretSettingsClient.soupData.config.disableState == 0) {
                if (spectateHandler.shaderLayer != null) {
                    Runnables.WorldRender.fromGameRender(spectateHandler.shaderLayer::render, framebuffer, objectAllocator);
                    ShaderLayer.renderCleanup(null);
                }
                if (renderType == Shader.RenderType.WORLD) {
                    Runnables.WorldRender.fromGameRender(this, framebuffer, objectAllocator);
                }
            }
        });
        Events.AfterGameRender.register(Identifier.of(SouperSecretSettingsClient.MODID, "rendering"), (framebuffer, objectAllocator) -> {
            if (renderType == Shader.RenderType.GAME && SouperSecretSettingsClient.soupData.config.disableState == 0) {
                Runnables.WorldRender.fromGameRender(this, framebuffer, objectAllocator);
            }
        });

        Events.OnShaderDataReset.register(Identifier.of(SouperSecretSettingsClient.MODID, "reset"), () -> {
            clearAll();
            SouperSecretSettingsClient.actions.clear();
            shaderGroupRegistries.clear();
            SouperSecretSettingsClient.soupRenderer.shaderGroups.clear();
        });
        Events.OnShaderDataRegistered.register(Identifier.of(SouperSecretSettingsClient.MODID, "add"), (shaderRegistryEntry, registries) -> runForGroups(shaderRegistryEntry, registries, (registry, group) -> shaderGroupRegistries.computeIfAbsent(registry, (i) -> new HashMap<>()).computeIfAbsent(group, (i) -> new ArrayList<>()).add(shaderRegistryEntry)));
        Events.OnShaderDataRemoved.register(Identifier.of(SouperSecretSettingsClient.MODID, "remove"), (shaderRegistryEntry, registries) -> runForGroups(shaderRegistryEntry, registries, (registry, group) -> {
            Map<String, List<ShaderRegistryEntry>> map = shaderGroupRegistries.get(registry);
            if (map != null) {
                List<ShaderRegistryEntry> list = map.get(group);
                list.remove(shaderRegistryEntry);
            }
        }));
        Events.AfterClientResourceReload.register(Identifier.of(SouperSecretSettingsClient.MODID, "reload"), this::loadDefault);

        Events.BeforeShaderRender.register(Identifier.of(SouperSecretSettingsClient.MODID, "before_render"), new OverrideManager.BeforeShaderRender());
        Events.AfterShaderRender.register(Identifier.of(SouperSecretSettingsClient.MODID, "after_render"), new OverrideManager.AfterShaderRender());
    }

    @Override
    public void run(FrameGraphBuilder builder, int textureWidth, int textureHeight, PostEffectProcessor.FramebufferSet framebufferSet) {
        if (shaderLayers != null) {
            for (ShaderLayer layer : shaderLayers) {
                layer.render(builder, textureWidth, textureHeight, framebufferSet);
            }
        }

        ShaderLayer.renderCleanup(builder);
    }

    public void tick() {
        if (randomTimer > 0) {
            randomTimer--;
            if (randomTimer == 0) {
                SouperSecretSettingsCommands.shaderCommand.removeAll(null);
            }
        }
    }

    @Nullable
    public List<ShaderData> getShaderAdditions(Identifier registry, Identifier id, int amount, ShaderLayer layer, boolean log) {
        if (id.getNamespace().equals("minecraft") && id.getPath().startsWith("random")) {
            int i = id.getPath().indexOf("_");
            return getRandomShaders(layer, registry, i == -1 ? null : id.getPath().substring(i+1), amount);
        }

        ShaderRegistryEntry shaderRegistry = getRegistryEntry(registry, id);
        if (shaderRegistry == null) {
            if (log) {
                SouperSecretSettingsClient.say("shader.missing", 1, id);
            }
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
                SouperSecretSettingsClient.say("shader.registry_empty", 1);
                return null;
            }
            shaders.add(new ShaderData(new Shader(shaderRegistry, this::getRenderType)));
            i++;
        }

        return shaders;
    }

    private ShaderRegistryEntry getRandomShader(Identifier registry, String groupName, ShaderRegistryEntry previous) {
        List<ShaderRegistryEntry> registryEntries;
        if (groupName == null) {
            registryEntries = Shaders.getRegistry(registry);
        } else {
            Group group = getShaderGroups(registry).get(groupName);
            if (group == null) {
                registryEntries = null;
            } else {
                registryEntries = group.getComputed(registry, groupName);
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
        List<ShaderRegistryEntry> registryEntries = Shaders.getRegistry(registry);

        for (ShaderRegistryEntry shaderRegistry : registryEntries) {
            if (shaderRegistry.getID().equals(identifier)) {
                return shaderRegistry;
            }
        }

        if (identifier.getNamespace().equals("minecraft")) {
            for (ShaderRegistryEntry shaderRegistry : registryEntries) {
                if (shaderRegistry.getID().getPath().equals(identifier.getPath())) {
                    return shaderRegistry;
                }
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
        return SouperSecretSettingsClient.translate(renderType == Shader.RenderType.GAME ? "gui.game" : "gui.world");
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

    @NotNull
    public Map<String, Group> getShaderGroups(Identifier registry) {
        if (!shaderGroups.containsKey(registry)) {
            Map<String, Group> map = new HashMap<>();
            SouperSecretSettingsClient.soupData.loadGroups(map, registry);
            shaderGroupRegistries.get(registry).forEach((name, group) -> map.putIfAbsent(name, new Group(List.of("+random_"+name))));
            shaderGroups.put(registry, map);
            return map;
        }
        return shaderGroups.get(registry);
    }
}
