package com.proxtextchat.LangProviders;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.registry.RegistryWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

// TODO: Hmmm. i may be doing translations wrong.
public class ProxChatBaseModEnglishLangProvider extends FabricLanguageProvider {
    public ProxChatBaseModEnglishLangProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        // Specifying en_us is optional, as it's the default language code
        super(dataOutput, "en_us", registryLookup);
    }

    @Override
    public void generateTranslations(RegistryWrapper.WrapperLookup wrapperLookup, @NotNull TranslationBuilder translationBuilder) {
        translationBuilder.add("proxchatbasemod.player_chat_distance", "Player Chat Distance");
        translationBuilder.add("proxchatbasemod.player_chat_range_definition", "Player Chat Range Definition");
        translationBuilder.add("proxchatbasemod.proximity_text_chat", "Proximity Text Chat");
        translationBuilder.add("proxchatbasemod.cant_parse_world_key", "Unable to parse world from world key %s.");
        translationBuilder.add("proxchatbasemod.chunk_set_not_found", "No chunk set found for identifier %s.");
        translationBuilder.add("proxchatbasemod.chunk_already_exists", "A chunk set with identifier %s already exists.");
    }
}
