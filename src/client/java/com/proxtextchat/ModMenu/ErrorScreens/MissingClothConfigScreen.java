package com.proxtextchat.ModMenu.ErrorScreens;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.nio.file.Path;

public class MissingClothConfigScreen extends Screen {

    private static final String CLOTH_CONFIG_GIT_HUB_URL =
            "https://github.com/shedaniel/cloth-config";

    private final Screen parent;
    private final Path configPath;

    public MissingClothConfigScreen(Screen parent, Path configPath) {
        super(Text.literal("Cloth Config is not installed"));

        this.parent = parent;
        this.configPath = configPath;


    }

    @Override
    protected void init() {

        // define the size of a button.
        final int buttonWidth = 200;
        final int buttonHeight = 20;
        final int left = (width - buttonWidth) / 2;

        addDrawableChild(
            ButtonWidget.builder(
                Text.literal("Open Cloth Config Git Hub"),
                ConfirmLinkScreen.opening(
                    this,
                    CLOTH_CONFIG_GIT_HUB_URL,
                    false
                )
            ).dimensions(
                left,
                height / 2 + 30,
                buttonWidth,
                buttonHeight
            ).build()
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
    public void render(
        DrawContext context,
        int mouseX,
        int mouseY,
        float delta
    ) {


        renderBackground(context, mouseX, mouseY, delta);

        super.render(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(textRenderer, title, width/2, 40, 0xFFFFFF);

        final int textWidth = Math.min(300, width - 40);
        final int textLeft = (width - textWidth) / 2;



        context.drawTextWrapped(
            textRenderer,
            Text.literal(
                "Cloth Config is required to use the graphical configuration screen."
            ),
            textLeft,
            70,
            textWidth,
            0xA0A0A0
        );

        context.drawTextWrapped(
            textRenderer,
            Text.literal(
                "The configuration can be edited manually at:\n"
                    + configPath.toAbsolutePath()
            ),
            textLeft,
            95,
            textWidth,
            0xA0A0A0
        );
    }

    @Override
    public void close() {
        if(client != null){
            client.setScreen(parent);
        }
    }

}
