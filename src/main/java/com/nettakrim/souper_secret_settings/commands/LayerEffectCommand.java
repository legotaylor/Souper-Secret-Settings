package com.nettakrim.souper_secret_settings.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.shaders.ShaderStack;
import com.nettakrim.souper_secret_settings.shaders.SoupRenderer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.util.Identifier;

public class LayerEffectCommand {
	//TODO: make layer effect commands and stack commands use the same object with different parameters (eg /soup:shader add, /soup:layer_effect add)
	public static LiteralCommandNode<FabricClientCommandSource> getCommandNode() {
		LiteralCommandNode<FabricClientCommandSource> addNode = ClientCommandManager
			.literal("soup:layer_effect")
			.then(
				ClientCommandManager.argument("shader", IdentifierArgumentType.identifier())
				.suggests(SouperSecretSettingsCommands.layerEffects)
				.executes((context -> add(context.getArgument("shader", Identifier.class), 1)))
				.then(
					ClientCommandManager.argument("amount", IntegerArgumentType.integer(1))
					.executes((context -> add(context.getArgument("shader", Identifier.class), IntegerArgumentType.getInteger(context, "amount"))))
				)
			)
			.build();

		return addNode;
	}

	public static int add(Identifier id, int amount) {
		ShaderStack stack = SouperSecretSettingsClient.soupRenderer.getActiveStack();
		return SouperSecretSettingsClient.soupRenderer.addShaders(SoupRenderer.layerEffectRegistry, id, amount, stack, stack::addLayerEffect) ? 1 : -1;
	}
}
