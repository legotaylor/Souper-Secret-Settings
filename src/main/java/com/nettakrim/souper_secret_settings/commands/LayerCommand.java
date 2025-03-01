package com.nettakrim.souper_secret_settings.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.actions.ListAddAction;
import com.nettakrim.souper_secret_settings.shaders.ShaderLayer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class LayerCommand extends ListCommand<ShaderLayer> {
    public LayerCommand() {

    }

    public void register(RootCommandNode<FabricClientCommandSource> root) {
        LiteralCommandNode<FabricClientCommandSource> commandNode = ClientCommandManager.literal("soup:layer").build();
        root.addChild(commandNode);

        LiteralCommandNode<FabricClientCommandSource> addNode = ClientCommandManager
                .literal("add")
                .then(
                        ClientCommandManager.argument("name", StringArgumentType.string())
                                .suggests(files)
                                .executes(context -> add(StringArgumentType.getString(context, "name")))
                )
                .build();

        commandNode.addChild(addNode);

        registerList(commandNode);
    }

    int add(String name) {
        ShaderLayer layer = new ShaderLayer(name);
        new ListAddAction<>(SouperSecretSettingsClient.soupRenderer.shaderLayers, layer).addToHistory();
        SouperSecretSettingsClient.soupRenderer.shaderLayers.add(layer);

        SouperSecretSettingsClient.soupRenderer.activeLayer = layer;
        return 1;
    }

    @Override
    public int removeAll() {
        int i = super.removeAll();
        onRemove();
        return i;
    }

    @Override
    public int removeTop() {
        int i = super.removeTop();
        onRemove();
        return i;
    }

    private void onRemove() {
        if (SouperSecretSettingsClient.soupRenderer.shaderLayers.isEmpty()) {
            SouperSecretSettingsClient.soupRenderer.clearAll();
            SouperSecretSettingsClient.soupRenderer.loadDefault();
        } else {
            SouperSecretSettingsClient.soupRenderer.activeLayer = SouperSecretSettingsClient.soupRenderer.shaderLayers.getLast();
        }
    }

    static SuggestionProvider<FabricClientCommandSource> files = (context, builder) -> {
        for (String name : SouperSecretSettingsClient.soupData.getSavedLayers()) {
            builder.suggest(name);
        }

        return CompletableFuture.completedFuture(builder.build());
    };

    @Override
    List<ShaderLayer> getList() {
        return SouperSecretSettingsClient.soupRenderer.shaderLayers;
    }
}
