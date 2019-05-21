package jp.archeartifacts.main.listeners.player;

import java.util.Arrays;
import java.util.Collection;
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
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.AbstractArrow.PickupStatus;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import jp.archeartifacts.main.ArcheArtifacts;
import jp.archeartifacts.main.helpers.InventoryHelper;
import jp.archeartifacts.main.helpers.ItemStackHelper;
import jp.archeartifacts.main.helpers.LivingEntityHelper;
import jp.archeartifacts.main.helpers.ParticleHelper;
import jp.archeartifacts.main.helpers.PotionEffectHelper;

public class PlayerInteractListener implements Listener {
	
	@EventHandler
	public void playerInteractEvent(PlayerInteractEvent event){
		
		//Variable gathering
		Player eventPlayer = event.getPlayer();
		Action eventAction = event.getAction();
		ItemStack eventItem = event.getItem();
		
		//First check to make sure the held item is not null
		if (eventItem != null) {
			
			Material eventMaterial = eventItem.getType();
			//Check to make sure this action is us right clicking
			if (eventAction.equals(Action.RIGHT_CLICK_AIR) || eventAction.equals(Action.RIGHT_CLICK_BLOCK)) {
				
				//Skeleton Head, allows you to fire arrows without charging.
				if (eventMaterial.equals(Material.BOW)) {
					if (InventoryHelper.wearingMagicalItem(eventPlayer, Material.SKELETON_SKULL)) {
						Inventory playerInventory = eventPlayer.getInventory();
						if (playerInventory.contains(Material.ARROW)) {
							//Arrow variable setting
							Location playerLocation = eventPlayer.getLocation();
							Arrow newArrow = eventPlayer.launchProjectile(Arrow.class, playerLocation.getDirection().multiply(3.0));
							newArrow.setDamage(2.0 + eventItem.getEnchantmentLevel(Enchantment.ARROW_DAMAGE));
							newArrow.setFireTicks((eventItem.containsEnchantment(Enchantment.ARROW_FIRE)) ? 2000 : 0);
							newArrow.setKnockbackStrength(eventItem.getEnchantmentLevel(Enchantment.ARROW_KNOCKBACK));
							newArrow.setCritical(true);
							
							//Cancel the event to prevent the user from charging the bow and damage/remove item
							eventPlayer.getWorld().playSound(playerLocation, Sound.ENTITY_ARROW_SHOOT, .75f, 1.25f);
							if (!eventItem.containsEnchantment(Enchantment.ARROW_INFINITE)) { playerInventory.removeItem(new ItemStack(Material.ARROW, 1)); }
							else { newArrow.setPickupStatus(PickupStatus.CREATIVE_ONLY); }
							ItemStackHelper.damageItem(eventItem, 2);
							event.setCancelled(true);
						}
					}
				}
				
				//If we are right clicking with a magical item, perform an action if it exists
				else if ( (ItemStackHelper.isMagicalItem(eventItem)) && ((!eventMaterial.isEdible()) && (!eventMaterial.equals(Material.POTION))) ) {
					
					ItemStackHelper.switchActivation(eventItem);
					
					//Variable gathering
					Material itemType = eventItem.getType();
					Inventory playerInventory = eventPlayer.getInventory();
					World playerWorld = eventPlayer.getWorld();
					Location playerLocation = eventPlayer.getLocation().clone();
					int playerLevel = eventPlayer.getLevel();
					
					//First we cancel the event to make sure the player doesn't "use" it by accident.
					event.setCancelled(true);
					switch (itemType) {
						case GLOWSTONE:
							//Trades Emeralds and levels to gamble
							if (playerInventory.contains(Material.EMERALD, 3)) {
								if (playerLevel > 0 && eventPlayer.getCooldown(itemType) <= 0){
									
									//Initial deductions and variable setting
									eventPlayer.giveExpLevels(-1);
									playerInventory.removeItem(new ItemStack(Material.EMERALD, 3));
									eventPlayer.setCooldown(itemType, 30);
									playerWorld.playSound(playerLocation, Sound.BLOCK_IRON_DOOR_CLOSE, 1.0f, 1.5f);
									
									new BukkitRunnable() {
										int loopValue = 0;
										//List of possible prizes
										List<Material> prizes = Arrays.asList(Material.GOLD_INGOT, Material.IRON_INGOT, Material.LAPIS_LAZULI, Material.DIRT, Material.DIAMOND);
										
										@Override
										public void run() {
											if (loopValue >= 6) {
												playerWorld.dropItemNaturally(eventPlayer.getLocation().clone().add(0, 1, 0), new ItemStack(prizes.get(ThreadLocalRandom.current().nextInt(prizes.size())), 1));
												Bukkit.getScheduler().cancelTask(this.getTaskId());
											}
											else {
												playerWorld.playSound(eventPlayer.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, .33f, .75f + (.25f * loopValue));
												loopValue++;
											}
										}
									}.runTaskTimer(ArcheArtifacts.getPlugin(), 2, 4);
								}
							}
							break;
						case BLAZE_ROD:
							//Turns iron nuggets to gold nuggets via health and hunger.
							if (playerInventory.contains(Material.IRON_NUGGET)) {
								
								//Variable gathering and setting
								double damage = .125;
								float playerSaturation = eventPlayer.getSaturation();
								int playerFood = eventPlayer.getFoodLevel();
								
								if (playerSaturation > 0) { eventPlayer.setSaturation(Math.max(0, --playerSaturation)); }
								else if (playerFood > 0) { eventPlayer.setFoodLevel(Math.max(0, --playerFood)); }
								else { damage *= 2; }
								
								LivingEntityHelper.removeHealth(eventPlayer, damage);
								playerWorld.playSound(playerLocation, Sound.BLOCK_ANVIL_LAND, .2f, 2.0f);
								playerInventory.removeItem(new ItemStack(Material.IRON_NUGGET, 1));
								playerWorld.dropItemNaturally(playerLocation.add(0, 1, 0), new ItemStack(Material.GOLD_NUGGET, 1));
							}
							break;
						case SHULKER_SHELL:
							//Opens player's enderchest.
							eventPlayer.playSound(playerLocation, Sound.BLOCK_ENDER_CHEST_OPEN, .5f, 1.0f);
							eventPlayer.openInventory(eventPlayer.getEnderChest());
							break;
						case DANDELION:
							//Removes all potion effects from player in exchange for a level.
							Collection<PotionEffect> potionEffects = eventPlayer.getActivePotionEffects();
							if (playerLevel > 0 && potionEffects.size() > 0){
								eventPlayer.giveExpLevels(-1);
								eventPlayer.playSound(playerLocation, Sound.ENTITY_ZOMBIE_VILLAGER_CONVERTED, .75f, 1.5f);
								potionEffects.forEach(effect -> eventPlayer.removePotionEffect(effect.getType()));
							}
							break;
						case OAK_SAPLING:
							//Nature's Blessing, Trades life and levels in exchange for a buff area.
							if ( (playerLevel >= 3) && (eventPlayer.getCooldown(Material.OAK_SAPLING) <= 0) ) {
								//Initial deductions and variable setting
								eventPlayer.giveExpLevels(-3);
								LivingEntityHelper.removeHealth(eventPlayer, 5);
								eventPlayer.setCooldown(itemType, 600);
								
								ArcheArtifacts.getPlugin().getNatureController().addBlessing(playerLocation, 30);
								playerWorld.playSound(playerLocation, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, .75f);
							}
							break;
						case LILY_OF_THE_VALLEY:
							//Flower of Immortality, prevents players from dying within an area.
							if ( (playerLevel >= 3) && (eventPlayer.getCooldown(Material.LILY_OF_THE_VALLEY) <= 0) && (playerInventory.contains(Material.DIAMOND)) ) {
								//Initial deductions and variable setting
								eventPlayer.giveExpLevels(-3);
								playerInventory.removeItem(new ItemStack(Material.DIAMOND, 1));
								LivingEntityHelper.removeHealth(eventPlayer, 5);
								eventPlayer.setCooldown(itemType, 1200);
								
								ArcheArtifacts.getPlugin().getImmortalityController().addBlessing(playerLocation, 600);
								playerWorld.playSound(playerLocation, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.25f);
							}
							break;
						case STICK:
							//Grants the player pseudo-flight for a short period.
							if ((playerLevel >= 1) && (eventPlayer.getCooldown(Material.STICK) <= 0)) {
								
								eventPlayer.setCooldown(Material.STICK, 600);
								eventPlayer.giveExpLevels(-1);
								new BukkitRunnable() {
									
									int loopValue = 0;
									
									public void run() {
										eventPlayer.setFallDistance(-5);
										if (loopValue >= 10) {
											Bukkit.getScheduler().cancelTask(this.getTaskId());
										}
										else {
											playerWorld.playSound(eventPlayer.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, .5f, 1.5f);
											eventPlayer.setVelocity(eventPlayer.getLocation().getDirection().multiply(1.5));
											loopValue++;
										}
									}
								}.runTaskTimer(ArcheArtifacts.getPlugin(), 1, 4);
							}
							break;
						case EMERALD:
							//Grants the player luck for a short period.
							if (eventPlayer.getCooldown(Material.EMERALD) <= 0) {
								boolean luckActive = eventPlayer.hasPotionEffect(PotionEffectType.LUCK);
								int effectAmplifier = 0;
								if ((luckActive) && (playerLevel >= 2) && (playerInventory.contains(Material.EMERALD_BLOCK, 2))) {
									
									//Potion effect gathering, determines if we should overwrite the existing effect or not
									PotionEffect existingEffect = eventPlayer.getPotionEffect(PotionEffectType.LUCK);
									int existingEffectAmp = existingEffect.getAmplifier();
									PotionEffect newEffect = new PotionEffect(PotionEffectType.LUCK, 1200, 1);
									int newEffectAmp = newEffect.getAmplifier();
									if ( (newEffectAmp > existingEffectAmp) || ((newEffectAmp == existingEffectAmp) && (newEffect.getDuration() > existingEffect.getDuration())) ) { effectAmplifier = 2; }
								}
								else if ((playerLevel >= 1) && (playerInventory.contains(Material.EMERALD_BLOCK))) { effectAmplifier = 1; }
								
								//If the effect amplifier is not 0, so we are doing an action for this event...
								if (effectAmplifier > 0) {
									//Initial deductions and variable setting
									eventPlayer.giveExpLevels(-effectAmplifier);
									playerInventory.removeItem(new ItemStack(Material.EMERALD_BLOCK, effectAmplifier));
									eventPlayer.setCooldown(itemType, 300);
									playerWorld.playSound(playerLocation, Sound.ENTITY_ENDER_EYE_DEATH, 1.0f, 1.75f);
									
									eventPlayer.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, 1200, effectAmplifier - 1, true), true);
								}
							}
							break;
						case END_PORTAL_FRAME:
							//Void Walk, teleports to nearby entities and damages them.
							if ((eventPlayer.getCooldown(Material.END_PORTAL_FRAME) <= 0) && (playerLevel >= 1) && (playerInventory.contains(Material.ENDER_PEARL, 3))) {
								
								//Get entity list for this effect
								List<Entity> damageEntities = eventPlayer.getNearbyEntities(16, 16, 16);
								damageEntities.removeIf(entity -> !(entity instanceof LivingEntity) || (entity.getLocation().distance(playerLocation) > 16));
								if (damageEntities.size() > 0) {
									//Initial deductions and variable setting
									eventPlayer.giveExpLevels(-1);
									playerInventory.removeItem(new ItemStack(Material.ENDER_PEARL, 3));
									eventPlayer.setCooldown(itemType, 300);
									playerWorld.playSound(playerLocation, Sound.BLOCK_PORTAL_TRIGGER, .75f, .75f);
								
									//Actual effects
									new BukkitRunnable() {
										int loopValue = 0;
										public void run() {
											if (loopValue >= damageEntities.size()) {
												Bukkit.getScheduler().cancelTask(this.getTaskId());
												eventPlayer.teleport(playerLocation);
											}
											else {
												LivingEntity loopEntity = (LivingEntity)(damageEntities.get(loopValue));
												eventPlayer.teleport(loopEntity);
												playerWorld.playSound(eventPlayer.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, .75f, 1.25f);
												loopEntity.damage(eventPlayer.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getValue(), eventPlayer);
												PotionEffect newEffect = PotionEffectHelper.comparePotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 5, 5), eventPlayer);
												eventPlayer.addPotionEffect(newEffect, true);
												loopValue++;
											}
										}
									}.runTaskTimer(ArcheArtifacts.getPlugin(), 100, 2);
								}
							}
							break;
						case END_CRYSTAL:
							//Banner of Command, After accelerating for a few seconds, gain a burst of speed and resistance
							if ((eventPlayer.getCooldown(Material.END_CRYSTAL) <= 0) && (playerLevel >= 1) && (playerInventory.contains(Material.GOLD_INGOT))) {
								
								//Initial deductions and variable setting
								eventPlayer.giveExpLevels(-1);
								playerInventory.removeItem(new ItemStack(Material.GOLD_INGOT, 1));
								eventPlayer.setCooldown(itemType, 300);
								
								//Actual effects
								new BukkitRunnable() {
									
									//Variable initialization
									Player commander = eventPlayer;
									int loopValue = 0;
									
									public void run() {
										Location commanderLocation = commander.getLocation().clone();
										List<Entity> nearbyEntities = commander.getNearbyEntities(12.0, 12.0, 12.0);
										nearbyEntities.removeIf(entity -> (!(entity instanceof LivingEntity) || (entity.getLocation().distance(commanderLocation) > 12.0)));
										nearbyEntities.add(eventPlayer);
										//If this this the final loop, we add resistance instead of speed
										if (loopValue >= 5) {
											Bukkit.getScheduler().cancelTask(this.getTaskId());
											nearbyEntities.forEach(entity -> {
												PotionEffect newEffect = PotionEffectHelper.comparePotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 300, 0), eventPlayer);
												eventPlayer.addPotionEffect(newEffect, true);
											});
											commander.getWorld().playSound(commanderLocation, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
											ParticleHelper.spawnWorldParticles(Particle.EXPLOSION_HUGE, commanderLocation.add(0, 1, 0), 1);
											ParticleHelper.spawnParticleRing(Particle.REDSTONE, commander.getLocation(), 12.0, 72, 2, 0, new DustOptions(Color.WHITE, 1.5f));
										}
										//If this is not the final loop, we are adding speed to all entities
										else {
											commander.getWorld().playSound(commanderLocation, Sound.ENTITY_EVOKER_CAST_SPELL, 1.0f, (float) (1.0 + (loopValue * .25)));
											ParticleHelper.spawnParticleRing(Particle.REDSTONE, commander.getLocation(), 12.0, 72, 2, 0, new DustOptions(Color.WHITE, 1.5f));
											nearbyEntities.forEach(entity -> {
												LivingEntity livingEntity = (LivingEntity)entity;
												PotionEffect newEffect = PotionEffectHelper.comparePotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, loopValue), livingEntity);
												livingEntity.addPotionEffect(newEffect, true);
												ParticleHelper.spawnWorldParticles(Particle.END_ROD, livingEntity.getLocation().clone().add(0, 1, 0), 5 + (loopValue * 5), .25, .25, .25, .05);
											});
											loopValue++;
										}
										
									}
								}.runTaskTimer(ArcheArtifacts.getPlugin(), 0, 30);
							}
							break;
						case WITHER_ROSE:
							//Wither rose, attacks nearby players and entities with slow, blindness, and wither skulls
							if ((eventPlayer.getCooldown(Material.WITHER_ROSE) <= 0) && (playerLevel >= 1) && (playerInventory.contains(Material.GOLD_BLOCK))) {
								
								//Initial deductions and variable setting
								eventPlayer.giveExpLevels(-1);
								playerInventory.removeItem(new ItemStack(Material.GOLD_BLOCK, 1));
								eventPlayer.setCooldown(itemType, 600);
								PotionEffect newWither = PotionEffectHelper.comparePotionEffect(new PotionEffect(PotionEffectType.WITHER, 120, 1), eventPlayer);
								eventPlayer.addPotionEffect(newWither, true);
								playerWorld.playSound(playerLocation, Sound.ENTITY_WITHER_AMBIENT, 1.0f, .5f);
								ParticleHelper.spawnParticleRing(Particle.REDSTONE, playerLocation, 16.0, 128, 2, 0, new DustOptions(Color.BLACK, 1.5f));
								
								List<Entity> nearbyEntities = eventPlayer.getNearbyEntities(16.0, 16.0, 16.0);
								nearbyEntities.removeIf(entity -> (!(entity instanceof LivingEntity) || (entity.getLocation().distance(playerLocation) > 16.0)));
								nearbyEntities.forEach(entity -> {
									LivingEntity livingEntity = (LivingEntity)entity;
									eventPlayer.launchProjectile(WitherSkull.class, livingEntity.getEyeLocation().toVector().subtract(eventPlayer.getEyeLocation().toVector()).normalize().multiply(1.5));
									PotionEffect newBlindness = PotionEffectHelper.comparePotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 300, 0), livingEntity);
									PotionEffect newSlowness = PotionEffectHelper.comparePotionEffect(new PotionEffect(PotionEffectType.SLOW, 300, 1), livingEntity);
									livingEntity.addPotionEffect(newBlindness, true);
									livingEntity.addPotionEffect(newSlowness, true);
								});
							}
							break;
						case DRAGON_EGG:
							//Draconic Leap, jumps high into the air and pounds the ground upon landing
							if ((eventPlayer.getCooldown(Material.DRAGON_EGG) <= 0) && (playerLevel >= 1) && (playerInventory.contains(Material.DIAMOND))) {
								
								//Initial deductions and variable setting
								eventPlayer.giveExpLevels(-1);
								playerInventory.removeItem(new ItemStack(Material.DIAMOND, 1));
								eventPlayer.setCooldown(itemType, 600);
								
								//Actual launch event
								Vector currentVelocty = eventPlayer.getVelocity();
								eventPlayer.setVelocity(new Vector(currentVelocty.getX(), 2.4, currentVelocty.getZ()));
								new BukkitRunnable() {
									@Override
									public void run() {
										eventPlayer.addScoreboardTag("DRACONIC_LEAP");
									}
								}.runTaskLater(ArcheArtifacts.getPlugin(), 2);
							}
							break;
						default:
							break;
					}
				}
			}
		}
	}
}
