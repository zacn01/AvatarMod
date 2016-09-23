package com.crowsofwar.gorecore.tree;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.command.ICommandSender;
import net.minecraft.world.World;

/**
 * An argument for a player's username. Supports tab completion if the player is currently in the
 * world. However, players not in the world can be specified.
 * 
 * @author CrowsOfWar
 */
public class ArgumentPlayerName implements IArgument<String> {
	
	private final String name;
	
	public ArgumentPlayerName(String name) {
		this.name = name;
	}
	
	@Override
	public boolean isOptional() {
		return false;
	}
	
	@Override
	public String getDefaultValue() {
		return null;
	}
	
	@Override
	public String convert(String input) {
		return input;
	}
	
	@Override
	public String getArgumentName() {
		return name;
	}
	
	@Override
	public String getHelpString() {
		return "<playername>";
	}
	
	@Override
	public String getSpecificationString() {
		return "<" + getArgumentName() + ">";
	}
	
	@Override
	public List<String> getCompletionSuggestions(ICommandSender sender, String currentInput) {
		World world = sender.getEntityWorld();
		List<String> suggestions = new ArrayList<>();
		
		world.playerEntities.forEach(player -> {
			suggestions.add(player.getName());
		});
		
		if (!suggestions.get(0).toLowerCase().startsWith(currentInput.toLowerCase()))
			return new ArrayList<>();
		return suggestions;
	}
	
}