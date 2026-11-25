package com.proxtextchat.util;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.*;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector2d;


/**
 * A custom argument type contains two doubles.
 */
public class Vector2DArgumentType implements ArgumentType<PosArgument> {
	private static final Collection<String> EXAMPLES = Arrays.asList("0 0", "~ ~", "0.1 -0.5", "~1 ~-2");
    /**
     * The exception warning that the system expected 2 variables
     */
	public static final SimpleCommandExceptionType INCOMPLETE_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("argument.pos2d.incomplete"));
	private final boolean centerIntegers;

    /**
	 * A constructor for {@link Vector2DArgumentType}
     * @param centerIntegers
     */
	// TODO what are center integers?
	public Vector2DArgumentType(boolean centerIntegers) {
		this.centerIntegers = centerIntegers;
	}

    /**
     * @return A {@link Vector2DArgumentType} where {@code centerIntegers} is true.
     */
	public static Vector2DArgumentType vec2() {
		return new Vector2DArgumentType(true);
	}

    /**
     * Makes a new {@link Vector2DArgumentType}
	 * @param centerIntegers
     * @return a new {@link Vector2DArgumentType}
     */
	public static Vector2DArgumentType vec2(boolean centerIntegers) {
		return new Vector2DArgumentType(centerIntegers);
	}


    /**
     * @param context Pulls a 2d vector out of a 3d vector argument.
     * @param name the argument name
     * @return a {@link Vector2d} containing the x and z components of the original {@link Vec3d}
     */
	public static Vector2d getVec2(CommandContext<ServerCommandSource> context, String name) {
		Vec3d vec3d = context.getArgument(name, PosArgument.class).toAbsolutePos(context.getSource());
		return new Vector2d(vec3d.x, vec3d.z);
	}

	public PosArgument parse(StringReader stringReader) throws CommandSyntaxException {
		int i = stringReader.getCursor();
		if (!stringReader.canRead()) {
			throw INCOMPLETE_EXCEPTION.createWithContext(stringReader);
		} else {
			CoordinateArgument coordinateArgument = CoordinateArgument.parse(stringReader, this.centerIntegers);
			if (stringReader.canRead() && stringReader.peek() == ' ') {
				stringReader.skip();
				CoordinateArgument coordinateArgument2 = CoordinateArgument.parse(stringReader, this.centerIntegers);
				return new DefaultPosArgument(coordinateArgument, new CoordinateArgument(true, 0.0), coordinateArgument2);
			} else {
				stringReader.setCursor(i);
				throw INCOMPLETE_EXCEPTION.createWithContext(stringReader);
			}
		}
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		if (!(context.getSource() instanceof CommandSource)) {
			return Suggestions.empty();
		} else {
			String string = builder.getRemaining();
			Collection<CommandSource.RelativePosition> collection;
			if (!string.isEmpty() && string.charAt(0) == '^') {
				collection = Collections.singleton(CommandSource.RelativePosition.ZERO_LOCAL);
			} else {
				collection = ((CommandSource)context.getSource()).getPositionSuggestions();
			}

			return CommandSource.suggestColumnPositions(string, collection, builder, CommandManager.getCommandValidator(this::parse));
		}
	}

	@Override
	public Collection<String> getExamples() {
		return EXAMPLES;
	}
}
