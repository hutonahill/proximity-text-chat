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
		/*ConfigBuilder builder = ConfigBuilder.create()
				.setParentScreen(*//*need to figure out how to get the partent screen. where ever that is.*//*)
				.setTitle(Text.literal("Proximity Chat Base Mod Configuration"));

		builder.setSavingRunnable(() -> {
			// This should be code to read the config the used made and modify the game rules/server config.
			// This means we can safely disable the ClothConfig without losing functionality.
		});

		ConfigEntryBuilder entryBuilder = builder.entryBuilder();

		// documentation is not clear on what general is
		general.addEntry(entryBuilder.startStrField(new TranslatableText("option.examplemod.optionA"), currentValue)
				// still not sure what this is. does it mean that
				.setDefaultValue("This is the default value") // Recommended: Used when user click "Reset"
				.setTooltip(new TranslatableText("This option is awesome!")) // Optional: Shown when the user hover over this option
				.setSaveConsumer(newValue -> currentValue = newValue) // Recommended: Called when user save the config
				.build()); // Builds the option entry for cloth config*/
	}
}