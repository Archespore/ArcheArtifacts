package jp.archeartifacts.main.listeners.player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import jp.archeartifacts.main.ArcheArtifacts;
import jp.archeartifacts.main.helpers.InventoryHelper;
import jp.archeartifacts.main.helpers.ItemStackHelper;
import jp.archeartifacts.main.helpers.PotionEffectHelper;

public class PlayerItemConsumeListener implements Listener {
	
	@EventHandler
	public void playerItemConsumeEvent(PlayerItemConsumeEvent event) {
		
		ItemStack eventItem = event.getItem();
		Player eventPlayer = event.getPlayer();
		
		if (ItemStackHelper.isMagicalItem(eventItem)) {
			
			//Mysterious Potion, grants random potion effect upon drinking.
			if (eventItem.getType().equals(Material.POTION)) {
				List<PotionEffectType> potionEffects = new ArrayList<>(Arrays.asList(PotionEffectType.values()));
				PotionEffectType newEffect = potionEffects.get(ThreadLocalRandom.current().nextInt(potionEffects.size()));
				int amplifier = ThreadLocalRandom.current().nextInt(0, 2);
				int duration = ThreadLocalRandom.current().nextInt(600, 1200);
				if (newEffect.equals(PotionEffectType.HEAL) || newEffect.equals(PotionEffectType.HARM)){
					amplifier = 0;
					duration = duration/2;
				}
				eventPlayer.addPotionEffect(PotionEffectHelper.comparePotionEffect(new PotionEffect(newEffect, duration, amplifier), eventPlayer), true);
				event.setCancelled(true);
			}
			
			//Infinity Cookie, never goes away and is better than a regular cookie.
			else if (eventItem.getType().equals(Material.COOKIE)){
				eventPlayer.addPotionEffect(PotionEffectHelper.comparePotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 0), eventPlayer), true);
				eventPlayer.setFoodLevel(eventPlayer.getFoodLevel() + 2);
				eventPlayer.setSaturation(eventPlayer.getSaturation() + 2);
				new BukkitRunnable() {
					@Override
					public void run() {
						ItemStack itemToAdd = eventItem.clone();
						itemToAdd.setAmount(1);
						eventPlayer.getInventory().addItem(itemToAdd);
					}
				}.runTask(ArcheArtifacts.getPlugin());
			}
		}
		
		//Sugar Rush, grants attack speed and movement speed upon eating, also makes food a little better.
		if (InventoryHelper.containsMagicalItem(eventPlayer, Material.SUGAR)) {
			eventPlayer.addPotionEffect(PotionEffectHelper.comparePotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 100, 0), eventPlayer), true);
			eventPlayer.addPotionEffect(PotionEffectHelper.comparePotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 0), eventPlayer), true);
			eventPlayer.setFoodLevel(eventPlayer.getFoodLevel() + 1);
			eventPlayer.setSaturation(eventPlayer.getSaturation() + 1);
		}
	}
}
