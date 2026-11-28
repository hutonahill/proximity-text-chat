package com.proxtextchat;

import com.proxtextchat.network.soucekeys.MessageSourceKey;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

@FunctionalInterface
public interface AliasResolver {
    Text resolve(
        ServerCommandSource source,
        MessageSourceKey sourceKey
    );
}
