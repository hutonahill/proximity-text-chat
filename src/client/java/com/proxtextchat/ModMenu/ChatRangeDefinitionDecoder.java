package com.proxtextchat.ModMenu;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.proxtextchat.PlayerChatRageMethodCommand.ChatRangeRegistry;
import com.proxtextchat.PlayerChatRageMethodCommand.PlayerChatRangeDefinition;
import net.minecraft.util.Identifier;

public class ChatRangeDefinitionDecoder implements Decoder<PlayerChatRangeDefinition>
{
	@Override
	public <T> DataResult<Pair<PlayerChatRangeDefinition, T>> decode(DynamicOps<T> ops, T input)
	{
		return DataResult.success(new Pair<>(ChatRangeRegistry.registry.get(Identifier.of(ops.getStringValue(input).getOrThrow())), input));
	}
}
