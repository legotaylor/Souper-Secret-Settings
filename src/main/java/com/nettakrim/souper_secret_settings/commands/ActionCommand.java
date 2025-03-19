package com.nettakrim.souper_secret_settings.commands;

import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public class ActionCommand {
    public static void register(RootCommandNode<FabricClientCommandSource> root) {
        LiteralCommandNode<FabricClientCommandSource> undoNode = ClientCommandManager.literal("soup:undo").executes(context -> undo()).build();
        root.addChild(undoNode);

        LiteralCommandNode<FabricClientCommandSource> redoNode = ClientCommandManager.literal("soup:redo").executes(context -> redo()).build();
        root.addChild(redoNode);
    }

    static int undo() {
        SouperSecretSettingsClient.actions.undo();
        return 1;
    }

    static int redo() {
        SouperSecretSettingsClient.actions.redo();
        return 1;
    }
}
