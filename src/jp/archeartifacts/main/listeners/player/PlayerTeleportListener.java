package jp.archeartifacts.main.listeners.player;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import jp.archeartifacts.main.helpers.InventoryHelper;
import jp.archeartifacts.main.helpers.PotionEffectHelper;

public class PlayerTeleportListener implements Listener {

	@EventHandler
	public void playerTeleportEvent(PlayerTeleportEvent event){
		
		//Ender Pearl Mastery, prevents enderpeal damage and gives speed and resistance upon teleport
		Player eventPlayer = event.getPlayer();
		if (event.getCause().equals(TeleportCause.ENDER_PEARL)){
			if (InventoryHelper.containsMagicalItem(eventPlayer, Material.ENDER_PEARL)) {
				event.setCancelled(true);
				eventPlayer.teleport(event.getTo());
				
				PotionEffect newPotionEffect = PotionEffectHelper.comparePotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 0), eventPlayer);
				eventPlayer.addPotionEffect(newPotionEffect, true);
				
				newPotionEffect = PotionEffectHelper.comparePotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 100, 0), eventPlayer);
				eventPlayer.addPotionEffect(newPotionEffect, true);
			}
		}
	}
}
