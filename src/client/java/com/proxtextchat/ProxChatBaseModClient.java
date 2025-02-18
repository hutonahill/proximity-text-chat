package com.proxtextchat;

import com.proxtextchat.ModMenu.ClothConfigCompat;
import com.proxtextchat.ModMenu.ClothConfigCompatBase;
import com.proxtextchat.ModMenu.Config;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.File;
import java.util.ServiceLoader;

/**
 * main class for the client.
 */
public class ProxChatBaseModClient implements ClientModInitializer {

	public static final String MOD_ID = "proxchatbasemod";

	public static Config config;
	public static File configFile;
	public static final Logger logger = LoggerFactory.getLogger("Proximity Text Chat");
	@Override
	public void onInitializeClient() {
		config = new Config(Config.DEFAULT_PLAYER_TO_PLAYER_CHAT_RANGE, Config.DEFAULT_CHAT_METHOD.getID().toString());
	}


	public static MutableText translatable(String path) {
		return Text.translatable(MOD_ID + "." + path);
	}

	public static Screen getConfigScreen(Screen parent) {
		ServiceLoader<ClothConfigCompatBase> loader = ServiceLoader.load(ClothConfigCompatBase.class);
		return loader.findFirst().get().getConfigScreen(parent);
		/*if (isModLoaded("cloth_config") || isModLoaded("cloth-config") || isModLoaded("cloth-config")) {
			ServiceLoader<ClothConfigCompatBase> loader = ServiceLoader.load(ClothConfigCompatBase.class);
			if (loader.findFirst().isEmpty()) {
				//huh??
				return null;
			}
			return loader.findFirst().get().getConfigScreen(parent);
		} else return null;*/
	}


	/**
	 * Checks if a mod is loaded
	 * @param id The id of the mod
	 * @return weather or not the mod is loaded
	 */
	public static boolean isModLoaded(String id) {
		return FabricLoader.getInstance().isModLoaded(id);
	}
}