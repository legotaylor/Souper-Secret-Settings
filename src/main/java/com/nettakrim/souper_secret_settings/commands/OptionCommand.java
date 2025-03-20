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
    public static void register(RootCommandNode<FabricClientCommandSource> root, CommandRegistryAccess commandRegistryAccess) {
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
                .executes(context -> queryRenderType(1))
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
                .executes(context -> warningQuery(1))
                .build();
        commandNode.addChild(warningNode);

        LiteralCommandNode<FabricClientCommandSource> filterNode = ClientCommandManager
                .literal("message_filter")
                .then(
                        ClientCommandManager.literal("all")
                                .executes(context -> setMessageFilter(0))
                )
                .then(
                        ClientCommandManager.literal("important")
                                .executes(context -> setMessageFilter(1))
                )
                .then(
                        ClientCommandManager.literal("none")
                                .executes(context -> setMessageFilter(2))
                )
                .executes(context -> queryMessageFilter(1))
                .build();
        commandNode.addChild(filterNode);
    }

    public static int setRandomItem(ItemStackArgument itemStack) throws CommandSyntaxException {
        SouperSecretSettingsClient.soupData.config.randomItem = itemStack.createStack(1, false);
        sayItem("option.random.set", SouperSecretSettingsClient.soupData.config.randomItem, 0);
        SouperSecretSettingsClient.soupData.changeConfig();
        return 1;
    }

    public static int queryRandomItem() {
        sayItem("option.random.query", SouperSecretSettingsClient.soupData.config.randomItem, 1);
        return 1;
    }

    public static int setClearItem(ItemStackArgument itemStack) throws CommandSyntaxException {
        SouperSecretSettingsClient.soupData.config.clearItem = itemStack.createStack(1, false);
        sayItem("option.clear.set", SouperSecretSettingsClient.soupData.config.clearItem, 0);
        SouperSecretSettingsClient.soupData.changeConfig();
        return 1;
    }

    public static int queryClearItem() {
        sayItem("option.clear.query", SouperSecretSettingsClient.soupData.config.clearItem, 1);
        return 1;
    }

    public static void sayItem(String key, ItemStack itemStack, int priority) {
        String s = itemStack.getItem().toString();
        if (!itemStack.getComponentChanges().isEmpty()) {
            s += " "+itemStack.getComponentChanges().toString();
        }
        SouperSecretSettingsClient.say(key, priority, s);
    }

    public static int setRenderType(Shader.RenderType renderType) {
        SouperSecretSettingsClient.soupRenderer.setRenderType(renderType);
        return queryRenderType(0);
    }

    public static int queryRenderType(int priority) {
        SouperSecretSettingsClient.say("option.render_type."+SouperSecretSettingsClient.soupRenderer.getRenderType().toString().toLowerCase(), priority);
        return 1;
    }

    public static int toggle(boolean stay) {
        SouperSecretSettingsClient.soupData.config.disableState = stay ? 2 : SouperSecretSettingsClient.soupData.config.disableState > 0 ? 0 : 1;
        SouperSecretSettingsClient.say("option.toggle."+SouperSecretSettingsClient.soupData.config.disableState, 0);
        SouperSecretSettingsClient.soupData.changeConfig();
        return 1;
    }

    public static int warning(boolean state) {
        SouperSecretSettingsClient.soupData.config.warning = state;
        SouperSecretSettingsClient.soupData.changeConfig();
        return warningQuery(0);
    }

    public static int warningQuery(int priority) {
        SouperSecretSettingsClient.say("option.warning."+(SouperSecretSettingsClient.soupData.config.warning ? "on" : "off"), priority);
        return 1;
    }

    public static int setMessageFilter(int to) {
        SouperSecretSettingsClient.soupData.config.messageFilter = to;
        return queryMessageFilter(2);
    }

    public static int queryMessageFilter(int priority) {
        SouperSecretSettingsClient.say("option.filter."+(SouperSecretSettingsClient.soupData.config.messageFilter), priority);
        return 1;
    }
}
