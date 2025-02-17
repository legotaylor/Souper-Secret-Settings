package com.nettakrim.souper_secret_settings.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class Config {
    public static final Codec<Config> CODEC = RecordCodecBuilder.create((instance) -> instance.group(Codec.BOOL.fieldOf("transferredOldData").forGetter((config) -> config.transferredOldData)).apply(instance, Config::new));

    boolean transferredOldData;

    public Config(boolean transferredOldData) {
        this.transferredOldData = transferredOldData;
    }

    public static Config getDefaultConfig() {
        return new Config(false);
    }

    public void transferOldData() {
        if (transferredOldData) {
            return;
        }
        SouperSecretSettingsClient.log("transferring old data...");

        File data = FabricLoader.getInstance().getGameDir().resolve("souper_secret_settings.txt").toFile();
        if (data.exists()) {
            try {
                Scanner scanner = new Scanner(data);
                if (scanner.hasNextLine()) scanner.nextLine();

                while (scanner.hasNextLine()) {
                    String s = scanner.nextLine();
                    String[] sections = s.split(": ");
                    if (sections.length != 2) continue;
                    try {
                        transferRecipe(sections[0], sections[1]);
                    } catch (Exception e) {
                        SouperSecretSettingsClient.log("failed to transfer recipe", s);
                    }
                }

                scanner.close();
            } catch (IOException e) {
                SouperSecretSettingsClient.log("failed to transfer old data", e);
            }
        }

        transferredOldData = true;
        SouperSecretSettingsClient.soupData.saveConfig();
    }

    private void transferRecipe(String name, String data) {
        Path path = SouperSecretSettingsClient.soupData.getLayerPath(name);
        if (path.toFile().exists()) {
            SouperSecretSettingsClient.log("layer", name, "already exists, skipping...");
            return;
        }

        String[] shaderIDs = data.split("/");

        List<LayerCodecs.Shader> shaders = new ArrayList<>();
        for (String id : shaderIDs) {
            int split;
            for (split = 0; split < id.length(); split++) {
                if (!Character.isDigit(id.charAt(split))) {
                    break;
                }
            }
            int count = Integer.parseInt(id.substring(0, split));
            id = id.substring(split);
            id = switch(id) {
                case "pixels" -> "pixelated";
                case "blur" -> "box_blur";
                default -> id;
            };
            LayerCodecs.Shader shader = new LayerCodecs.Shader(id, Optional.empty());
            for (int i = 0; i < count; i++) {
                shaders.add(shader);
            }
        }

        LayerCodecs layer = new LayerCodecs(Optional.of(shaders), Optional.empty(), Optional.empty());
        SoupData.saveToPath(LayerCodecs.CODEC, path, layer);
    }
}
