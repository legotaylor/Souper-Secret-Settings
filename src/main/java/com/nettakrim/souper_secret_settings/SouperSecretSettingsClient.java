package com.nettakrim.souper_secret_settings;

import com.mclegoman.luminance.client.data.ClientData;
import com.nettakrim.souper_secret_settings.actions.Actions;
import com.nettakrim.souper_secret_settings.data.SoupData;
import com.nettakrim.souper_secret_settings.gui.SoupGui;
import com.nettakrim.souper_secret_settings.shaders.SoupRenderer;
import com.nettakrim.souper_secret_settings.shaders.SoupUniforms;
import com.nettakrim.souper_secret_settings.shaders.calculations.Calculations;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.ItemStack;
import net.minecraft.text.*;
import net.minecraft.util.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nettakrim.souper_secret_settings.commands.SouperSecretSettingsCommands;

public class SouperSecretSettingsClient implements ClientModInitializer {
	public static final String MODID = "souper_secret_settings";
	private static final Logger LOGGER = LoggerFactory.getLogger(MODID);

	public static final TextColor textColor = TextColor.fromRgb(0xAAAAAA);
	public static final TextColor nameTextColor = TextColor.fromRgb(0xB6484C);

	public static SoupData soupData;
	public static SoupRenderer soupRenderer;
	public static SoupGui soupGui;
	public static Actions actions;

	@Override
	public void onInitializeClient() {
		soupData = new SoupData();
		soupRenderer = new SoupRenderer();
		soupGui = new SoupGui();
		actions = new Actions();

		ResourceManagerHelper.registerBuiltinResourcePack(Identifier.of("soup"), FabricLoader.getInstance().getModContainer(MODID).orElseThrow(), Text.literal("Extra Soup"), ResourcePackActivationType.DEFAULT_ENABLED);
		soupData.config.transferOldData();
		Keybinds.tick();

		SoupUniforms.register();
		Calculations.register();
		SouperSecretSettingsCommands.initialize();

		ClientTickEvents.END_CLIENT_TICK.register((client) -> {
			if (!ClientData.minecraft.isFinishedLoading()) {
				return;
			}

			Keybinds.tick();
			soupData.tick();
		});

		ClientLifecycleEvents.CLIENT_STOPPING.register((client) -> soupData.saveIfChanged());
	}

	public static void say(String key, int priority, Object... args) {
		sayStyled(translate(key, args).setStyle(Style.EMPTY.withColor(textColor)), priority);
	}

	public static void sayStyled(MutableText text, int priority) {
		sayRaw(Text.translatable(MODID + ".say").setStyle(Style.EMPTY.withColor(nameTextColor)).append(text.setStyle(Style.EMPTY.withColor(textColor))), priority);
	}

	public static void sayRaw(MutableText text, int priority) {
		if (ClientData.minecraft.player == null || priority < soupData.config.messageFilter) return;
		ClientData.minecraft.player.sendMessage(text, false);
	}

	public static MutableText translate(String key, Object... args) {
		return Text.translatable(MODID+"."+key, args);
	}

	public static void log(Object... args) {
		StringBuilder s = new StringBuilder();
		for (Object object : args) {
			if (!s.isEmpty()) {
				s.append(" ");
			}
			s.append(object);
		}
		LOGGER.info(s.toString());
	}

	public static void consumeItem(ItemStack stack) {
		if (stacksMatch(stack, soupData.config.randomItem)) {
			SouperSecretSettingsCommands.shaderCommand.removeAll();
			SouperSecretSettingsCommands.shaderCommand.add(Identifier.of("random_edible"), 1, true);
			RandomSound.play();
		} else if (stacksMatch(stack, soupData.config.clearItem)) {
			SouperSecretSettingsCommands.shaderCommand.removeAll();
		}
	}

	private static boolean stacksMatch(ItemStack stack, ItemStack reference) {
		return stack.isOf(reference.getItem()) && stack.getComponentChanges().toString().equals(reference.getComponentChanges().toString());
	}
}
