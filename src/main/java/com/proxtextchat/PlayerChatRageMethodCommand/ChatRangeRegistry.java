package com.proxtextchat.PlayerChatRageMethodCommand;

import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.world.chunk.Chunk;

import java.util.*;

public class ChatRangeRegistry {

    private ChatRangeRegistry() {}

    public static final ChatRangeRegistry INSTANCE = new ChatRangeRegistry();



    private static final Map<Identifier, PlayerChatRangeDefinition> registry = new HashMap<>();


    private static PlayerChatRangeDefinition CurrentMethod;

    // Register a new method in the registry
    public static void registerMethod(Identifier key, PlayerChatRangeDefinition method) {
        if(registry.containsKey(key)){
            throw new IllegalArgumentException("Method with key '" + key + "' is already registered.");
        }

        registry.put(key, method);
    }

    public static void selectKey(Identifier key){
        CurrentMethod = registry.get(key);
    }

    // Get all registered method keys
    public static Set<Identifier> getKeys() {

        return registry.keySet();
    }

    public static Set<Chunk> Run(Entity entity, int range){
        return  CurrentMethod.getChunks(entity, range);
    }
}
