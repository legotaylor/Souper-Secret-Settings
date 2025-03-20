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
import com.nettakrim.souper_secret_settings.shaders.ShaderLayer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class SoupData {
    protected final Path configDir;
    protected final Gson gson;

    public final Config config;

    private int saveChange;

    public SoupData() {
        configDir = FabricLoader.getInstance().getConfigDir().resolve(SouperSecretSettingsClient.MODID);
        gson = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();

        Optional<Config> data = loadFromPath(Config.CODEC, configDir.resolve("config.json"));
        config = data.orElseGet(Config::getDefaultConfig);
    }

    public boolean loadLayer(ShaderLayer shaderLayer) {
        if (!savedLayerExists(shaderLayer.name)) {
            return false;
        }

        Optional<LayerCodecs> data = loadFromPath(LayerCodecs.CODEC, getLayerPath(shaderLayer.name));
        data.ifPresent(layerCodecs -> layerCodecs.apply(shaderLayer));
        return true;
    }

    public boolean savedLayerExists(String name) {
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

    public List<String> getSavedLayers() {
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
        return names;
    }

    public void deleteSavedLayer(String name) {
        deleteFile(configDir.resolve("layers").resolve(name+".json"));
    }

    public boolean isValidName(String name) {
        return name != null && !name.isBlank() && name.matches("^[A-Za-z0-9._ ]{1,255}$");
    }

    public void saveConfig() {
        SouperSecretSettingsClient.log("Saving Config");
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
            saveChange = 0;
        }
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
