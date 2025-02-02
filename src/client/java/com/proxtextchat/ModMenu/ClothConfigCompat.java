package com.proxtextchat.ModMenu;

import com.proxtextchat.ProxChatBaseModClient;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;


public class ClothConfigCompat implements ClothConfigCompatBase {
    @Override
    public Screen getConfigScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.translatable(""))
                .setSavingRunnable(() -> ProxChatBaseModClient.config.save(ProxChatBaseModClient.configFile));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        ConfigCategory general = builder.getOrCreateCategory(Text.empty()); // doesn't show when there's only 1 category anyway
        general.addEntry(
            entryBuilder.startEnumSelector(
                ProxChatBaseModClient.translatable("config.shard_render_style"),
                Config.ShardRenderStyle.class,
                ProxChatBaseModClient.config.getStyle()
            ).setDefaultValue(Config.DEFAULT_STYLE)
            .setEnumNameProvider((e) -> ProxChatBaseModClient.translatable(
                    "config.shard_render_style." + ((Config.ShardRenderStyle)e).getSerializedName())
            )
            .setTooltip(ProxChatBaseModClient.translatable("config.shard_render_style.ttp"))
            .setSaveConsumer((style) -> ProxChatBaseModClient.config.setStyle(style))
            .build()
        );
        general.addEntry(entryBuilder.startBooleanToggle(
                        ProxChatBaseModClient.translatable("config.music_turnoff"),
                        ProxChatBaseModClient.config.getShouldStopSound()
                        ).setDefaultValue(Config.DEFAULT_MUSIC_TURNOFF)
                        .setTooltip(ProxChatBaseModClient.translatable("config.music_turnoff.ttp"))
                        .setSaveConsumer((musicTurnoff) -> ProxChatBaseModClient.config.setShouldStopSound(musicTurnoff))
                        .build()
        );

        return builder.build();
    }
}
