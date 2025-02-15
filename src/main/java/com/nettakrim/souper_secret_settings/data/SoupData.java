package com.nettakrim.souper_secret_settings.data;

import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.stream.JsonWriter;
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
import java.util.concurrent.CompletableFuture;

public class SoupData {
    protected final Path configDir;
    protected final Gson gson;

    public SoupData() {
        configDir = FabricLoader.getInstance().getConfigDir().resolve(SouperSecretSettingsClient.MODID);
        gson = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();
    }

    public void loadLayer(ShaderLayer shaderLayer) {
        Path path = getLayerPath(shaderLayer);
        if (!path.toFile().exists()) {
            return;
        }

        try {
            LayerCodecs.CODEC.parse(JsonOps.INSTANCE, gson.fromJson(Files.newBufferedReader(getLayerPath(shaderLayer)), JsonElement.class)).result().ifPresent((data) -> data.apply(shaderLayer));
        } catch (IOException e) {
            SouperSecretSettingsClient.log("Failed to load layer "+shaderLayer.name);
        }
    }

    public void saveLayer(ShaderLayer shaderLayer, Runnable onComplete) {
        if (!isValidName(shaderLayer.name)) {
            return;
        }

        LayerCodecs layerCodec = LayerCodecs.from(shaderLayer);
        JsonElement jsonElement = layerCodec.isEmpty() ? null : LayerCodecs.CODEC.encodeStart(JsonOps.INSTANCE, layerCodec).getOrThrow();

        writeToPath(jsonElement, getLayerPath(shaderLayer)).whenComplete((a,b) -> onComplete.run());
    }

    private static CompletableFuture<?> writeToPath(@Nullable JsonElement json, Path path) {
        //see DataProvider.writeCodecToPath
        return CompletableFuture.runAsync(() -> {
            try {
                if (json == null) {
                    File file = path.toFile();
                    if (file.exists() && !file.delete()) {
                        SouperSecretSettingsClient.log("failed to delete file", path);
                    }
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
            } catch (IOException var10) {
                SouperSecretSettingsClient.log("Failed to save file to", path, var10);
            }

        }, Util.getMainWorkerExecutor().named("saveStable"));
    }

    public Path getLayerPath(ShaderLayer shaderLayer) {
        return configDir.resolve("layers").resolve(shaderLayer.name+".json");
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

    public boolean isValidName(String name) {
        return name != null && !name.isBlank() && name.matches("^[A-Za-z0-9._ ]{1,255}$");
    }
}
