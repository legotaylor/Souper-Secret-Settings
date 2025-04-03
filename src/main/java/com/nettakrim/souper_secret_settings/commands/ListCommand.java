package com.nettakrim.souper_secret_settings.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.nettakrim.souper_secret_settings.actions.ListRemoveAction;
import com.nettakrim.souper_secret_settings.actions.ListShiftAction;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class ListCommand<T> {
    public void registerList(LiteralCommandNode<FabricClientCommandSource> commandNode) {
        LiteralCommandNode<FabricClientCommandSource> removeNode = ClientCommandManager
                .literal("remove")
                .build();

        LiteralCommandNode<FabricClientCommandSource> clearNode = ClientCommandManager
                .literal("all")
                .executes(context -> removeAll(null))
                .then(
                        ClientCommandManager.argument("id", StringArgumentType.greedyString())
                                .suggests(listSuggestions)
                                .executes(context -> removeAll(StringArgumentType.getString(context, "id")))
                )
                .build();

        LiteralCommandNode<FabricClientCommandSource> topNode = ClientCommandManager
                .literal("top")
                .executes(context -> removeTop(1, null))
                .then(
                        ClientCommandManager.argument("amount", IntegerArgumentType.integer(1))
                                .executes(context -> removeTop(IntegerArgumentType.getInteger(context, "amount"), null))
                                .then(
                                        ClientCommandManager.argument("id", StringArgumentType.greedyString())
                                                .suggests(listSuggestions)
                                                .executes(context -> removeTop(IntegerArgumentType.getInteger(context, "amount"), StringArgumentType.getString(context, "id")))
                                )
                )
                .build();

        LiteralCommandNode<FabricClientCommandSource> firstNode = ClientCommandManager
                .literal("first")
                .executes(context -> removeFirst(1, null))
                .then(
                        ClientCommandManager.argument("amount", IntegerArgumentType.integer(1))
                                .executes(context -> removeFirst(IntegerArgumentType.getInteger(context, "amount"), null))
                                .then(
                                        ClientCommandManager.argument("id", StringArgumentType.greedyString())
                                                .suggests(listSuggestions)
                                                .executes(context -> removeFirst(IntegerArgumentType.getInteger(context, "amount"), StringArgumentType.getString(context, "id")))
                                )
                )
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
        removeNode.addChild(firstNode);
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

    public int removeAll(@Nullable String filter) {
        List<T> list = getList();
        for (int i = list.size()-1; i >= 0; i--) {
            if (filter != null) {
                T value = list.get(i);
                if (filterDenies(value, filter)) {
                    continue;
                }
            }

            new ListRemoveAction<>(list, i).addToHistory();
            list.remove(i);
        }
        onRemove();
        return 1;
    }

    public int removeTop(int amount, @Nullable String filter) {
        List<T> list = getList();
        if (list.isEmpty()) {
            return -1;
        }

        int index = list.size() - 1;
        while (index >= 0 && amount > 0) {
            if (filter != null) {
                T value = list.get(index);
                if (filterDenies(value, filter)) {
                    index--;
                    continue;
                }
            }

            new ListRemoveAction<>(list, index).addToHistory();
            list.remove(index);

            index--;
            amount--;
        }

        onRemove();
        return 1;
    }

    public int removeFirst(int amount, @Nullable String filter) {
        List<T> list = getList();
        if (list.isEmpty()) {
            return -1;
        }

        int index = 0;
        while (index < list.size() && amount > 0) {
            if (filter != null) {
                T value = list.get(index);
                if (filterDenies(value, filter)) {
                    index++;
                    continue;
                }
            }

            new ListRemoveAction<>(list, index).addToHistory();
            list.remove(index);

            amount--;
        }

        onRemove();
        return 1;
    }

    protected boolean filterDenies(T value, String filter) {
        String id = getID(value);
        boolean starts = filter.startsWith("*");
        boolean ends = filter.endsWith("*");
        filter = filter.substring(starts ? 1 : 0, ends ? filter.length()-1 : filter.length());
        if (starts) {
            if (ends) {
                return !id.contains(filter);
            }
            return !id.endsWith(filter);
        }
        if (ends) {
            return !id.startsWith(filter);
        }
        return !id.equals(filter);
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

    protected SuggestionProvider<FabricClientCommandSource> listSuggestions = (context, builder) -> {
        List<T> list = getList();
        for (T value : list) {
            builder.suggest(getID(value));
        }
        return CompletableFuture.completedFuture(builder.build());
    };

    abstract String getID(T value);

    abstract SuggestionProvider<FabricClientCommandSource> getIndexSuggestions();
}
