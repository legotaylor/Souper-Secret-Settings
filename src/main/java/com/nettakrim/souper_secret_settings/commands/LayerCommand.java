package com.nettakrim.souper_secret_settings.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.actions.LayerRenameAction;
import com.nettakrim.souper_secret_settings.actions.ListAddAction;
import com.nettakrim.souper_secret_settings.shaders.ShaderLayer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;

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

        LiteralCommandNode<FabricClientCommandSource> activeNode = ClientCommandManager
                .literal("active")
                .then(
                        ClientCommandManager.argument("index", IntegerArgumentType.integer(0))
                                .suggests(layerIndexes)
                                .executes(context -> setActive(IntegerArgumentType.getInteger(context, "index")))
                )
                .executes(context -> queryActive())
                .build();
        commandNode.addChild(activeNode);

        LiteralCommandNode<FabricClientCommandSource> nameNode = ClientCommandManager
                .literal("name")
                .then(
                        ClientCommandManager.argument("name", StringArgumentType.string())
                                .executes(context -> setName(StringArgumentType.getString(context, "name")))
                )
                .executes(context -> queryName())
                .build();
        commandNode.addChild(nameNode);

        registerList(commandNode);
    }

    private int add(String name) {
        ShaderLayer layer = new ShaderLayer(name);
        new ListAddAction<>(SouperSecretSettingsClient.soupRenderer.shaderLayers, layer).addToHistory();
        SouperSecretSettingsClient.soupRenderer.shaderLayers.add(layer);

        SouperSecretSettingsClient.soupRenderer.activeLayer = layer;
        return 1;
    }

    @Override
    protected void onRemove() {
        if (SouperSecretSettingsClient.soupRenderer.shaderLayers.isEmpty()) {
            SouperSecretSettingsClient.soupRenderer.clearAll();
            SouperSecretSettingsClient.soupRenderer.loadDefault();
        } else {
            SouperSecretSettingsClient.soupRenderer.activeLayer = SouperSecretSettingsClient.soupRenderer.shaderLayers.getLast();
        }
    }

    private int setActive(int index) {
        if (index < SouperSecretSettingsClient.soupRenderer.shaderLayers.size()) {
            ShaderLayer layer = SouperSecretSettingsClient.soupRenderer.shaderLayers.get(index);
            SouperSecretSettingsClient.soupRenderer.activeLayer = layer;
            SouperSecretSettingsClient.say("layer.active.set", layer.name);
            return 1;
        }
        return 0;
    }

    private int queryActive() {
        SouperSecretSettingsClient.say("layer.active.query", SouperSecretSettingsClient.soupRenderer.activeLayer.name);
        return 1;
    }

    private int setName(String name) {
        new LayerRenameAction(SouperSecretSettingsClient.soupRenderer.activeLayer).addToHistory();
        SouperSecretSettingsClient.soupRenderer.activeLayer.name = name;
        SouperSecretSettingsClient.soupRenderer.activeLayer.disambiguateName();
        SouperSecretSettingsClient.say("layer.name.set", SouperSecretSettingsClient.soupRenderer.activeLayer.name);
        return 1;
    }

    private int queryName() {
        SouperSecretSettingsClient.say("layer.name.query", SouperSecretSettingsClient.soupRenderer.activeLayer.name);
        return 1;
    }

    private static final SuggestionProvider<FabricClientCommandSource> files = (context, builder) -> {
        for (String name : SouperSecretSettingsClient.soupData.getSavedLayers()) {
            builder.suggest(name);
        }

        return CompletableFuture.completedFuture(builder.build());
    };

    private static final SuggestionProvider<FabricClientCommandSource> layerIndexes = (context, builder) -> {
        for (int i = 0; i < SouperSecretSettingsClient.soupRenderer.shaderLayers.size(); i++) {
            builder.suggest(i, Text.literal(SouperSecretSettingsClient.soupRenderer.shaderLayers.get(i).name));
        }

        return CompletableFuture.completedFuture(builder.build());
    };


    @Override
    List<ShaderLayer> getList() {
        return SouperSecretSettingsClient.soupRenderer.shaderLayers;
    }

    @Override
    SuggestionProvider<FabricClientCommandSource> getIndexSuggestions() {
        return layerIndexes;
    }
}
