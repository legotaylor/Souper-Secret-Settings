package com.nettakrim.souper_secret_settings.commands;

import com.mclegoman.luminance.client.shaders.Shader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.item.ItemStack;

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
                .executes(context -> queryRandomItem())
                .build();
        commandNode.addChild(randomNode);

        LiteralCommandNode<FabricClientCommandSource> clearNode = ClientCommandManager
                .literal("clear_item")
                .then(
                        ClientCommandManager.argument("item", ItemStackArgumentType.itemStack(commandRegistryAccess))
                                .executes(context -> setClearItem(ItemStackArgumentType.getItemStackArgument(context, "item")))
                )
                .executes(context -> queryClearItem())
                .build();
        commandNode.addChild(clearNode);

        LiteralCommandNode<FabricClientCommandSource> renderTypeNode = ClientCommandManager
                .literal("render_type")
                .then(
                        ClientCommandManager.literal("world").executes(context -> setRenderType(Shader.RenderType.WORLD))
                )
                .then(
                        ClientCommandManager.literal("ui").executes(context -> setRenderType(Shader.RenderType.GAME))
                )
                .executes(context -> queryRenderType())
                .build();
        commandNode.addChild(renderTypeNode);

        LiteralCommandNode<FabricClientCommandSource> toggleNode = ClientCommandManager
                .literal("toggle")
                .then(
                        ClientCommandManager.literal("stay")
                                .executes(context -> toggle(true))
                )
                .executes(context -> toggle(false))
                .build();
        commandNode.addChild(toggleNode);

        LiteralCommandNode<FabricClientCommandSource> warningNode = ClientCommandManager
                .literal("warning")
                .then(
                        ClientCommandManager.literal("enable")
                                .executes(context -> warning(true))
                )
                .then(
                        ClientCommandManager.literal("disable")
                                .executes(context -> warning(false))
                )
                .build();
        commandNode.addChild(warningNode);
    }

    private int setRandomItem(ItemStackArgument itemStack) throws CommandSyntaxException {
        SouperSecretSettingsClient.soupData.config.randomItem = itemStack.createStack(1, false);
        sayItem("option.random.set", SouperSecretSettingsClient.soupData.config.randomItem);
        SouperSecretSettingsClient.soupData.saveConfig();
        return 1;
    }

    private int queryRandomItem() {
        sayItem("option.random.query", SouperSecretSettingsClient.soupData.config.randomItem);
        return 1;
    }

    private int setClearItem(ItemStackArgument itemStack) throws CommandSyntaxException {
        SouperSecretSettingsClient.soupData.config.clearItem = itemStack.createStack(1, false);
        sayItem("option.clear.set", SouperSecretSettingsClient.soupData.config.clearItem);
        SouperSecretSettingsClient.soupData.saveConfig();
        return 1;
    }

    private int queryClearItem() {
        sayItem("option.clear.query", SouperSecretSettingsClient.soupData.config.clearItem);
        return 1;
    }

    private void sayItem(String key, ItemStack itemStack) {
        String s = itemStack.getItem().toString();
        if (!itemStack.getComponentChanges().isEmpty()) {
            s += " "+itemStack.getComponentChanges().toString();
        }
        SouperSecretSettingsClient.say(key, s);
    }

    private int setRenderType(Shader.RenderType renderType) {
        SouperSecretSettingsClient.soupRenderer.setRenderType(renderType);
        return queryRenderType();
    }

    private int queryRenderType() {
        SouperSecretSettingsClient.say("option.render_type."+SouperSecretSettingsClient.soupRenderer.getRenderType().toString().toLowerCase());
        return 1;
    }

    private int toggle(boolean stay) {
        SouperSecretSettingsClient.soupData.config.disableState = stay ? 2 : SouperSecretSettingsClient.soupData.config.disableState > 0 ? 0 : 1;
        SouperSecretSettingsClient.say("option.toggle."+SouperSecretSettingsClient.soupData.config.disableState);
        SouperSecretSettingsClient.soupData.saveConfig();
        return 1;
    }

    private int warning(boolean state) {
        SouperSecretSettingsClient.soupData.config.warning = state;
        SouperSecretSettingsClient.say("option.warning."+(state ? "on" : "off"));
        SouperSecretSettingsClient.soupData.saveConfig();
        return 1;
    }
}
