package com.nettakrim.souper_secret_settings.commands;

import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;

public class GuiCommand {
    public GuiCommand() {

    }

    public void register(RootCommandNode<FabricClientCommandSource> root) {
        LiteralCommandNode<FabricClientCommandSource> commandNode = ClientCommandManager.literal("soup:gui").executes(context -> open()).build();
        root.addChild(commandNode);
    }

    int open() {
        // delay by a tick, since the chat screen is closed *after* the command is executed
        MinecraftClient.getInstance().send(() -> SouperSecretSettingsClient.soupGui.open(SouperSecretSettingsClient.soupGui.getCurrentScreenType()));
        return 1;
    }
}
