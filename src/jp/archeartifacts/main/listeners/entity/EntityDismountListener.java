package jp.archeartifacts.main.listeners.entity;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffectType;
import org.spigotmc.event.entity.EntityDismountEvent;

public class EntityDismountListener implements Listener {

	@EventHandler
	public void entityDismountEvent(EntityDismountEvent event) {
		
		Entity eventMounted = event.getDismounted();
		Entity eventMounter = event.getEntity();
		
		if ((eventMounted instanceof Horse) && (eventMounter instanceof Player)) {
			
			//Removes the speed from the horse if the potion duration is greater than 10 minutes
			Horse eventHorse = (Horse)eventMounted;
			if ((eventHorse.hasPotionEffect(PotionEffectType.SPEED)) && (eventHorse.getPotionEffect(PotionEffectType.SPEED).getDuration() > 12000)) { eventHorse.removePotionEffect(PotionEffectType.SPEED); }
			if ((eventHorse.hasPotionEffect(PotionEffectType.REGENERATION)) && (eventHorse.getPotionEffect(PotionEffectType.REGENERATION).getDuration() > 12000)) { eventHorse.removePotionEffect(PotionEffectType.REGENERATION); }
		}
	}
}
