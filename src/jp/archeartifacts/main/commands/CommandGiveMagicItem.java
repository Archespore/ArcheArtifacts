package jp.archeartifacts.main.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import jp.archeartifacts.main.ArcheArtifacts;
import jp.archeartifacts.main.controllers.ArtifactController;

public class CommandGiveMagicItem implements CommandExecutor, TabCompleter {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 2) {
			Player commandPlayer = Bukkit.getPlayer(args[0]);
			if (commandPlayer != null) {
				Material artifactMaterial = Material.getMaterial(args[1]);
				ArtifactController artifactController = ArcheArtifacts.getPlugin().getArtifactController();
				if ( (artifactMaterial != null) && (artifactController.doesArtifactExist(artifactMaterial)) ) {
					Inventory playerInventory = commandPlayer.getInventory();
					if (playerInventory.firstEmpty() != -1) { 
						playerInventory.addItem(artifactController.generateArtifact(artifactMaterial));
						sender.sendMessage(ChatColor.GREEN + commandPlayer.getDisplayName() + " Was given the magical item: " + artifactMaterial.toString());
					}
					else { sender.sendMessage(ChatColor.DARK_RED + "The specified player does not have room in their inventory!"); }
				}
				else { sender.sendMessage(ChatColor.DARK_RED + "An artifact of that type does not exist!"); }
			}
			else { sender.sendMessage(ChatColor.DARK_RED + "Player was not found!"); }
			return true;
		}
		else { sender.sendMessage(ChatColor.DARK_RED + "Invalid number of arguments!"); }
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 2) {
			
			//Variable gathering and initialization
			String argumentString = args[1].toLowerCase();
			Set<Material> artifactSet = ArcheArtifacts.getPlugin().getArtifactController().getRegisteredArtifacts();
			List<String> commandOptions = new ArrayList<>();
			
			//If the second argument is blank, return all values
			if (argumentString.equals("")) {
				for (Material artifact : artifactSet) { commandOptions.add(artifact.toString()); }
			}
			//Otherwise, return the values that start with our argument
			else {
				for (Material artifact : artifactSet) { 
					if (artifact.toString().toLowerCase().startsWith(argumentString)) { commandOptions.add(artifact.toString()); }
				}
			}
			
			//Sort the collection afterwards
			Collections.sort(commandOptions);
			return commandOptions;
		}
		return null;
	}

}
