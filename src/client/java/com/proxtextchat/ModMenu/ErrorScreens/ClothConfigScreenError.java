package com.proxtextchat.ModMenu.ErrorScreens;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class ClothConfigScreenError extends Screen {

    private final Screen parent;

    public ClothConfigScreenError(Screen parent){
        super(Text.literal("Cloth Config Screen Not Found"));

        this.parent = parent;
    }

    @Override
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
            Text.literal(
                "Cloth Config is installed but no config screen provider was found."
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
