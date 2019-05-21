package jp.archeartifacts.main.timers;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitRunnable;

import jp.archeartifacts.main.ArcheArtifacts;
import jp.archeartifacts.main.controllers.ArtifactController;
import jp.archeartifacts.main.helpers.InventoryHelper;
import jp.archeartifacts.main.helpers.LivingEntityHelper;
import jp.archeartifacts.main.helpers.ParticleHelper;
import jp.archeartifacts.main.helpers.PotionEffectHelper;

public class MainTimer {

	public MainTimer(ArtifactController controller) {
		new BukkitRunnable() {
			
			//Variable initializations
			final int MAX_CYCLE = 1200;
			int tickCount = 0;
			ArtifactController artifactController = controller;
			ThreadLocalRandom randomThread = ThreadLocalRandom.current();
			
			public void run() {
				boolean twentyCycle = (tickCount % 20 == 0);
				boolean fiftyCycle = (tickCount % 50 == 0);
				boolean hundredCycle = (tickCount % 100 == 0);
				
				//Entity loop
				for(World world : Bukkit.getServer().getWorlds()) {
					
					long worldTime = world.getTime();
					Environment worldEnviroment = world.getEnvironment();
					
					//Remove immortal tag from all entities
					for(Entity entity : world.getEntities()) {
						entity.removeScoreboardTag("IMMORTAL");
					}
					
					//Check if we should activate a sky fall artifact
					if ( (artifactController.skyFallEnabled) && (worldEnviroment.equals(Environment.NORMAL)) ) {
						if ( (worldTime == (18000)) && (!world.hasMetadata("SKYFALL_ACTIVATED")) ) {
							world.setMetadata("SKYFALL_ACTIVATED", new FixedMetadataValue(ArcheArtifacts.getPlugin(), true));
							if (randomThread.nextDouble(100.0) < artifactController.skyFallChance) {
								List<Player> worldPlayers = world.getPlayers();
								artifactController.summonArtifact(worldPlayers.get(randomThread.nextInt(worldPlayers.size())).getLocation());
							}
						}
						else if ( ((worldTime >= 0) && (worldTime < 12000)) ) { world.removeMetadata("SKYFALL_ACTIVATED", ArcheArtifacts.getPlugin()); }
					}
				}
				
				//Player loop
				for (Player player : Bukkit.getOnlinePlayers()) {
					
					//Draconic leap check & Immortal tag removal
					if (player.getScoreboardTags().contains("DRACONIC_LEAP")) { draconicLeapCheck(player); }
					player.removeScoreboardTag("IMMORTAL");
					
					//Gem of hope, trades exp for rapid healing
					if (InventoryHelper.containsMagicalItem(player, Material.DIAMOND)) {
						if ( (player.getHealth() < player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()) && ((player.getExp() > 0) || (player.getLevel() > 0)) ) {
							player.giveExp(-1);
							LivingEntityHelper.addHealth(player, .05);
							player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, .1f, (float)(1.05 + randomThread.nextDouble(.4)));
						}
					}
					
					//Particle effects, creates a swirl of particles around the player.
					int particleAnimation = (tickCount % 50);
					double particleAngle = Math.toRadians((360.0/50.0) * particleAnimation);
					if (InventoryHelper.containsMagicalItem(player, Material.REDSTONE)) {
						player.getWorld().spawnParticle(Particle.REDSTONE, player.getLocation().add(Math.cos(particleAngle), (particleAnimation/50.0) * 2.0, Math.sin(particleAngle)), 2, 0, 0, 0, 0, new Particle.DustOptions(Color.RED, .75f));
						player.getWorld().spawnParticle(Particle.REDSTONE, player.getLocation().subtract(Math.cos(particleAngle), -(particleAnimation/50.0) * 2.0, Math.sin(particleAngle)), 2, 0, 0, 0, 0, new Particle.DustOptions(Color.RED, .75f));
					}
					
					//Imortality field, prevents entities from dying inside
					ArcheArtifacts plugin = ArcheArtifacts.getPlugin();
					plugin.getImmortalityController().loopBlessings();
					
					if (twentyCycle) { twentyTickCycle(player); }
					if (fiftyCycle) { fiftyTickCycle(player); }
					if (hundredCycle) { hundredTickCycle(player); }
				}
				tickCount = (++tickCount >= MAX_CYCLE ? 0 : tickCount);
			}
			
			private void twentyTickCycle(Player player) {
				//Loops blessings stored in their respective controllers
				ArcheArtifacts plugin = ArcheArtifacts.getPlugin();
				plugin.getNatureController().loopBlessings();
				
				// Get Information related to player location
				Location playerLocation = player.getLocation();
				
				//Wolf's Bone, gives friendly nearby wolves speed.
				if (InventoryHelper.containsMagicalItem(player, Material.BONE)) {
					List<Entity> nearbyEntities = player.getNearbyEntities(16, 16, 16);
					nearbyEntities.removeIf(entity -> !(entity instanceof Wolf));
					for (Entity entity : nearbyEntities){
						Wolf wolf = (Wolf)entity;
						if (wolf.getLocation().distance(playerLocation) <= 16 && wolf.isTamed() && wolf.getOwner().equals(player)){
							wolf.addPotionEffect(PotionEffectHelper.comparePotionEffect(new PotionEffect(PotionEffectType.SPEED, 50, 0), player));
							LivingEntityHelper.addHealth(wolf, .25);
						}
					}
				}
				
				//All-Seeing Eye, grants night vision and glows nearby entities.
				if (InventoryHelper.containsMagicalItem(player, Material.ENDER_EYE)) {
					player.addPotionEffect(PotionEffectHelper.comparePotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 300, 0), player), true);
					for (Entity entity : player.getNearbyEntities(16, 16, 16)){
						if (entity instanceof LivingEntity && entity.getLocation().distance(playerLocation) <= 16) {
							LivingEntity livingEntity = (LivingEntity)entity;
							livingEntity.addPotionEffect(PotionEffectHelper.comparePotionEffect(new PotionEffect(PotionEffectType.GLOWING, 30, 0), livingEntity), true);
						}
					}
				}
			}
			
			private void fiftyTickCycle(Player player) {
				// Get Information related to player location
				Location playerLocation = player.getLocation();
				Block playerBlock = playerLocation.getBlock();
				byte skyLight = player.getEyeLocation().getBlock().getLightFromSky();
				long worldTime = player.getWorld().getTime();

				// Items that heal over time depending on location
				if (LivingEntityHelper.isDamaged(player)) {
					// Items that reduce damage and increase damage depending on location
					if (skyLight >= 15) {
						// Lively Sunflower
						if ((worldTime >= 0) && (worldTime < 12000)) {
							if (InventoryHelper.containsMagicalItem(player, Material.SUNFLOWER)) {
								player.playSound(player.getLocation(), Sound.ENTITY_ENDER_EYE_DEATH, .75f, 1.25f);
								LivingEntityHelper.addHealth(player, 1);
							}
						}
						// Eye of Night
						else {
							if (InventoryHelper.containsMagicalItem(player, Material.FERMENTED_SPIDER_EYE)) {
								player.playSound(player.getLocation(), Sound.ENTITY_ENDER_EYE_DEATH, .75f, 1.25f);
								LivingEntityHelper.addHealth(player, 1);
							}
						}
					}
					// Miner's Blessing
					else if (skyLight <= 0) {
						if (InventoryHelper.containsMagicalItem(player, Material.CLAY_BALL)) {
							player.playSound(player.getLocation(), Sound.ENTITY_ENDER_EYE_DEATH, .75f, 1.25f);
							LivingEntityHelper.addHealth(player, 1);
						}
					}
					// Soul of the Ocean
					if (playerBlock.getType().equals(Material.WATER)) {
						if (InventoryHelper.containsMagicalItem(player, Material.PRISMARINE_SHARD)) {
							player.playSound(player.getLocation(), Sound.ENTITY_ENDER_EYE_DEATH, .75f, 1.25f);
							LivingEntityHelper.addHealth(player, 1);
						}
					}
				}
				
				//Hellish Aura, damages nearby entities every 2.5 seconds.
				if (InventoryHelper.containsMagicalItem(player, Material.NETHER_BRICK)) {
					List<Entity> nearbyEntities = player.getNearbyEntities(5, 5, 5);
					nearbyEntities.removeIf(entity -> !(entity instanceof LivingEntity));
					for (Entity entity : nearbyEntities) {
						if (entity.getLocation().distance(player.getLocation()) <= 5) {
							LivingEntityHelper.removeHealth((LivingEntity)entity, 1);
						}
					}
				}
				
				//Creeper head, explodes nearby entities
				if (InventoryHelper.wearingMagicalItem(player, Material.CREEPER_HEAD)) {
					
					//Variable initializations
					double closestDistance = 8.0;
					LivingEntity closestEntity = null;
					List<Entity> nearbyEntities = player.getNearbyEntities(8, 8, 8);
					nearbyEntities.removeIf(entity -> !(entity instanceof LivingEntity));
					ParticleHelper.spawnParticleRing(Particle.REDSTONE, playerLocation.clone().add(0, .5, 0), 8.0, 64, 3, 0.0, new DustOptions(Color.GRAY, 1.5f));
					
					for (Entity entity : nearbyEntities) {
						Location entityLocation = entity.getLocation();
						if (entity instanceof LivingEntity && entityLocation.distance(playerLocation) <= closestDistance) {
							closestDistance = entityLocation.distance(playerLocation);
							closestEntity = (LivingEntity)entity;
						}
					}
					if (closestEntity != null){
						Location entityLocation = closestEntity.getLocation();
						closestEntity.getWorld().createExplosion(entityLocation.getX(), entityLocation.clone().add(0, 1, 0).getY(), entityLocation.getZ(), 1.25f, false, false);
					}
				}
			}
			
			private void hundredTickCycle(Player player) {
				Inventory playerInventory = player.getInventory();
				
				//Photosynthetic Seeds, slowly restores hunger and saturation over time
				if (InventoryHelper.containsMagicalItem(player, Material.WHEAT_SEEDS)) {
					boolean effectActivated = true;
					if (player.getFoodLevel() < 20) { player.setFoodLevel(Math.min(player.getFoodLevel() + 1, 20)); }
					else if (player.getSaturation() < 20) { player.setSaturation(Math.min(player.getSaturation() + .5f, 20)); }
					else { effectActivated = false; }
					if (effectActivated) { player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EAT, .75f, 1.0f); }
				}
				
				//Enchanter's Stone, turns lapis into xp bottles
				if (InventoryHelper.containsMagicalItem(player, Material.LAPIS_BLOCK)) {
					if (playerInventory.firstEmpty() != -1 && playerInventory.contains(Material.LAPIS_LAZULI)) { 
						player.playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_INFECT, .75f, 1.25f);
						playerInventory.removeItem(new ItemStack(Material.LAPIS_LAZULI, 1));
						playerInventory.addItem(new ItemStack(Material.EXPERIENCE_BOTTLE, 1));
						player.updateInventory();
					}
				}
				
				//Portable Farm, turns seeds into bread
				if (InventoryHelper.containsMagicalItem(player, Material.WHEAT)) {
					if (playerInventory.firstEmpty() != -1 && playerInventory.contains(Material.WHEAT_SEEDS)) {
						player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, .75f, 1.0f);
						playerInventory.removeItem(new ItemStack(Material.WHEAT_SEEDS, 1));
						playerInventory.addItem(new ItemStack(Material.BREAD, 1));
						player.updateInventory();
					}
				}
				
				//Witch's Brewing Stand, turns nether wart into random potions
				if (InventoryHelper.containsMagicalItem(player, Material.BREWING_STAND)) {
					if (playerInventory.firstEmpty() != -1 && playerInventory.contains(Material.NETHER_WART_BLOCK)) {
						player.playSound(player.getLocation(), Sound.BLOCK_BREWING_STAND_BREW, 1.0f, 1.5f);
						playerInventory.removeItem(new ItemStack(Material.NETHER_WART_BLOCK, 1));
						
						//Random potion generation
						ItemStack randomPotion = new ItemStack((ThreadLocalRandom.current().nextBoolean() ? Material.POTION : Material.SPLASH_POTION), 1);
						PotionMeta potionMeta = (PotionMeta)randomPotion.getItemMeta();
						potionMeta.setBasePotionData(new PotionData(PotionType.AWKWARD));
						PotionEffectType[] potionEffects = PotionEffectType.values();
						int randomEffect = ThreadLocalRandom.current().nextInt(potionEffects.length);
						potionMeta.addCustomEffect(new PotionEffect(potionEffects[randomEffect], ThreadLocalRandom.current().nextInt(1200, 2401), ThreadLocalRandom.current().nextInt(2), false, true), true);
						randomPotion.setItemMeta(potionMeta);
						
						playerInventory.addItem(randomPotion);
						player.updateInventory();
					}
				}
			}
		}.runTaskTimer(ArcheArtifacts.getPlugin(), 0, 1);
	}
	
	/**
	 * Checks to make sure that a player with Draconic Leap active is not on the ground
	 * @param player the player to check
	 * @return true/false if the player meets the requirements for a Draconic Leap
	 */
	private void draconicLeapCheck(Player player) {
		if ((player.isOnGround() && player.getFallDistance() == 0) || player.isGliding()) { player.removeScoreboardTag("DRACONIC_LEAP"); }
	}
}
