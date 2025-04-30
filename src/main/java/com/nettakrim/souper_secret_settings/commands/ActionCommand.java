package com.nettakrim.souper_secret_settings.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public class ActionCommand {
    public static void register(RootCommandNode<FabricClientCommandSource> root) {
        LiteralCommandNode<FabricClientCommandSource> undoNode = ClientCommandManager.literal("soup:undo")
                .then(
                        ClientCommandManager.argument("amount", IntegerArgumentType.integer(1))
                                .executes(context -> undo(IntegerArgumentType.getInteger(context, "amount")))
                )
                .executes(context -> undo(1))
                .build();
        root.addChild(undoNode);

        LiteralCommandNode<FabricClientCommandSource> redoNode = ClientCommandManager.literal("soup:redo")
                .then(
                        ClientCommandManager.argument("amount", IntegerArgumentType.integer(1))
                                .executes(context -> redo(IntegerArgumentType.getInteger(context, "amount")))
                )
                .executes(context -> redo(1))
                .build();
        root.addChild(redoNode);
    }

    static int undo(int count) {
        while (count > 0 && SouperSecretSettingsClient.actions.undo()) {
            count--;
        }
        return 1;
    }

    static int redo(int count) {
        while (count > 0 && SouperSecretSettingsClient.actions.redo()) {
            count--;
        }
        return 1;
    }
}
