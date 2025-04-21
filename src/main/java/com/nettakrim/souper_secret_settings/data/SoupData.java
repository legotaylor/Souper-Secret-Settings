package com.nettakrim.souper_secret_settings.data;

import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.stream.JsonWriter;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.shaders.Group;
import com.nettakrim.souper_secret_settings.shaders.ShaderLayer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class SoupData {
    protected final Path configDir;
    public final Gson gson;

    public final Config config;

    private int saveChange;

    public Map<Identifier, LayerCodecs> resourceLayers;

    public SoupData() {
        configDir = FabricLoader.getInstance().getConfigDir().resolve(SouperSecretSettingsClient.MODID);
        gson = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();

        Optional<Config> data = loadFromPath(Config.CODEC, configDir.resolve("config.json"));
        config = data.orElseGet(Config::getDefaultConfig);

        resourceLayers = new HashMap<>();
    }

    public boolean loadLayer(ShaderLayer shaderLayer) {
        LayerCodecs layerCodecs = getLayerCodec(shaderLayer.name);
        if (layerCodecs == null) {
            return false;
        }

        layerCodecs.apply(shaderLayer);
        return true;
    }

    @Nullable
    public LayerCodecs getLayerCodec(String name) {
        if (name.contains(":")) {
            return resourceLayers.get(Identifier.tryParse(name));
        } else {
            if (savedLayerExists(name)) {
                return loadFromPath(LayerCodecs.CODEC, getLayerPath(name)).orElse(null);
            }
            return null;
        }
    }

    public boolean savedLayerExists(String name) {
        if (!isValidName(name)) {
            return false;
        }
        Path path = getLayerPath(name);
        return path.toFile().exists();
    }

    public void saveLayer(ShaderLayer shaderLayer, Runnable onComplete) {
        if (!isValidName(shaderLayer.name)) {
            return;
        }

        saveToPath(LayerCodecs.CODEC, getLayerPath(shaderLayer.name), LayerCodecs.from(shaderLayer)).whenComplete((a,b) -> onComplete.run());
    }

    public Path getLayerPath(String name) {
        return configDir.resolve("layers").resolve(name+".json");
    }

    public List<String> getSavedLayers(boolean includeResources) {
        List<String> names = new ArrayList<>();
        File[] files = configDir.resolve("layers").toFile().listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    String name = file.getName();
                    names.add(name.substring(0, name.length()-5));
                }
            }
        }
        if (includeResources) {
            for (Identifier identifier : resourceLayers.keySet()) {
                names.add(identifier.toString());
            }
        }
        return names;
    }

    public void deleteSavedLayer(String name) {
        deleteFile(configDir.resolve("layers").resolve(name+".json"));
    }

    public boolean isValidName(String name) {
        return name != null && !name.isBlank() && name.matches("^[A-Za-z0-9._ ]{1,255}$");
    }

    public void saveConfig() {
        saveChange = 0;
        writeToPath(Config.CODEC.encodeStart(JsonOps.INSTANCE, config).getOrThrow(), configDir.resolve("config.json"));
    }

    public void tick() {
        if (saveChange > 0) {
            saveChange--;
            if (saveChange == 0) {
                saveConfig();
            }
        }
    }

    public void changeConfig() {
        saveChange = 1200;
    }

    public void saveIfChanged() {
        if (saveChange > 0) {
            saveConfig();
        }
    }

    public void saveGroups() {
        SouperSecretSettingsClient.soupRenderer.shaderGroups.forEach(((registry, groups) -> groups.forEach((name, group) -> {
            if (group.file != null && !name.startsWith("user_") && group.entries.size() == 1 && group.entries.getFirst().equals("+random_"+name)) {
                if (group.file.delete()) {
                    group.file = null;
                } else {
                    SouperSecretSettingsClient.log("Failed to delete file "+group.file);
                }
                return;
            }

            if (!group.changed) {
                return;
            }

            if (group.file == null) {
                group.file = SouperSecretSettingsClient.soupData.getGroupLocation(registry, name).toFile();
            }

            saveToPath(Group.CODEC, group.file.toPath(), group);
        })));
    }

    public void loadGroups(Map<String, Group> registryMap, Identifier registry) {
        File[] files = configDir.resolve("groups").resolve(registry.toString().replace(":","_")).toFile().listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    Optional<Group> group = loadFromPath(Group.CODEC, file.toPath());
                    if (group.isPresent()) {
                        String name = file.getName();
                        registryMap.put(name.substring(0, name.length() - 5), group.get());
                    }
                }
            }
        }
    }

    public Path getGroupLocation(Identifier registry, String name) {
        return configDir.resolve("groups").resolve(registry.toString().replace(":","_")).resolve(name+".json");
    }

    public <T> Optional<T> loadFromPath(Codec<T> codec, Path path) {
        try {
            return codec.parse(JsonOps.INSTANCE, gson.fromJson(Files.newBufferedReader(path), JsonElement.class)).result();
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    public static <T> CompletableFuture<?> saveToPath(Codec<T> codec, Path path, T value) {
        JsonElement jsonElement;
        if (value instanceof DeletableCodec deletableCodec && deletableCodec.isEmpty()) {
            jsonElement = null;
        } else {
            jsonElement = codec.encodeStart(JsonOps.INSTANCE, value).getOrThrow();
        }
        return writeToPath(jsonElement, path);
    }

    //see DataProvider.writeCodecToPath - it uses various @Beta and @Deprecated methods/classes
    @SuppressWarnings({"UnstableApiUsage", "deprecation"})
    private static CompletableFuture<?> writeToPath(@Nullable JsonElement json, Path path) {
        return CompletableFuture.runAsync(() -> {
            try {
                if (json == null) {
                    deleteFile(path);
                    return;
                }

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                HashingOutputStream hashingOutputStream = new HashingOutputStream(Hashing.sha1(), byteArrayOutputStream);
                JsonWriter jsonWriter = new JsonWriter(new OutputStreamWriter(hashingOutputStream, StandardCharsets.UTF_8));

                try {
                    jsonWriter.setSerializeNulls(false);
                    jsonWriter.setIndent("");
                    JsonHelper.writeSorted(jsonWriter, json, DataProvider.JSON_KEY_SORTING_COMPARATOR);
                } catch (Throwable var9) {
                    try {
                        jsonWriter.close();
                    } catch (Throwable var8) {
                        var9.addSuppressed(var8);
                    }

                    throw var9;
                }

                jsonWriter.close();
                DataWriter.UNCACHED.write(path, byteArrayOutputStream.toByteArray(), hashingOutputStream.hash());
            } catch (IOException e) {
                SouperSecretSettingsClient.log("Failed to save file to", path, e);
            }

        }, Util.getMainWorkerExecutor().named("saveStable"));
    }

    protected static void deleteFile(Path path) {
        File file = path.toFile();
        if (file.exists() && !file.delete()) {
            SouperSecretSettingsClient.log("failed to delete file", path);
        }
    }
}
