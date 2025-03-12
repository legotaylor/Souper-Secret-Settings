package com.nettakrim.souper_secret_settings.commands;

import com.mclegoman.luminance.client.shaders.Shaders;
import com.mojang.brigadier.tree.RootCommandNode;

import com.nettakrim.souper_secret_settings.shaders.SoupRenderer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public class SouperSecretSettingsCommands {
    public static LayerCommand layerCommand;
    public static ShaderListCommand shaderCommand;
    public static ShaderListCommand modifierCommand;
    public static ParameterCommand parameterCommand;

    public static void initialize() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            RootCommandNode<FabricClientCommandSource> root = dispatcher.getRoot();

            new GuiCommand().register(root);
            new OptionCommand().register(root, registryAccess);

            layerCommand = new LayerCommand();
            shaderCommand = new ShaderListCommand("shader", Shaders.getMainRegistryId());
            modifierCommand = new ShaderListCommand("modifier", SoupRenderer.modifierRegistry);
            parameterCommand = new ParameterCommand();

            layerCommand.register(root);
            shaderCommand.register(root);
            modifierCommand.register(root);
            parameterCommand.register(root);
        });
    }
}
