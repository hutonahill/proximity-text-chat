package com.proxtextchat.ModMenu;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.proxtextchat.PlayerChatRageMethodCommand.PlayerChatRangeDefinition;

public class ChatRangeDefinitionEncoder implements Encoder<PlayerChatRangeDefinition>
{
	@Override
	public <T> DataResult<T> encode(PlayerChatRangeDefinition input, DynamicOps<T> ops, T prefix)
	{
		return DataResult.success(ops.createString(input.getID().toString()));
	}
}
