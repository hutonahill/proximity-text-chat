package com.proxtextchat.ModMenu;

import com.proxtextchat.PlayerChatRageMethodCommand.ChatRangeRegistry;
import com.proxtextchat.ProxChatBaseModClient;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ClothConfigCompat implements ClothConfigCompatBase {
    @Override
    public Screen getConfigScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
            .setParentScreen(parent)
            .setTitle(Text.translatable("proximity_text_chat"))
            .setSavingRunnable(() -> ProxChatBaseModClient.config.save(ProxChatBaseModClient.configFile));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        ConfigCategory general = builder.getOrCreateCategory(Text.empty()); // doesn't show when there's only 1 category anyway
        general.addEntry(
                entryBuilder.startIntSlider(
                                    ProxChatBaseModClient.translatable("config.player_chat_distance"),
                                    0,
                                    ProxChatBaseModClient.config.getPlayerChatRange(),
                                    256
                            ).setDefaultValue(Config.DEFAULT_PLAYER_TO_PLAYER_CHAT_RANGE)
                            .setTooltip(ProxChatBaseModClient.translatable("cconfig.chatRangeDefinition.ttp"))
                            .setSaveConsumer((chatRange) -> ProxChatBaseModClient.config.setPlayerChatRange(chatRange))
                            .build()
        );
        general.addEntry(
                entryBuilder.startSelector(
                        ProxChatBaseModClient.translatable("config.chatRangeDefinition"),
                        ChatRangeRegistry.getKeys().toArray(new Identifier[0]),
                        ProxChatBaseModClient.config.getChatRangeDefinition()
                ).setDefaultValue(Config.DEFAULT_CHAT_METHOD.getID().toString())
                .setTooltip(ProxChatBaseModClient.translatable("config.chatRangeDefinition_tooltip"))
                .setSaveConsumer((chatRangeDefinition) -> ProxChatBaseModClient.config.setChatRangeDefinition(ChatRangeRegistry.registry.get(chatRangeDefinition)))
                .build()
        );

        return builder.build();
    }
}
