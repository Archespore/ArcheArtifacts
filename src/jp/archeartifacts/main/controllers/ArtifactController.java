package jp.archeartifacts.main.controllers;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import jp.archeartifacts.main.ArcheArtifacts;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class ArtifactController {

	File artifactFile;
	FileConfiguration pluginConfiguration;
	Map<Material, ItemStack> registeredArtifacts = new HashMap<>();
	
	//Config Settings
	public boolean skyFallEnabled;
	public double skyFallChance;
	
	public boolean buriedReliquaryEnabled;
	public double buriedReliquaryChance;
	
	public boolean mobDeathEnabled;
	public boolean mobDeathSpawnerNerf;
	public double mobDeathChance;
	
	public ArtifactController(FileConfiguration pluginConfiguration, File artifactFile) {
		this.artifactFile = artifactFile;
		this.pluginConfiguration = pluginConfiguration;
		loadPluginData();
		loadArtifactData();
	}
	
	/**
	 * Checks whether an artifact by the specified material exists
	 * @param material the material of the artifact to check for
	 * @return true/false if the artifact exists
	 */
	public boolean doesArtifactExist(Material material) {
		return registeredArtifacts.containsKey(material);
	}
	
	/***
	 * Generates a random magical item from the registered materials
	 * @return and ItemStack of the generated magical item
	 */
	public ItemStack generateArtifact() {
		Set<Material> artifactMaterials = registeredArtifacts.keySet();
		Material randomArtifact = artifactMaterials.stream().collect(Collectors.toList()).get(ThreadLocalRandom.current().nextInt(artifactMaterials.size()));
		return generateArtifact(randomArtifact);
	}
	
	/***
	 * Generates the specified magical item type
	 * @param material the magical item to generate
	 * @return and ItemStack of the generated magical item
	 */
	public ItemStack generateArtifact(Material material) {
		//First, make sure the item we are try to generate actually exists.
		if (doesArtifactExist(material)) {
			//Clone a new object of the artifact item
			ItemStack artifactItem = registeredArtifacts.get(material).clone();
			
			//Assign a UID to the newly generated artifact to prevent item stacking
			ItemMeta artifactMeta = artifactItem.getItemMeta();
			PersistentDataContainer dataContainer = artifactMeta.getPersistentDataContainer();
			dataContainer.set(new NamespacedKey(ArcheArtifacts.getPlugin(), "UID"), PersistentDataType.DOUBLE, ThreadLocalRandom.current().nextDouble());
			artifactItem.setItemMeta(artifactMeta);
			return artifactItem;
		}
		return null;
	}
	
	/**
	 * Summons a random falling artifact at the specified location
	 * @param location the location to summon the new artifact
	 * @return the summoned artifact Item
	 */
	public Item summonArtifact(Location location) {
		ItemStack artifactItem = generateArtifact();
		return summonArtifact(location, artifactItem.getType());
	}
	
	/**
	 * Summons a falling artifact at the specified location
	 * @param location the location to summon the new artifact
	 * @param material the material of the artifact to summon
	 * @return the summoned artifact Item
	 */
	public Item summonArtifact(Location location, Material material) {
		ItemStack artifactItem = generateArtifact(material);
		if (artifactItem != null) {
			World summoningWorld = location.getWorld();
			Bukkit.getOnlinePlayers().forEach(player -> {
				Location playerLocation = player.getLocation();
				if (playerLocation.distance(location) <= 640) { 
					player.playSound(playerLocation, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, .5f);
					player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.GOLD + "A magical artifact has fallen from the sky nearby!"));
				}
			});
			location.setY(summoningWorld.getHighestBlockYAt(location.getBlockX(), location.getBlockZ()));
			Item summonedArtifact = summoningWorld.dropItemNaturally(location.add(ThreadLocalRandom.current().nextDouble(64.0), 256, ThreadLocalRandom.current().nextDouble(64.0)), artifactItem);
			summonedArtifact.setVelocity(summonedArtifact.getVelocity().add(new Vector(ThreadLocalRandom.current().nextDouble(1.0) - .5, 0.0, ThreadLocalRandom.current().nextDouble(1.0) - .5)));
			new BukkitRunnable() {
				Item artifactEntity = summonedArtifact;
				public void run() {
					if (artifactEntity.isOnGround() || !artifactEntity.isValid()) { Bukkit.getScheduler().cancelTask(this.getTaskId()); }
					else { 			
						Firework firework = summonedArtifact.getWorld().spawn(summonedArtifact.getLocation(), Firework.class);
						FireworkMeta fireworkMeta = firework.getFireworkMeta();
						fireworkMeta.setPower(0);
						fireworkMeta.addEffect(FireworkEffect.builder()
								.flicker(false)
								.trail(true)
								.with(Type.BALL)
								.withColor(Color.fromRGB(ThreadLocalRandom.current().nextInt(256), ThreadLocalRandom.current().nextInt(256), ThreadLocalRandom.current().nextInt(256)))
								.withColor(Color.fromRGB(ThreadLocalRandom.current().nextInt(256), ThreadLocalRandom.current().nextInt(256), ThreadLocalRandom.current().nextInt(256)))
								.withFade(Color.fromRGB(ThreadLocalRandom.current().nextInt(256), ThreadLocalRandom.current().nextInt(256), ThreadLocalRandom.current().nextInt(256)))
								.build());
						firework.setFireworkMeta(fireworkMeta);
					}
				}
			}.runTaskTimer(ArcheArtifacts.getPlugin(), 0, 3);
			return summonedArtifact;
		}
		return null;
	}
	
	/**
	 * Gets the set of all registered artifact material types
	 * @return the set of registered material types
	 */
	public Set<Material> getRegisteredArtifacts() {
		return registeredArtifacts.keySet();
	}
	
	/**
	 * Loads plugin data from the plugin's configuration file.
	 */
	private void loadPluginData() {
		skyFallEnabled = pluginConfiguration.getBoolean("GENERATION.SKY-FALL.Enabled", false);
		skyFallChance = pluginConfiguration.getDouble("GENERATION.SKY-FALL.Chance", 0.0);
		
		buriedReliquaryEnabled = pluginConfiguration.getBoolean("GENERATION.BURIED-RELIQUARY.Enabled", false);
		buriedReliquaryChance = pluginConfiguration.getDouble("GENERATION.BURIED-RELIQUARY.Chance", 0.0);
		
		mobDeathEnabled = pluginConfiguration.getBoolean("GENERATION.MOB-DEATH.Enabled", false);
		mobDeathSpawnerNerf = pluginConfiguration.getBoolean("GENERATION.MOB-DEATH.SpawnerNerf", true);
		mobDeathChance = pluginConfiguration.getDouble("GENERATION.MOB-DEATH.Chance", 0.0);
	}
	
	/**
	 * Loads artifact data from the controller's data file.
	 */
	private void loadArtifactData() {
		YamlConfiguration artifactYaml = new YamlConfiguration();
		try {
			//If the file doesn't exists, grab the file from the jar and add it to our data folder
			if (!artifactFile.exists()) {
				artifactFile.getParentFile().mkdirs();
				ArcheArtifacts.getPlugin().saveResource("resources/" + artifactFile.getName(), false);
			}
			//Load in the itemstacks in the file
			artifactYaml.load(artifactFile);
			ConfigurationSection artifactSection = artifactYaml.getConfigurationSection("ITEMS");
			for (String key : artifactSection.getKeys(false)) {
				Material artifactMaterial = Material.valueOf(key);
				ItemStack artifact = artifactSection.getItemStack(key);
				registeredArtifacts.put(artifactMaterial, artifact);
			}
		} 
		catch (Exception error) { error.printStackTrace(); }
	}
}
