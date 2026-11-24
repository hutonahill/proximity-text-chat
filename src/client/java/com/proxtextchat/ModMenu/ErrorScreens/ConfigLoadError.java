package com.proxtextchat.ModMenu.ErrorScreens;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.nio.file.Path;

public class ConfigLoadError extends Screen {

    private final Screen parent;
    private final Text errorMessage;
    private final Path configFile;

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

        renderBackground(context, mouseX, mouseY, delta);

        super.render(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(textRenderer, title, width/2, 40, 0xFFFFFF);

        final int textWidth = Math.min(300, width - 40);
        final int textLeft = (width - textWidth) / 2;

        context.drawTextWrapped(
            textRenderer,
            Text.translatable(
                "proxchatbasemod.config.load_error.invalid_json",
                Text.translatable("proxchatbasemod.mod_name")
            ),
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
