package com.nettakrim.souper_secret_settings.commands;

import com.mojang.brigadier.tree.LiteralCommandNode;
import com.nettakrim.souper_secret_settings.actions.ListRemoveAction;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import java.util.List;

public abstract class ListCommand<T> {
    public void registerList(LiteralCommandNode<FabricClientCommandSource> commandNode) {
        LiteralCommandNode<FabricClientCommandSource> removeNode = ClientCommandManager
                .literal("remove")
                .build();

        LiteralCommandNode<FabricClientCommandSource> clearNode = ClientCommandManager
                .literal("all")
                .executes(context -> removeAll())
                .build();

        LiteralCommandNode<FabricClientCommandSource> topNode = ClientCommandManager
                .literal("top")
                .executes(context -> removeTop())
                .build();

        commandNode.addChild(removeNode);
        removeNode.addChild(clearNode);
        removeNode.addChild(topNode);
    }

    public int removeAll() {
        List<T> shaders = getList();
        for (int i = shaders.size()-1; i >= 0; i--) {
            new ListRemoveAction<>(shaders, i).addToHistory();
        }
        shaders.clear();
        return 1;
    }

    public int removeTop() {
        List<T> shaders = getList();
        if (shaders.isEmpty()) {
            return -1;
        }
        new ListRemoveAction<>(shaders, shaders.size()-1).addToHistory();
        shaders.removeLast();
        return 1;
    }

    abstract List<T> getList();
}
