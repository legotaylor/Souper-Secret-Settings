package com.nettakrim.souper_secret_settings;

import com.mclegoman.luminance.client.data.ClientData;
import com.nettakrim.souper_secret_settings.gui.SoupGui;
import com.nettakrim.souper_secret_settings.shaders.SoupRenderer;
import com.nettakrim.souper_secret_settings.shaders.SoupUniforms;
import com.nettakrim.souper_secret_settings.shaders.calculations.Calculations;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.*;
import net.minecraft.util.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nettakrim.souper_secret_settings.commands.SouperSecretSettingsCommands;

public class SouperSecretSettingsClient implements ClientModInitializer {
	public static final String MODID = "souper_secret_settings";
	private static final Logger LOGGER = LoggerFactory.getLogger("souper_secret_settings");

	private static final TextColor textColor = TextColor.fromRgb(0xAAAAAA);
	private static final TextColor nameTextColor = TextColor.fromRgb(0xB6484C);

	public static SoupRenderer soupRenderer;
	public static SoupGui soupGui;

	@Override
	public void onInitializeClient() {
		soupRenderer = new SoupRenderer();
		soupGui = new SoupGui();

		ResourceManagerHelper.registerBuiltinResourcePack(Identifier.of("soup"), FabricLoader.getInstance().getModContainer(MODID).orElseThrow(), Text.literal("Extra Soup"), ResourcePackActivationType.DEFAULT_ENABLED);

		SoupUniforms.register();
		Calculations.register();
		SouperSecretSettingsCommands.initialize();
		Keybinds.init();
	}

	public static void say(String key, Object... args) {
		sayText(translate(key, args).setStyle(Style.EMPTY.withColor(textColor)));
	}

	public static void sayText(MutableText text) {
		if (ClientData.minecraft.player == null) return;
		ClientData.minecraft.player.sendMessage(Text.translatable(MODID + ".say").setStyle(Style.EMPTY.withColor(nameTextColor)).append(text.setStyle(Style.EMPTY.withColor(textColor))), false);
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
}
