package com.nettakrim.souper_secret_settings.shaders;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mclegoman.luminance.client.util.JsonResourceReloader;
import com.mojang.serialization.JsonOps;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.data.LayerCodecs;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.util.Map;
import java.util.Optional;

public class SoupReloader extends JsonResourceReloader {
    public static final String resourceLocation = "souper_secret_settings";
    public SoupReloader() {
        super(new Gson(), resourceLocation);
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> prepared, ResourceManager manager, Profiler profiler) {
        SouperSecretSettingsClient.soupData.resourceLayers.clear();

        prepared.forEach((identifier, jsonElement) -> {
            try {
                if (identifier.getPath().startsWith("layers/")) {
                    Optional<LayerCodecs> layerCodecs = LayerCodecs.CODEC.parse(JsonOps.INSTANCE, jsonElement).result();
                    layerCodecs.ifPresent(codecs -> SouperSecretSettingsClient.soupData.resourceLayers.put(Identifier.of(identifier.getNamespace(), identifier.getPath().substring(7)), codecs));
                }
                else if (identifier.getPath().startsWith("groups/")) {
                    String full = identifier.getPath().substring(7);
                    int i = full.indexOf("/");
                    Identifier registry = Identifier.tryParse(full.substring(0, i).replaceFirst("_", ":"));
                    String name = full.substring(i + 1);

                    Map<String, Group> registryMap = SouperSecretSettingsClient.soupRenderer.getRegistryGroups(registry);
                    Optional<Group> group = Group.CODEC.parse(JsonOps.INSTANCE, jsonElement).result();

                    if (group.isPresent()) {
                        if (!registryMap.containsKey(name) || registryMap.get(name).file == null) {
                            registryMap.put(name, group.get());
                        }
                    }
                }
            }
            catch (Exception ignored) {}
        });
    }
}
