package com.nettakrim.souper_secret_settings.commands;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.command.argument.ItemStackArgumentType;

public class OptionCommand {
    public OptionCommand() {

    }

    public void register(RootCommandNode<FabricClientCommandSource> root, CommandRegistryAccess commandRegistryAccess) {
        LiteralCommandNode<FabricClientCommandSource> commandNode = ClientCommandManager.literal("soup:option").build();
        root.addChild(commandNode);

        LiteralCommandNode<FabricClientCommandSource> randomNode = ClientCommandManager
                .literal("random_item")
                .then(
                        ClientCommandManager.argument("item", ItemStackArgumentType.itemStack(commandRegistryAccess))
                                .executes(context -> setRandomItem(ItemStackArgumentType.getItemStackArgument(context, "item")))
                )
                .build();
        commandNode.addChild(randomNode);

        LiteralCommandNode<FabricClientCommandSource> clearNode = ClientCommandManager
                .literal("clear_item")
                .then(
                        ClientCommandManager.argument("item", ItemStackArgumentType.itemStack(commandRegistryAccess))
                                .executes(context -> setClearItem(ItemStackArgumentType.getItemStackArgument(context, "item")))
                )
                .build();
        commandNode.addChild(clearNode);
    }

    int setRandomItem(ItemStackArgument itemStack) throws CommandSyntaxException {
        SouperSecretSettingsClient.randomItem = itemStack.createStack(1, false);
        return 1;
    }

    int setClearItem(ItemStackArgument itemStack) throws CommandSyntaxException {
        SouperSecretSettingsClient.clearItem = itemStack.createStack(1, false);
        return 1;
    }

}
