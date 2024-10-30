package com.proxtextchat.PlayerChatRageMethodCommand;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.proxtextchat.ProximityTextChat;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class PlayerChatRangeMethodCommandSuggestionProvider implements SuggestionProvider<ServerCommandSource> {
    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder){
        Identifier chatMethodId = context.getArgument(ProximityTextChat.ChatMethodArgumentName, Identifier.class);

        Set<Identifier> possible = ChatRangeRegistry.getKeys();

        @SuppressWarnings("UnnecessaryLocalVariable") CompletableFuture<Suggestions> output = CommandSource.suggestMatching(
            possible.stream()
                .map(Identifier::toString)
                .filter(id -> builder.getRemaining().isEmpty() || CommandSource.shouldSuggest(builder.getRemaining(), id)),
        builder);

        return output;
    }
}
