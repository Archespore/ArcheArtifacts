package jp.archeartifacts.main.listeners.player;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import jp.archeartifacts.main.helpers.InventoryHelper;
import jp.archeartifacts.main.helpers.PotionEffectHelper;

public class PlayerToggleSneakListener implements Listener {

	@EventHandler
	public void playerToggleSneakEvent(PlayerToggleSneakEvent event) {
		
		Player eventPlayer = event.getPlayer();
		
		if ((!eventPlayer.isSneaking()) && (InventoryHelper.containsMagicalItem(eventPlayer, Material.RABBIT_FOOT))) {
			PotionEffect newEffect = PotionEffectHelper.comparePotionEffect(new PotionEffect(PotionEffectType.JUMP, 15, 2), eventPlayer);
			eventPlayer.addPotionEffect(newEffect, true);
		}
	}
}
