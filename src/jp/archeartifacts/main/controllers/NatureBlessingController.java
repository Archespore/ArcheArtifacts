package jp.archeartifacts.main.controllers;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import jp.archeartifacts.main.helpers.LivingEntityHelper;
import jp.archeartifacts.main.helpers.ParticleHelper;
import jp.archeartifacts.main.helpers.PotionEffectHelper;

public class NatureBlessingController {

	Map<Location, Integer> blessingLocations;
	
	/**
	 * Default constructor
	 */
	public NatureBlessingController() {
		blessingLocations = new HashMap<>();
	}
	
	/**
	 * Loops through all registered
	 */
	public void loopBlessings() {
		blessingLocations.entrySet().removeIf(entry -> entry.getValue() <= 0);
		blessingLocations.forEach( (location, duration) -> {
			World world = location.getWorld();
			ParticleHelper.spawnParticleRing(Particle.VILLAGER_HAPPY, location, 8.0, 72, 2);
			Collection<Entity> nearbyEntites = world.getNearbyEntities(location, 8, 8, 8);
			nearbyEntites.removeIf(entity -> !(entity instanceof Player));
			for (Entity entity : nearbyEntites){
				Player player = (Player)entity;
				if (player.getLocation().distance(location) <= 8){
					LivingEntityHelper.addHealth(player, .4);
					player.addPotionEffect(PotionEffectHelper.comparePotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 40, 0), player), true);
					player.addPotionEffect(PotionEffectHelper.comparePotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 40, 0), player), true);
					player.addPotionEffect(PotionEffectHelper.comparePotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 40, 0), player), true);
					player.addPotionEffect(PotionEffectHelper.comparePotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 40, 0), player), true);
				}
			}
			blessingLocations.put(location, --duration);
		});
	}
	
	/**
	 * Adds a blessing at the specified location to the controller
	 * @param location the location of the blessing
	 * @param duration the duration of the blessing
	 */
	public void addBlessing(Location location, int duration) {
		blessingLocations.put(location, duration);
	}
	
	/**
	 * Removes a blessing at the specified location from the controller
	 * @param location the location of the blessing
	 */
	public void removeBlessing(Location location) {
		blessingLocations.remove(location);
	}
}
