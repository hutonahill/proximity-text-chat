package com.proxtextchat;

import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.message.MessageType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class ChatInterceptor {

    public static void registerChatListener() {
        ServerMessageEvents.

        ServerMessageEvents.registerGlobalReceiver(ServerPlayNetworking.PLAY_READY, (server, player, handler, buf, responseSender) -> {
            server.execute(() -> {
                player.sendMessage(Text.literal("Chat is disabled on this server."), MessageType.SYSTEM);
                // Do not broadcast the message to other players
            });
        });
    }
}
