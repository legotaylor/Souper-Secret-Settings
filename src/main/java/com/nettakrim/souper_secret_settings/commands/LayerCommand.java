package com.nettakrim.souper_secret_settings.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.actions.LayerRenameAction;
import com.nettakrim.souper_secret_settings.actions.ListAddAction;
import com.nettakrim.souper_secret_settings.actions.ShaderLoadAction;
import com.nettakrim.souper_secret_settings.actions.ToggleAction;
import com.nettakrim.souper_secret_settings.shaders.ShaderLayer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class LayerCommand extends ListCommand<ShaderLayer> {
    String saveConfirm = null;

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
                                .suggests((context, builder) -> CompletableFuture.completedFuture(builder.suggest(SouperSecretSettingsClient.soupRenderer.activeLayer.name).build()))
                                .executes(context -> setName(StringArgumentType.getString(context, "name")))
                )
                .executes(context -> queryName())
                .build();
        commandNode.addChild(nameNode);

        LiteralCommandNode<FabricClientCommandSource> toggleNode = ClientCommandManager
                .literal("toggle")
                .executes(context -> toggle())
                .build();
        commandNode.addChild(toggleNode);

        LiteralCommandNode<FabricClientCommandSource> saveNode = ClientCommandManager
                .literal("save")
                .then(
                        ClientCommandManager.argument("name", StringArgumentType.string())
                                .suggests(files)
                                .executes(context -> save(StringArgumentType.getString(context, "name")))
                )
                .build();
        commandNode.addChild(saveNode);

        LiteralCommandNode<FabricClientCommandSource> loadNode = ClientCommandManager
                .literal("load")
                .then(
                        ClientCommandManager.argument("name", StringArgumentType.string())
                                .suggests(files)
                                .executes(context -> load(StringArgumentType.getString(context, "name")))
                )
                .build();
        commandNode.addChild(loadNode);

        LiteralCommandNode<FabricClientCommandSource> infoNode = ClientCommandManager
                .literal("info")
                .executes(context -> info())
                .build();
        commandNode.addChild(infoNode);

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
            SouperSecretSettingsClient.say("layer.active.set", 0, layer.name);
            return 1;
        }
        return 0;
    }

    private int queryActive() {
        SouperSecretSettingsClient.say("layer.active.query", 1, SouperSecretSettingsClient.soupRenderer.activeLayer.name);
        return 1;
    }

    private int setName(String name) {
        new LayerRenameAction(SouperSecretSettingsClient.soupRenderer.activeLayer).addToHistory();
        SouperSecretSettingsClient.soupRenderer.activeLayer.name = name;
        SouperSecretSettingsClient.soupRenderer.activeLayer.disambiguateName();
        SouperSecretSettingsClient.say("layer.name.set", 0, SouperSecretSettingsClient.soupRenderer.activeLayer.name);
        return 1;
    }

    private int queryName() {
        SouperSecretSettingsClient.say("layer.name.query", 1, SouperSecretSettingsClient.soupRenderer.activeLayer.name);
        return 1;
    }

    private int toggle() {
        new ToggleAction(SouperSecretSettingsClient.soupRenderer.activeLayer).addToHistory();
        SouperSecretSettingsClient.soupRenderer.activeLayer.toggle();
        return 1;
    }

    private int save(String name) {
        SouperSecretSettingsClient.log(saveConfirm, name, name.equals(saveConfirm));
        if (name.equals(saveConfirm)) {
            ShaderLayer layer = SouperSecretSettingsClient.soupRenderer.activeLayer;
            String nameTemp = layer.name;
            layer.name = name;

            SouperSecretSettingsClient.soupData.saveLayer(layer, () -> SouperSecretSettingsClient.say("layer.save", 1, name));

            layer.name = nameTemp;
            saveConfirm = null;
        } else {
            SouperSecretSettingsClient.say("layer.save.prompt", 1, name);
            saveConfirm = name;
        }
        return 1;
    }

    private int load(String name) {
        saveConfirm = null;

        ShaderLayer layer = SouperSecretSettingsClient.soupRenderer.activeLayer;
        String nameTemp = layer.name;
        layer.name = name;

        new ShaderLoadAction(layer).addToHistory();
        layer.clear();
        SouperSecretSettingsClient.soupData.loadLayer(layer);

        layer.name = nameTemp;
        return 1;
    }

    private int info() {
        SouperSecretSettingsClient.sayStyled(Text.translatable(SouperSecretSettingsClient.MODID+".layer.info.name", SouperSecretSettingsClient.soupRenderer.activeLayer.name), 1);
        for (MutableText text : SouperSecretSettingsClient.soupRenderer.activeLayer.getInfo()) {
            SouperSecretSettingsClient.sayRaw(text.setStyle(Style.EMPTY.withColor(SouperSecretSettingsClient.textColor)), 1);
        }
        return 1;
    }

    private static final SuggestionProvider<FabricClientCommandSource> files = (context, builder) -> {
        for (String name : SouperSecretSettingsClient.soupData.getSavedLayers()) {
            builder.suggest(name);
        }

        return CompletableFuture.completedFuture(builder.build());
    };

    private static final SuggestionProvider<FabricClientCommandSource> layerIndexes = SouperSecretSettingsCommands.createIndexSuggestion(
            (context) -> SouperSecretSettingsClient.soupRenderer.shaderLayers,
            (message) -> Text.literal(message.name)
    );

    @Override
    List<ShaderLayer> getList() {
        return SouperSecretSettingsClient.soupRenderer.shaderLayers;
    }

    @Override
    SuggestionProvider<FabricClientCommandSource> getIndexSuggestions() {
        return layerIndexes;
    }
}
