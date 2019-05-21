package jp.archeartifacts.main.controllers;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import jp.archeartifacts.main.helpers.ParticleHelper;

public class ImmortalityFieldController {

	Map<Location, Integer> blessingLocations;
	
	/**
	 * Default constructor
	 */
	public ImmortalityFieldController() {
		blessingLocations = new HashMap<>();
	}
	
	/**
	 * Loops through all registered
	 */
	public void loopBlessings() {
		blessingLocations.entrySet().removeIf(entry -> entry.getValue() <= 0);
		blessingLocations.forEach( (location, duration) -> {
			World world = location.getWorld();
			ParticleHelper.spawnParticleRing(Particle.HEART, location, 8.0, 64, 1);
			Collection<Entity> nearbyEntites = world.getNearbyEntities(location, 8, 8, 8);
			nearbyEntites.removeIf(entity -> !(entity instanceof LivingEntity));
			for (Entity entity : nearbyEntites){
				LivingEntity livingEntity = (LivingEntity)entity;
				if (livingEntity.getLocation().distance(location) <= 8){
					livingEntity.addScoreboardTag("IMMORTAL");
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
