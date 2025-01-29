package com.nettakrim.souper_secret_settings.commands;

import com.mclegoman.luminance.client.shaders.ShaderRegistryEntry;
import com.mclegoman.luminance.client.shaders.Shaders;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.shaders.ShaderData;
import com.nettakrim.souper_secret_settings.shaders.ShaderStack;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class ShaderListCommand {
    protected final String name;
    protected final Identifier registry;
    protected final Function<ShaderStack, List<ShaderData>> list;
    protected final SuggestionProvider<FabricClientCommandSource> suggestionProvider;

    public ShaderListCommand(String name, Identifier registry, Function<ShaderStack, List<ShaderData>> list) {
        this.name = name;
        this.registry = registry;
        this.list = list;

        this.suggestionProvider = getRegistrySuggestions(registry);
    }

    public void register(RootCommandNode<FabricClientCommandSource> root) {
        LiteralCommandNode<FabricClientCommandSource> commandNode = ClientCommandManager.literal("soup:"+name).build();
        root.addChild(commandNode);

        LiteralCommandNode<FabricClientCommandSource> addNode = ClientCommandManager
                .literal("add")
                .then(
                        ClientCommandManager.argument("shader", IdentifierArgumentType.identifier())
                                .suggests(suggestionProvider)
                                .executes((context -> add(context.getArgument("shader", Identifier.class), 1)))
                                .then(
                                        ClientCommandManager.argument("amount", IntegerArgumentType.integer(1))
                                                .executes((context -> add(context.getArgument("shader", Identifier.class), IntegerArgumentType.getInteger(context, "amount"))))
                                )
                )
                .build();

        commandNode.addChild(addNode);



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

    public int add(Identifier id, int amount) {
        ShaderStack stack = SouperSecretSettingsClient.soupRenderer.getActiveStack();

        List<ShaderData> shaders = SouperSecretSettingsClient.soupRenderer.getShaderAdditions(registry, id, amount, stack);
        if (shaders == null) {
            return -1;
        }

        list.apply(stack).addAll(shaders);
        return 1;
    }

    public int removeAll() {
        ShaderStack stack = SouperSecretSettingsClient.soupRenderer.getActiveStack();
        list.apply(stack).clear();
        return 1;
    }

    public int removeTop() {
        ShaderStack stack = SouperSecretSettingsClient.soupRenderer.getActiveStack();
        List<ShaderData> shaders = list.apply(stack);
        if (shaders.isEmpty()) {
            return -1;
        }
        shaders.removeLast();
        return 1;
    }

    private static SuggestionProvider<FabricClientCommandSource> getRegistrySuggestions(Identifier identifier) {
        return (context, builder) -> {
            for (ShaderRegistryEntry shaderRegistry : Shaders.getRegistry(identifier)) {
                builder.suggest(shaderRegistry.getID().toString());
            }
            if (Shaders.getShaderAmount(identifier) > 1) builder.suggest("random");
            return CompletableFuture.completedFuture(builder.build());
        };
    }
}
