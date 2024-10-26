package com.proxtextchat;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.command.argument.IdentifierArgumentType;
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

    public int command(CommandContext<ServerCommandSource> context) {
        ServerCommandSource sorce = context.getSource();

        boolean hasLevel = sorce.hasPermissionLevel(4);
        return 0; // Your logic here
    }


    public static final String MOD_ID = "ProximityTextChatTools";

    private static final String MethodArgumentName = "Method";

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> dispatcher
                .register(literal("PlayerRangeMethod")
                    .requires(source -> source.hasPermissionLevel(4))
                        .then(argument(MethodArgumentName, IdentifierArgumentType.identifier()))
                    .executes(context -> {
                        Identifier methodName = IdentifierArgumentType.getIdentifier(context, MethodArgumentName);
                        context.getSource().sendFeedback(() -> Text.literal("called /PlayerRangeMethod with argument " + methodName), false);

                        return 1;
                }))));
    }
}
