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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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
        DataProvider.writeCodecToPath(DataWriter.UNCACHED, LayerCodecs.CODEC, LayerCodecs.from(shaderLayer), getLayerPath(shaderLayer)).whenComplete((a,b) -> onComplete.run());
    }

    public Path getLayerPath(ShaderLayer shaderLayer) {
        return configDir.resolve("layers").resolve(shaderLayer.name+".json");
    }
}
