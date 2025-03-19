package com.nettakrim.souper_secret_settings.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.nettakrim.souper_secret_settings.actions.ListRemoveAction;
import com.nettakrim.souper_secret_settings.actions.ListShiftAction;
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

        LiteralCommandNode<FabricClientCommandSource> indexNode = ClientCommandManager
                .literal("index")
                .then(
                        ClientCommandManager.argument("index", IntegerArgumentType.integer(0))
                                .suggests(getIndexSuggestions())
                                .executes(context -> removeIndex(IntegerArgumentType.getInteger(context, "index")))
                )
                .build();

        commandNode.addChild(removeNode);
        removeNode.addChild(clearNode);
        removeNode.addChild(topNode);
        removeNode.addChild(indexNode);

        LiteralCommandNode<FabricClientCommandSource> shiftNode = ClientCommandManager
                .literal("shift")
                .then(
                        ClientCommandManager.argument("index", IntegerArgumentType.integer(0))
                                .suggests(getIndexSuggestions())
                                .then(
                                        ClientCommandManager.argument("destination", IntegerArgumentType.integer(0))
                                                .suggests(getIndexSuggestions())
                                                .executes(context -> shift(IntegerArgumentType.getInteger(context, "index"), IntegerArgumentType.getInteger(context, "destination")))
                                )
                )
                .build();
        commandNode.addChild(shiftNode);
    }

    public int removeAll() {
        List<T> list = getList();
        for (int i = list.size()-1; i >= 0; i--) {
            new ListRemoveAction<>(list, i).addToHistory();
        }
        list.clear();
        onRemove();
        return 1;
    }

    public int removeTop() {
        List<T> list = getList();
        if (list.isEmpty()) {
            return -1;
        }
        new ListRemoveAction<>(list, list.size()-1).addToHistory();
        list.removeLast();
        onRemove();
        return 1;
    }

    public int removeIndex(int index) {
        List<T> list = getList();
        if (list.isEmpty() || index >= list.size()) {
            return -1;
        }

        new ListRemoveAction<>(list, index).addToHistory();
        list.remove(index);
        onRemove();
        return 1;
    }

    protected void onRemove() {}

    public int shift(int index, int destination) {
        List<T> list = getList();
        if (index >= list.size() || destination >= list.size()) {
            return 0;
        }

        new ListShiftAction<>(list, index, destination-index).addToHistory();
        T value = list.remove(index);
        list.add(destination, value);
        return 1;
    }

    abstract List<T> getList();

    abstract SuggestionProvider<FabricClientCommandSource> getIndexSuggestions();
}
