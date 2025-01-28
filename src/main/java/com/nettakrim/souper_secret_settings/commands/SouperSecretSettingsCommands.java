package com.nettakrim.souper_secret_settings.commands;

import java.util.concurrent.CompletableFuture;

import com.mclegoman.luminance.client.shaders.ShaderRegistryEntry;
import com.mclegoman.luminance.client.shaders.Shaders;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.RootCommandNode;

import com.nettakrim.souper_secret_settings.shaders.SoupRenderer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.util.Identifier;

public class SouperSecretSettingsCommands {
    public static final SuggestionProvider<FabricClientCommandSource> postShaders = getRegistrySuggestions(Shaders.getMainRegistryId());

    public static final SuggestionProvider<FabricClientCommandSource> layerEffects = getRegistrySuggestions(SoupRenderer.layerEffectRegistry);

    private static SuggestionProvider<FabricClientCommandSource> getRegistrySuggestions(Identifier identifier) {
        return (context, builder) -> {
            for (ShaderRegistryEntry shaderRegistry : Shaders.getRegistry(identifier)) {
                builder.suggest(shaderRegistry.getID().toString());
            }
            if (Shaders.getShaderAmount(identifier) > 1) builder.suggest("random");
            return CompletableFuture.completedFuture(builder.build());
        };
    }



    public static void initialize() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            RootCommandNode<FabricClientCommandSource> root = dispatcher.getRoot();

            root.addChild(AddCommand.getCommandNode());
            root.addChild(ClearCommand.getCommandNode());
            root.addChild(LayerEffectCommand.getCommandNode());
        });
    }
}
