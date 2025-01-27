package com.proxtextchat;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class ProxChatBaseModDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		var pack = fabricDataGenerator.createPack();
		pack.addProvider(ItemTagGenerator::new);
	}
	private static class ItemTagGenerator extends FabricTagProvider.ItemTagProvider
	{
		public ItemTagGenerator(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> completableFuture)
		{
			super(output, completableFuture);
		}

		@Override
		protected void configure(RegistryWrapper.WrapperLookup wrapperLookup)
		{
			getOrCreateTagBuilder(ProxChatBaseMod.SEND_IN_HAND);
			getOrCreateTagBuilder(ProxChatBaseMod.SEND_IN_HOTBAR);
			getOrCreateTagBuilder(ProxChatBaseMod.SEND_IN_INVENTORY);
			getOrCreateTagBuilder(ProxChatBaseMod.RECEIVE_IN_HAND);
			getOrCreateTagBuilder(ProxChatBaseMod.RECEIVE_IN_HOTBAR);
			getOrCreateTagBuilder(ProxChatBaseMod.RECEIVE_IN_INVENTORY);
		}
	}

}
