package com.proxtextchat.ModMenu;

import com.proxtextchat.PlayerChatRageMethodCommand.ChatRangeRegistry;
import com.proxtextchat.ProxChatBaseModClient;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.SelectionListEntry;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Optional;

/**
 * Bridges the config system and the Cloth Config system.
 */
public class ClothConfigCompat implements ClothConfigCompatBase {
    @Override
    public Screen getConfigScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
            .setParentScreen(parent)
            .setTitle(Text.translatable("proxchatbasemod.mod_name"))
            .setSavingRunnable(() -> ProxChatBaseModClient.config.save(ProxChatBaseModClient.configFile));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        ConfigCategory general = builder.getOrCreateCategory(Text.empty()); // doesn't show when there's only 1 category anyway

        AbstractConfigListEntry<Integer> entry1 = entryBuilder.startIntSlider(
                ProxChatBaseModClient.translatable("config.player_chat_distance"),
                ProxChatBaseModClient.config.getPlayerChatRange(),
                Config.MIN_PLAYER_CHAT_RANGE,
                Config.MAX_PLAYER_CHAT_RANGE
            ).setDefaultValue(Config.DEFAULT_PLAYER_TO_PLAYER_CHAT_RANGE)
            .setTooltip(ProxChatBaseModClient.translatable("config.player_chat_distance.tooltip"))
            .setSaveConsumer((chatRange) -> ProxChatBaseModClient.config.setPlayerChatRange(chatRange))
            .build();
        general.addEntry(entry1);

        SelectionListEntry<Identifier> entry2 = entryBuilder.startSelector(
                ProxChatBaseModClient.translatable("config.player_chat_range_definition"),
                ChatRangeRegistry.getKeys().toArray(new Identifier[0]),
                ProxChatBaseModClient.config.getChatRangeDefinition()
            ).setDefaultValue(Config.DEFAULT_CHAT_METHOD.getID())
            .setTooltipSupplier(
                identifier -> Optional.of(
                    new Text[] {
                        ProxChatBaseModClient.translatable(
                            "config.player_chat_range_definition.tooltip"
                        ),
                        Text.literal(identifier.toString())
                    }
                )
            ).setSaveConsumer(
                (chatRangeDefinition) -> ProxChatBaseModClient.config
                    .setChatRangeDefinition(ChatRangeRegistry.registry.get(chatRangeDefinition))
            ).build();

        general.addEntry(entry2);

        return builder.build();
    }
}
