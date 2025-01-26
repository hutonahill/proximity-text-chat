package com.proxtextchat.PlayerChatRageMethodCommand;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameRules;
import net.minecraft.world.chunk.Chunk;

import java.util.*;

/**
 * Registers and manages methods for determining the chat range for players.
 */
public class ChatRangeRegistry {

    private ChatRangeRegistry() {}

    /**
     * A game rule key that determines the player's chat range.
     */
    public static final GameRules.Key<GameRules.IntRule> PLAYER_CHAT_RANGE =
            GameRuleRegistry.register("playerChatRange", GameRules.Category.CHAT,
                    GameRuleFactory.createIntRule(64));


    private static final Map<Identifier, PlayerChatRangeDefinition> registry = new HashMap<>();


    private static PlayerChatRangeDefinition CurrentMethod;

    /**
     * Registers a new chat range method in the registry.
     *
     * @param key the identifier for the method.
     * @param method the method to be registered.
     * @throws IllegalArgumentException if a method with the same key is already registered.
     */
    public static void registerMethod(Identifier key, PlayerChatRangeDefinition method) {
        if(registry.containsKey(key)){
            throw new IllegalArgumentException("Method with key '" + key + "' is already registered.");
        }

        registry.put(key, method);
    }


    /**
     * Selects a specific method from the registry to be used.
     *
     * @param key the identifier for the method to be selected.
     */
    public static void selectKey(Identifier key){
        CurrentMethod = registry.get(key);
    }

    /**
     * Gets all registered method keys.
     *
     * @return a set of all registered method keys.
     */
    public static Set<Identifier> getKeys() {

        return registry.keySet();
    }

    /**
     * Runs the selected chat range method to determine which players are within range of a given entity.
     *
     * @param entity the entity for which the range is calculated.
     * @param range the range to check for players.
     * @return a set of players that are within the specified range of the entity.
     */
    public static Set<PlayerEntity> Run(Entity entity, int range){
        return  CurrentMethod.getPlayers(entity, range);
    }
}
