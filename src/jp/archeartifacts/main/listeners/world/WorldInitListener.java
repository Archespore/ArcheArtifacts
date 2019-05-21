package jp.archeartifacts.main.listeners.world;

import org.bukkit.World.Environment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;

import jp.archeartifacts.main.populators.ArtifactDungeonPopulator;

public class WorldInitListener implements Listener{
	
	@EventHandler
	public void worldInitEvent(WorldInitEvent event) {
		if (event.getWorld().getEnvironment().equals(Environment.NORMAL)) { event.getWorld().getPopulators().add(new ArtifactDungeonPopulator()); }
	}
}
