package com.nettakrim.souper_secret_settings.commands;

import com.mclegoman.luminance.client.shaders.ShaderRegistryEntry;
import com.mclegoman.luminance.client.shaders.Shaders;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.actions.ListAddAction;
import com.nettakrim.souper_secret_settings.shaders.ShaderData;
import com.nettakrim.souper_secret_settings.shaders.ShaderLayer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ShaderListCommand extends ListCommand<ShaderData> {
    protected final String name;
    protected final Identifier registry;
    protected final SuggestionProvider<FabricClientCommandSource> registrySuggestions;

    public ShaderListCommand(String name, Identifier registry) {
        this.name = name;
        this.registry = registry;

        this.registrySuggestions = getRegistrySuggestions(registry);
    }

    public void register(RootCommandNode<FabricClientCommandSource> root) {
        LiteralCommandNode<FabricClientCommandSource> commandNode = ClientCommandManager.literal("soup:"+name).build();
        root.addChild(commandNode);

        LiteralCommandNode<FabricClientCommandSource> addNode = ClientCommandManager
                .literal("add")
                .then(
                        ClientCommandManager.argument("shader", IdentifierArgumentType.identifier())
                                .suggests(registrySuggestions)
                                .executes((context -> add(context.getArgument("shader", Identifier.class), 1)))
                                .then(
                                        ClientCommandManager.argument("amount", IntegerArgumentType.integer(1))
                                                .executes((context -> add(context.getArgument("shader", Identifier.class), IntegerArgumentType.getInteger(context, "amount"))))
                                )
                )
                .build();

        commandNode.addChild(addNode);

        registerList(commandNode);
    }

    public int add(Identifier id, int amount) {
        ShaderLayer layer = SouperSecretSettingsClient.soupRenderer.activeLayer;

        List<ShaderData> shaders = SouperSecretSettingsClient.soupRenderer.getShaderAdditions(registry, id, amount, layer);
        if (shaders == null) {
            return -1;
        }

        List<ShaderData> list = layer.getList(registry);
        for (ShaderData shaderData : shaders) {
            new ListAddAction<>(list, shaderData).addToHistory();
        }
        list.addAll(shaders);
        return 1;
    }

    private static SuggestionProvider<FabricClientCommandSource> getRegistrySuggestions(Identifier registry) {
        return (context, builder) -> {
            List<ShaderRegistryEntry> registryEntries = Shaders.getRegistry(registry);
            for (ShaderRegistryEntry shaderRegistry : registryEntries) {
                builder.suggest(shaderRegistry.getID().toString());
            }
            if (registryEntries.size() >= 2) builder.suggest("random");

            Map<String, List<ShaderRegistryEntry>> registryGroups = SouperSecretSettingsClient.soupRenderer.shaderGroups.get(registry);
            if (registryGroups != null) {
                for (String s : registryGroups.keySet()) {
                    builder.suggest("random_"+s);
                }
            }

            return CompletableFuture.completedFuture(builder.build());
        };
    }

    @Override
    List<ShaderData> getList() {
        return SouperSecretSettingsClient.soupRenderer.activeLayer.getList(registry);
    }
}
