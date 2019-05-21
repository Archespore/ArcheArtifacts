package jp.archeartifacts.main.listeners.entity;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import jp.archeartifacts.main.ArcheArtifacts;
import jp.archeartifacts.main.controllers.ArtifactController;
import jp.archeartifacts.main.helpers.InventoryHelper;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class EntityDeathListener implements Listener {

	@EventHandler
	public void entityDeathEvent(EntityDeathEvent event) {
		
		LivingEntity eventEntity = event.getEntity();
		Player eventKiller = eventEntity.getKiller();
		List<ItemStack> itemDrops = event.getDrops();
		int expDrop = event.getDroppedExp();
		
		if (eventKiller != null) {
			Inventory playerInventory = eventKiller.getInventory();
			
			//Check if an artifact should be generated on this mob death
			ArtifactController artifactController = ArcheArtifacts.getPlugin().getArtifactController();
			if ( (artifactController.mobDeathEnabled) && (eventEntity instanceof Monster) ) {
				double artifactChance = artifactController.mobDeathChance;
				if ( (artifactController.mobDeathSpawnerNerf) && (eventEntity.hasMetadata("SPAWNER_MOB")) ) { artifactChance /= 2; }
				if (ThreadLocalRandom.current().nextDouble(100.0) < artifactChance) {
					itemDrops.add(artifactController.generateArtifact());
					eventKiller.playSound(eventKiller.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, .5f);
					eventKiller.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.GOLD + "The slain mob was carrying a magical item!"));
				}
			}
			
			//Midas' Rod, chance to drop gold nuggets when killing an entity.
			if (InventoryHelper.containsMagicalItem(eventKiller, Material.BLAZE_ROD)) {
				if (ThreadLocalRandom.current().nextDouble(100) < 20.0) {
					itemDrops.add(new ItemStack(Material.GOLD_NUGGET, 1));
				}
			}
			
			//Knowledge Fragment, doubles xp dropped in exchange for a lapis
			if ( (InventoryHelper.containsMagicalItem(eventKiller, Material.PAPER)) && (playerInventory.contains(Material.LAPIS_LAZULI)) ) {
				playerInventory.removeItem(new ItemStack(Material.LAPIS_LAZULI, 1));
				event.setDroppedExp(expDrop * 3);
			}
			
			//Ball of Slime, gives items to killer instead of dropping them.
			if (InventoryHelper.containsMagicalItem(eventKiller, Material.SLIME_BALL)) {
				eventKiller.giveExp(expDrop);
				for (ItemStack itemStack : itemDrops) {
					if (eventKiller.getInventory().firstEmpty() != -1) { eventKiller.getInventory().addItem(itemStack); }
					else { eventKiller.getWorld().dropItemNaturally(eventKiller.getLocation(), itemStack); }
				}
				itemDrops.clear();
				event.setDroppedExp(0);
			}
		}
	}
}
