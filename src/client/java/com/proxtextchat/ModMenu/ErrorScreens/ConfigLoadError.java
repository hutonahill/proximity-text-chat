package com.proxtextchat.ModMenu.ErrorScreens;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

import java.nio.file.Path;

/**
 * A screen for warning the user that an error occurred while loading the config.
 */
public class ConfigLoadError extends Screen {
    // This screen exists because if the something goes wrong with the config, it's safe to assume the user edited it.
    // (or cloth screwed up, but that's not our problem)
    // in that case we need to warn the user as the game will just use the default config.


    private final Screen parent;
    private final Text errorMessage;
    private final Path configFile;

    /**
     * The constructor for {@link ConfigLoadError}
     * @param parent The screen this one will return to when the user is finished.
     * @param errorMessage The message that should be displayed to the user.
     * @param configFile The path to the config file.
     */
    public ConfigLoadError(Screen parent, Text errorMessage, Path configFile){
        super(Text.literal("Config Loading Error"));
        this.errorMessage = errorMessage;
        this.parent = parent;
        this.configFile = configFile;
    }

    protected void init(){
        final int buttonWidth = 200;
        final int buttonHeight = 20;
        final int left = (width - buttonWidth) / 2;
        Path configFolder = configFile.toAbsolutePath().getParent();

        addDrawableChild(
            ButtonWidget.builder(
                Text.translatable(
                    "proxchatbasemod.config.open_folder"
                ),
                button -> {
                    if (configFolder != null) {
                        Util.getOperatingSystem().open(configFolder);
                    }
                }
            )
            .dimensions(
                left,
                height / 2 + 30,
                buttonWidth,
                buttonHeight
            )
            .build()
        );

        addDrawableChild(
            ButtonWidget.builder(
                    Text.literal("Back"),
                    button -> close()
                )
                .dimensions(
                    left,
                    height / 2 + 55,
                    buttonWidth,
                    buttonHeight
                )
                .build()
        );
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta){

        super.render(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(textRenderer, title, width/2, 40, 0xFFFFFF);

        final int textWidth = Math.min(300, width - 40);
        final int textLeft = (width - textWidth) / 2;

        context.drawTextWrapped(
            textRenderer,
            Text.translatable(
                "proxchatbasemod.config.load_error.invalid_json",
                Text.translatable("proxchatbasemod.mod_name")
            ).append(Text.of("\n"))
            .append(errorMessage),
            textLeft,
            70,
            textWidth,
            0xA0A0A0
        );
    }

    @Override
    public void close() {
        if (client != null) {
            client.setScreen(parent);
        }
    }
}
