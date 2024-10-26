package com.proxtextchat;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.world.GameRules;
import org.jetbrains.annotations.Nullable;

public class StringGameRule extends GameRules.Rule<StringGameRule> {

    String CurrentRule;

    public StringGameRule(GameRules.Type<StringGameRule> type) {
        super(type);
    }

    @Override
    protected void setFromArgument(CommandContext<ServerCommandSource> context, String name) {

    }

    @Override
    protected void deserialize(String value) {
        CurrentRule = value;
    }

    @Override
    public String serialize() {
        return CurrentRule;
    }

    @Override
    public int getCommandResult() {
        return 0;
    }

    @Override
    protected StringGameRule getThis() {
        return null;
    }

    @Override
    protected StringGameRule copy() {
        return null;
    }

    @Override
    public void setValue(StringGameRule rule, @Nullable MinecraftServer server) {

    }
}
