package jp.archeartifacts.main.listeners.entity;

import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import jp.archeartifacts.main.helpers.InventoryHelper;
import jp.archeartifacts.main.helpers.LivingEntityHelper;
import jp.archeartifacts.main.helpers.ParticleHelper;
import jp.archeartifacts.main.helpers.PotionEffectHelper;

public class EntityDamageByEntityListener implements Listener {

	@EventHandler
	public void entityDamageByEntityEvent(EntityDamageByEntityEvent event) {
		
		Entity eventDefender = event.getEntity();
		Entity eventAttacker = event.getDamager();
		Double damageAmount = event.getDamage();
		
		//Make sure both parties involved are living entities
		if ((eventDefender instanceof LivingEntity) && (eventAttacker instanceof LivingEntity)) {
			
			LivingEntity livingDefender = (LivingEntity)eventDefender;
			LivingEntity livingAttacker = (LivingEntity)eventAttacker;
			
			Location defenderLocation = livingDefender.getLocation().clone();
			Location attackerLocation = livingAttacker.getLocation().clone();
			
			if (livingAttacker instanceof Player) {
				
				Player playerAttacker = (Player)livingAttacker;
				
				//Get Information related to player location
				Block playerBlock = attackerLocation.getBlock();
				World playerWorld = playerAttacker.getWorld();
				byte skyLight = playerAttacker.getEyeLocation().getBlock().getLightFromSky();
				long worldTime = playerWorld.getTime();
				
				// Items that reduce damage and increase damage depending on location
				if (skyLight >= 15) {
					// Lively Sunflower
					if ((worldTime >= 0) && (worldTime < 12000)) {
						if (InventoryHelper.containsMagicalItem(playerAttacker, Material.SUNFLOWER)) { damageAmount += 1; }
					}
					// Eye of Night
					else {
						if (InventoryHelper.containsMagicalItem(playerAttacker, Material.FERMENTED_SPIDER_EYE)) { damageAmount += 1; }
					}
				}
				// Miner's Blessing
				else if (skyLight <= 0) {
					if (InventoryHelper.containsMagicalItem(playerAttacker, Material.CLAY_BALL)) { damageAmount += 1; }
				}
				// Soul of the Ocean
				if (playerBlock.getType().equals(Material.WATER)) {
					if (InventoryHelper.containsMagicalItem(playerAttacker, Material.PRISMARINE_SHARD)) { damageAmount += 1; }
				}
				
				//Heart of a Cannibal, gives a chance to restore hunger and health upon attacking.
				if (InventoryHelper.containsMagicalItem(playerAttacker, Material.RED_DYE)) {
					boolean cannibalActivated = false;
					if (ThreadLocalRandom.current().nextDouble(100.0) < 20.0) {
						if (playerAttacker.getFoodLevel() >= 20) { playerAttacker.setSaturation(Math.min(playerAttacker.getSaturation() + 2, 20)); }
						else { playerAttacker.setFoodLevel(Math.min(playerAttacker.getFoodLevel() + 2, 20)); }
						cannibalActivated = true;
					}
					if (ThreadLocalRandom.current().nextDouble(100.0) < 10.0) {
						LivingEntityHelper.addHealth(playerAttacker, 1);
						cannibalActivated = true;
					}
					if (cannibalActivated) { playerAttacker.playSound(playerAttacker.getLocation(), Sound.ENTITY_GENERIC_EAT, .75f, .5f); }
				}
				
				//Adrenaline, gives attack speed on hits
				if (InventoryHelper.containsMagicalItem(playerAttacker, Material.BLAZE_POWDER)) {
					PotionEffect newEffect = PotionEffectHelper.comparePotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 30, 0), playerAttacker);
					playerAttacker.addPotionEffect(newEffect, true); 
				}
				
				//Chase down, gives speed upon hitting an entity.
				if (InventoryHelper.containsMagicalItem(playerAttacker, Material.FURNACE_MINECART)) {
					PotionEffect newEffect = PotionEffectHelper.comparePotionEffect(new PotionEffect(PotionEffectType.SPEED, 30, 0), playerAttacker);
					playerAttacker.addPotionEffect(newEffect, true);
				}
				
				//Smoke screen, applies blindness on attacks in exchange for xp
				if ( (InventoryHelper.containsMagicalItem(playerAttacker, Material.CHARCOAL)) && ((playerAttacker.getExp() > 0) || (playerAttacker.getLevel() > 0)) ) {
					playerAttacker.giveExp(-5);
					livingDefender.addPotionEffect(PotionEffectHelper.comparePotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 50, 0), playerAttacker), true);
					livingDefender.addPotionEffect(PotionEffectHelper.comparePotionEffect(new PotionEffect(PotionEffectType.SLOW, 50, 0), playerAttacker), true);
					ParticleHelper.spawnWorldParticles(Particle.SMOKE_NORMAL, defenderLocation.clone().add(0, 1, 0), 10, .25, .5, .25, .1);
					playerWorld.playSound(livingDefender.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, .75f, .75f);
				}
				
				//Divine Intervention, increases damage based on 8% of defender's max health
				if (InventoryHelper.containsMagicalItem(playerAttacker, Material.NETHER_STAR)) { damageAmount += .08 * (livingDefender.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()); }
				
				//Wolf's Bone, prevents you from damaging your own wolves.
				if ((eventDefender instanceof Wolf) && (InventoryHelper.containsMagicalItem(playerAttacker, Material.BONE))) {
					Wolf eventWolfDefender = (Wolf)eventDefender;
					if ((eventWolfDefender.isTamed()) && (eventWolfDefender.getOwner().equals(playerAttacker))) { event.setCancelled(true); }
				}
				
				//Wither Skull, Applies wither to hit entities.
				if (InventoryHelper.wearingMagicalItem(playerAttacker, Material.WITHER_SKELETON_SKULL)) {
					PotionEffect newEffect = PotionEffectHelper.comparePotionEffect(new PotionEffect(PotionEffectType.WITHER, 200, 0), playerAttacker);
					livingDefender.addPotionEffect(newEffect, true);
				}
				
				//Archespore's Head, kills entities below 25% health.
				if (InventoryHelper.wearingMagicalItem(playerAttacker, Material.PLAYER_HEAD)) {
					if (livingDefender.getHealth() < (livingDefender.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue())/4) {
						livingDefender.setHealth(0);
					}
				}
			}
			
			if (eventDefender instanceof Player) {
				
				Player playerDefender = (Player) eventDefender;
				
				//Countering Stone, has a chance to deal damage back to the attacker.
				if (InventoryHelper.containsMagicalItem(playerDefender, Material.FLINT)) {
					if (ThreadLocalRandom.current().nextDouble(100.0) < 20.0) {
						double reflectDamage = event.getDamage() / 4;
						if ((livingAttacker instanceof Player) && (InventoryHelper.containsMagicalItem((Player)livingAttacker, Material.QUARTZ))) { reflectDamage /= 2; }
						livingAttacker.damage(reflectDamage, playerDefender);
					}
				}
			}
		}
		
		//Update event damage after modifiers
		event.setDamage(damageAmount);
	}
}
