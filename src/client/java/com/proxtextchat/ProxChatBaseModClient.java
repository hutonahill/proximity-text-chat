package com.proxtextchat;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.text.Text;

/**
 * main class for the client.
 */
public class ProxChatBaseModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ConfigBuilder builder = ConfigBuilder.create()
				.setParentScreen(/*need to figure out how to get the partent screen*/)
				.setTitle(Text.literal("Proximity Chat Base Mod Configuration"));

		builder.setSavingRunnable(() -> {
			// This should be code to read the config the used made and modify the game rules/server config.
			// This means we can safely disable the ClothConfig without losing functionality.
		});

		ConfigEntryBuilder entryBuilder = builder.entryBuilder();


	}
}