package jp.archeartifacts.main;

import java.io.File;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import jp.archeartifacts.main.commands.CommandGiveMagicItem;
import jp.archeartifacts.main.commands.CommandReload;
import jp.archeartifacts.main.commands.CommandSummonMagicItem;

import jp.archeartifacts.main.controllers.ArtifactController;
import jp.archeartifacts.main.controllers.ImmortalityFieldController;
import jp.archeartifacts.main.controllers.NatureBlessingController;
import jp.archeartifacts.main.listeners.entity.EntityDamageByEntityListener;
import jp.archeartifacts.main.listeners.entity.EntityDamageListener;
import jp.archeartifacts.main.listeners.entity.EntityDeathListener;
import jp.archeartifacts.main.listeners.entity.EntityDismountListener;
import jp.archeartifacts.main.listeners.entity.EntityMountListener;
import jp.archeartifacts.main.listeners.entity.EntityRegainHealthListener;
import jp.archeartifacts.main.listeners.entity.EntityShootBowListeners;
import jp.archeartifacts.main.listeners.player.PlayerInteractListener;
import jp.archeartifacts.main.listeners.player.PlayerItemConsumeListener;
import jp.archeartifacts.main.listeners.player.PlayerTeleportListener;
import jp.archeartifacts.main.listeners.player.PlayerToggleSneakListener;
import jp.archeartifacts.main.listeners.projectile.ProjectileHitListener;
import jp.archeartifacts.main.listeners.spawning.SpawnerSpawnListener;
import jp.archeartifacts.main.listeners.world.WorldInitListener;
import jp.archeartifacts.main.timers.MainTimer;

public class ArcheArtifacts extends JavaPlugin {
	
	MainTimer eventsTimer;
	NatureBlessingController blessingController;
	ImmortalityFieldController immortalityController;
	ArtifactController artifactController;
	
	public void onEnable(){
		getLogger().info("ArcheArtifacts is enabled!");
		saveDefaultConfig();
		FileConfiguration config = getConfig();
		artifactController = new ArtifactController(config, new File(getDataFolder(), "resources/ArtifactData.yml"));
		eventsTimer = new MainTimer(artifactController);
		registerControllers();
		registerListeners();
		registerCommands();
	}
	
	public void onDisable(){
		getLogger().info("ArcheArtifacts is disabled!");
	}
	
	/***
	 * Static method that returns this plugin
	 * @return ArcheArtifacts plugin
	 */
	public static ArcheArtifacts getPlugin() {
		return JavaPlugin.getPlugin(ArcheArtifacts.class);
	}
	
	/**
	 * Reloads the configuration file and artifact data for the plugin.
	 */
	public void reloadPlugin() {
		saveDefaultConfig();
		reloadConfig();
		FileConfiguration config = getConfig();
		artifactController = new ArtifactController(config, new File(getDataFolder(), "resources/ArtifactData.yml"));
		getLogger().info("ArcheArtifacts' configuration has been reloaded!");
	}
	
	/***
	 * Returns this plugin's NatureBlessingController
	 * @return NatureBlessingController
	 */
	public NatureBlessingController getNatureController() {
		return blessingController;
	}
	
	/***
	 * Returns this plugin's ImmortalityFieldController
	 * @return ImmortalityFieldController
	 */
	public ImmortalityFieldController getImmortalityController() {
		return immortalityController;
	}
	
	/***
	 * Returns this plugin's ArtifactController
	 * @return ArtifactController
	 */
	public ArtifactController getArtifactController() {
		return artifactController;
	}
	
	private void registerControllers() {
		blessingController = new NatureBlessingController();
		immortalityController = new ImmortalityFieldController();
	}
	
	private void registerListeners() {
		getServer().getPluginManager().registerEvents(new EntityDamageListener(), this);
		getServer().getPluginManager().registerEvents(new EntityDamageByEntityListener(), this);
		getServer().getPluginManager().registerEvents(new EntityShootBowListeners(), this);
		getServer().getPluginManager().registerEvents(new EntityDeathListener(), this);
		getServer().getPluginManager().registerEvents(new EntityRegainHealthListener(), this);
		getServer().getPluginManager().registerEvents(new EntityMountListener(), this);
		getServer().getPluginManager().registerEvents(new EntityDismountListener(), this);
		getServer().getPluginManager().registerEvents(new PlayerInteractListener(), this);
		getServer().getPluginManager().registerEvents(new PlayerItemConsumeListener(), this);
		getServer().getPluginManager().registerEvents(new PlayerTeleportListener(), this);
		getServer().getPluginManager().registerEvents(new PlayerToggleSneakListener(), this);
		getServer().getPluginManager().registerEvents(new ProjectileHitListener(), this);
		getServer().getPluginManager().registerEvents(new SpawnerSpawnListener(), this);
		if (artifactController.buriedReliquaryEnabled) { getServer().getPluginManager().registerEvents(new WorldInitListener(), this); }
	}
	
	private void registerCommands() {
		getCommand("aartifactsreload").setExecutor(new CommandReload());
		getCommand("givemagicitem").setExecutor(new CommandGiveMagicItem());
		getCommand("summonmagicitem").setExecutor(new CommandSummonMagicItem());
	}
}
