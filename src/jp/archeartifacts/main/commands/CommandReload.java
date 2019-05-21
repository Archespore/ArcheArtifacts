package jp.archeartifacts.main.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import jp.archeartifacts.main.ArcheArtifacts;

public class CommandReload implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		ArcheArtifacts.getPlugin().reloadPlugin();
		return true;
	}

}
