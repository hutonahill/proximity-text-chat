package com.proxtextchat;


import com.proxtextchat.ModMenu.ClothConfigCompatBase;
import com.proxtextchat.ModMenu.Config;
import com.proxtextchat.ModMenu.ErrorScreens.ClothConfigScreenError;
import com.proxtextchat.ModMenu.ErrorScreens.ConfigLoadError;
import com.proxtextchat.ModMenu.ErrorScreens.MissingClothConfigScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.File;
import java.util.Optional;
import java.util.ServiceLoader;

/**
 * main class for the client.
 */
public class ProxChatBaseModClient implements ClientModInitializer {

	/**
	 * The unique identifier used to identify this mod to Fabric and Minecraft.
	 */
	public static final String MOD_ID = "proxchatbasemod";

	/**
	 * The active client configuration.
	 * Initialized during client startup.
	 */
	public static Config config;

	/**
	 * The file used to load and save the client configuration.
	 * The file is not necessarily created until the configuration is saved.
	 */
	public static File configFile;

	public static @Nullable String pendingConfigErrorMessage;

	/**
	 * Logger used for client-side status messages, warnings, and errors.
	 */
	public static final Logger logger = LoggerFactory.getLogger("Proximity Text Chat");
	@Override
	public void onInitializeClient() {
		configFile = FabricLoader.getInstance().getConfigDir().resolve("proximity_text_chat.json").toFile();
		Config.LoadResult result = Config.load(configFile);

		config = result.config();
		pendingConfigErrorMessage = result.error().orElse(null);

		// if there was an error loading the config, warn the user. they probably edited it and won't get the settings they expected.
		ClientTickEvents.END_CLIENT_TICK.register(
			client -> {
				if (pendingConfigErrorMessage != null
					&& client.currentScreen instanceof TitleScreen parent) {
					String error = pendingConfigErrorMessage;
					pendingConfigErrorMessage = null;

					client.setScreen(
						new ConfigLoadError(
							parent,
							Text.literal(error),
							configFile.toPath()
						)
					);
				}
			}
		);
	}


	public static MutableText translatable(String path) {
		return Text.translatable(MOD_ID + "." + path);
	}

	public static @Nullable Screen getConfigScreen(Screen parent) {

		// if cloth is not included, we need to make sure we don't load anything form it.
		if(!isModLoaded("cloth-config")){
			return new MissingClothConfigScreen(parent, configFile.toPath());
		}

		ServiceLoader<ClothConfigCompatBase> loader = ServiceLoader.load(ClothConfigCompatBase.class);
		Optional<ClothConfigCompatBase> provider = loader.findFirst();

		if (provider.isEmpty()){
			logger.error("Cloth Config is loaded, but the config-screen provider was not registered.");
			return new ClothConfigScreenError(parent);
		}

		return provider.get().getConfigScreen(parent);
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