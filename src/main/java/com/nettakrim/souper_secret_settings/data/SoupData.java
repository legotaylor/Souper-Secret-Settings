package com.nettakrim.souper_secret_settings.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.shaders.ShaderLayer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

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

        DataProvider.writeCodecToPath(DataWriter.UNCACHED, LayerCodecs.CODEC, LayerCodecs.from(shaderLayer), getLayerPath(shaderLayer)).whenComplete((a,b) -> onComplete.run());
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
