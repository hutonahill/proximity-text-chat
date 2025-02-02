package com.proxtextchat;

import com.proxtextchat.ModMenu.ClothConfigCompat;
import com.proxtextchat.ModMenu.ClothConfigCompatBase;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;


import java.util.ServiceLoader;

/**
 * main class for the client.
 */
public class ProxChatBaseModClient implements ClientModInitializer {

	public static final String MOD_ID = "proxchatbasemod";

	@Override
	public void onInitializeClient() {

	}

	public static Config config;


	public static MutableComponent translatable(String path) {
		return Text.translatable(MOD_ID + "." + path);
	}

	public static Screen getConfigScreen(Screen parent) {
		if (isModLoaded("cloth_config") || isModLoaded("cloth-config")) {
			ServiceLoader<ClothConfigCompatBase> loader = ServiceLoader.load(ClothConfigCompatBase.class);
			if (loader.findFirst().isEmpty()) {
				//huh??
				return null;
			}
			return loader.findFirst().get().getConfigScreen(parent);
		} else return null;
	}


	public static boolean isModLoaded(String id) {
		return FabricLoader.getInstance().isModLoaded(id);
	}
}