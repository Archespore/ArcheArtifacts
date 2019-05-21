package jp.archeartifacts.main.listeners.entity;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.spigotmc.event.entity.EntityMountEvent;

import jp.archeartifacts.main.helpers.InventoryHelper;
import jp.archeartifacts.main.helpers.PotionEffectHelper;

public class EntityMountListener implements Listener{

	@EventHandler
	public void entityMountEvent(EntityMountEvent event) {
		
		Entity eventMounted = event.getMount();
		Entity eventMounter = event.getEntity();
		
		if ((eventMounted instanceof Horse) && (eventMounter instanceof Player)){
			
			Player eventplayer = (Player)eventMounter;
			Horse eventHorse = (Horse)eventMounted;
			if(InventoryHelper.containsMagicalItem(eventplayer, Material.SADDLE)) {
				eventHorse.addPotionEffect(PotionEffectHelper.comparePotionEffect(new PotionEffect(PotionEffectType.SPEED, 864000, 1), eventHorse), true);
				eventHorse.addPotionEffect(PotionEffectHelper.comparePotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 864000, 0), eventHorse), true);
			}
		}
	}
}
