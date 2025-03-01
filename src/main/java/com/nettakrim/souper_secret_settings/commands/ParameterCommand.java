package com.nettakrim.souper_secret_settings.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.actions.ListAddAction;
import com.nettakrim.souper_secret_settings.shaders.calculations.Calculation;
import com.nettakrim.souper_secret_settings.shaders.calculations.Calculations;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ParameterCommand extends ListCommand<Calculation> {
    public ParameterCommand() {

    }

    public void register(RootCommandNode<FabricClientCommandSource> root) {
        LiteralCommandNode<FabricClientCommandSource> commandNode = ClientCommandManager.literal("soup:parameter").build();
        root.addChild(commandNode);

        LiteralCommandNode<FabricClientCommandSource> addNode = ClientCommandManager
                .literal("add")
                .then(
                        ClientCommandManager.argument("id", StringArgumentType.string())
                                .suggests(parameterIDs)
                                .executes(context -> add(StringArgumentType.getString(context, "id")))
                )
                .build();

        commandNode.addChild(addNode);

        registerList(commandNode);
    }

    int add(String id) {
        Calculation calculation = Calculations.createCalcultion(id);
        if (calculation == null) {
            return -1;
        }

        new ListAddAction<>(SouperSecretSettingsClient.soupRenderer.activeLayer.calculations, calculation).addToHistory();
        SouperSecretSettingsClient.soupRenderer.activeLayer.calculations.add(calculation);
        return 1;
    }

    static SuggestionProvider<FabricClientCommandSource> parameterIDs = (context, builder) -> {
        for (String id : Calculations.getIds()) {
            builder.suggest(id);
        }

        return CompletableFuture.completedFuture(builder.build());
    };

    @Override
    List<Calculation> getList() {
        return SouperSecretSettingsClient.soupRenderer.activeLayer.calculations;
    }
}
