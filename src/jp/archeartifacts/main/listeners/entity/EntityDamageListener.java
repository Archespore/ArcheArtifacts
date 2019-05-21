package jp.archeartifacts.main.listeners.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import jp.archeartifacts.main.helpers.InventoryHelper;
import jp.archeartifacts.main.helpers.LivingEntityHelper;
import jp.archeartifacts.main.helpers.ParticleHelper;
import jp.archeartifacts.main.helpers.PotionEffectHelper;

public class EntityDamageListener implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST)
	public void entityDamageEvent(EntityDamageEvent event) {

		Entity eventDefender = event.getEntity();
		DamageCause damageType = event.getCause();
		Double damageAmount = event.getDamage();

		// If the entity that was damaged is a player, check for magical items that
		// effect damage events
		if (eventDefender instanceof Player) {

			Player eventPlayer = (Player) eventDefender;
			
			if (InventoryHelper.containsMagicalItem(eventPlayer, Material.GHAST_TEAR)) {
				List<EntityType> entityTypes = new ArrayList<EntityType>(Arrays.asList(EntityType.values()));
				entityTypes.removeAll(Arrays.asList(EntityType.ARMOR_STAND, EntityType.CAVE_SPIDER, EntityType.MUSHROOM_COW, EntityType.TRADER_LLAMA, EntityType.GIANT));
				entityTypes.removeIf(type -> !type.isAlive());
				eventPlayer.getWorld().playSound(eventPlayer.getLocation(),
						Sound.valueOf("ENTITY_" + entityTypes.get(ThreadLocalRandom.current().nextInt(entityTypes.size())).getKey().getKey().toUpperCase() + "_HURT"),
						1.0f, 1.0f);
			}

			// Begin magical item damage modifiers check
			// Resistance crystal, reduces ALL damage by 1/4 of a heart before any other
			// modifiers.
			if (InventoryHelper.containsMagicalItem(eventPlayer, Material.QUARTZ)) {
				damageAmount -= .5;
			}

			// Get Information related to player location
			Location playerLocation = eventPlayer.getLocation();
			Block playerBlock = playerLocation.getBlock();
			byte skyLight = eventPlayer.getEyeLocation().getBlock().getLightFromSky();
			long worldTime = eventPlayer.getWorld().getTime();

			// Items that reduce damage and increase damage depending on location
			if (skyLight >= 15) {
				// Lively Sunflower
				if ((worldTime >= 0) && (worldTime < 12000)) {
					if (InventoryHelper.containsMagicalItem(eventPlayer, Material.SUNFLOWER)) { damageAmount -= 1; }
				}
				// Eye of Night
				else {
					if (InventoryHelper.containsMagicalItem(eventPlayer, Material.FERMENTED_SPIDER_EYE)) { damageAmount -= 1; }
				}
			}
			// Miner's Blessing
			else if (skyLight <= 0) {
				if (InventoryHelper.containsMagicalItem(eventPlayer, Material.CLAY_BALL)) { damageAmount -= 1; }
			}
			// Soul of the Ocean
			if (playerBlock.getType().equals(Material.WATER)) {
				if (InventoryHelper.containsMagicalItem(eventPlayer, Material.PRISMARINE_SHARD)) { damageAmount -= 1; }
			}

			// Zombie Head, reduces damage by 2% for each nearby living entity.
			if (InventoryHelper.wearingMagicalItem(eventPlayer, Material.ZOMBIE_HEAD)) {
				int reductionAmount = 0;
				for (Entity entity : eventPlayer.getNearbyEntities(12, 12, 12)) {
					if ((entity.getLocation().distance(playerLocation) <= 12) && (entity instanceof LivingEntity)) {
						reductionAmount += 2;
					}
				}
				// Reduction amount is capped at 50% for 25 nearby entities
				reductionAmount = Math.min(50, reductionAmount);
				damageAmount = (damageAmount * (1 - (reductionAmount * .01)));
			}

			switch (damageType) {
			// Feather o' Falling, prevents fall damage from exceeding 1/2 a heart.
			case FALL:
				if (eventPlayer.removeScoreboardTag("DRACONIC_LEAP")) { 
					
					/*We subtract 25 since that is the default fall distance from the jump, we only want extra fall distance
					We also need to make sure the fall distance doesn't somehow equal 0, because that returns infinity.*/
					double fallDistance = Math.floor((eventPlayer.getFallDistance() - 25.0));
					fallDistance = (fallDistance <= 0 ? 0 : fallDistance);
					//Cap the effect modifier at 5
					double effectModifier = Math.min(5, (1 + (Math.cbrt(Math.pow(fallDistance, 1.7)) / 6)));
					System.out.println(effectModifier);
					
					//Variable initializations
					event.setCancelled(true);
					double effectDistance = 8.0 * effectModifier;
					double effectLaunch = 1.0 * 1 + ((effectModifier - 1) / 2);
					
					//Damages and launches entities nearby
					for(Entity entity : eventPlayer.getNearbyEntities(effectDistance, effectDistance, effectDistance)) {
						Vector currentVelocty = entity.getVelocity();
						entity.setVelocity(new Vector(currentVelocty.getX(), effectLaunch, currentVelocty.getZ()));
						if (entity instanceof LivingEntity) {
							LivingEntity livingEntity = (LivingEntity)entity;
							LivingEntityHelper.removeHealth(livingEntity, 5);
							livingEntity.addPotionEffect(PotionEffectHelper.comparePotionEffect(new PotionEffect(PotionEffectType.SLOW, (int)(300 * effectModifier), 0), livingEntity), true);
						}
					}
					
					//Effects of the ability
					eventPlayer.getWorld().playSound(playerLocation, Sound.BLOCK_GRAVEL_BREAK, 1.5f, .5f);
					ParticleHelper.spawnParticleRing(Particle.BLOCK_CRACK, playerLocation.add(0, .5, 0), effectDistance, (int)(72 * effectModifier), 3, .25, Material.DIRT.createBlockData());
					ParticleHelper.spawnWorldParticles(Particle.BLOCK_CRACK, playerLocation, (int)(75 * effectModifier), effectDistance / 2, 0, effectDistance / 2, .25, Material.DIRT.createBlockData());
				}
				else if (InventoryHelper.containsMagicalItem(eventPlayer, Material.FEATHER)) { damageAmount = Math.min(damageAmount, 1); }
				break;
			// Holy Melon, reduces the below damages types by half, and fully prevents
			// starvation damage.
			case POISON:
			case WITHER:
			case STARVATION:
			case DRAGON_BREATH:
				if (InventoryHelper.containsMagicalItem(eventPlayer, Material.GLISTERING_MELON_SLICE)) {
					if (damageType == DamageCause.STARVATION) {
						event.setCancelled(true);
					} else {
						damageAmount /= 2;
					}
				}
				break;
			// Boiling Blood or Wither Skeleton Skull, prevents all fire/heat type damage.
			case FIRE:
			case FIRE_TICK:
			case LAVA:
			case HOT_FLOOR:
				if (InventoryHelper.containsMagicalItem(eventPlayer, Material.MAGMA_CREAM)
						|| InventoryHelper.wearingMagicalItem(eventPlayer, Material.WITHER_SKELETON_SKULL)) {
					event.setCancelled(true);
				}
				break;
			// Explosive powder, reduces all explosion damage by 33%.
			case BLOCK_EXPLOSION:
			case ENTITY_EXPLOSION:
				if (InventoryHelper.containsMagicalItem(eventPlayer, Material.GUNPOWDER)) {
					damageAmount /= 1.5;
				}
				break;
			// Tough leather, reduces the below damage by half.
			case CONTACT:
			case THORNS:
			case LIGHTNING:
			case FALLING_BLOCK:
				if (InventoryHelper.containsMagicalItem(eventPlayer, Material.LEATHER)) {
					damageAmount /= 2;
				}
				break;
			// Zombie head, heals from instant damage effects.
			case MAGIC:
				if (InventoryHelper.wearingMagicalItem(eventPlayer, Material.ZOMBIE_HEAD)) {
					LivingEntityHelper.addHealth(eventPlayer, event.getDamage());
					event.setCancelled(true);
				}
				break;
			default:
				break;
			}

			// Divine intervention, reduces damage by half if it exceeds 10% of current
			// health
			double playerHealth = eventPlayer.getHealth();
			double damageLimit = (playerHealth / 10);
			if ((InventoryHelper.containsMagicalItem(eventPlayer, Material.NETHER_STAR))
					&& (damageAmount >= damageLimit)) {
				double remainderDamage = damageAmount - damageLimit;
				damageAmount = damageLimit + (remainderDamage / 2);
			}
		}

		// Update event damage after modifiers
		event.setDamage(damageAmount);
		
		//Make sure entity isn't immortal
		if ( (eventDefender instanceof LivingEntity) && (eventDefender.getScoreboardTags().contains("IMMORTAL")) ) {
			LivingEntity livingDefender = (LivingEntity)eventDefender;
			if (livingDefender.getHealth() - event.getFinalDamage() <= 0) {
				livingDefender.setHealth(.01);
				event.setDamage(0);
			}
		}
	}
}
