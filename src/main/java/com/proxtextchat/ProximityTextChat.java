package com.proxtextchat;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.proxtextchat.PlayerChatRageMethodCommand.ChatRangeRegistry;
import com.proxtextchat.PlayerChatRageMethodCommand.PlayerChatRangeDefinition;
import com.proxtextchat.PlayerChatRageMethodCommand.PlayerChatRangeMethodCommandSuggestionProvider;
import com.proxtextchat.PlayerChatRageMethodCommand.StandardPlayerChatRangeMethod;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameRules;

import static net.minecraft.server.command.CommandManager.*;

//import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;

public class ProximityTextChat implements ModInitializer {

    public static final GameRules.Key<GameRules.BooleanRule> CUSTOM_RULE = GameRuleRegistry.register(
            "EnableProximityTextChat",
            GameRules.Category.CHAT,
            GameRuleFactory.createBooleanRule(false)
    );




    public static final String MOD_ID = "proximity_text_chat_tools";

    public static final String ChatMethodArgumentName = "Method";

    private static final PlayerChatRangeDefinition ChatMethod = StandardPlayerChatRangeMethod.getInstance();

    @Override
    public void onInitialize() {

        ChatRangeRegistry.registerMethod(ChatMethod.getID(), ChatMethod);

        ChatRangeRegistry.selectKey(ChatMethod.getID());

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            LiteralCommandNode<ServerCommandSource> PlayerChatRangeMethodNode = CommandManager
                    .literal("PlayerChatRangeMethod")
                    .requires(source -> source.hasPermissionLevel(4))
                    .then(argument(ChatMethodArgumentName, IdentifierArgumentType.identifier())
                            .suggests(new PlayerChatRangeMethodCommandSuggestionProvider())
                            .executes(this::PlayerRangeMethodCommand)
                    )
                    .build();

            dispatcher.getRoot().addChild(PlayerChatRangeMethodNode);
        });
    }

    private int PlayerRangeMethodCommand(CommandContext<ServerCommandSource> context) {
        Identifier methodName = IdentifierArgumentType.getIdentifier(context, ChatMethodArgumentName);

        boolean exists =  ChatRangeRegistry.getKeys().contains(methodName);

        if (exists == true){
            ChatRangeRegistry.selectKey(methodName);
            context.getSource().sendFeedback(() -> Text.literal("PlayerChatRangeMethod has been defined as " + methodName + "."), true);
        }
        else{
            context.getSource().sendFeedback(() -> Text.literal("Could not find PlayerChatRangeMethod " + methodName + "."), true);
        }


        return 1;
    }
}
