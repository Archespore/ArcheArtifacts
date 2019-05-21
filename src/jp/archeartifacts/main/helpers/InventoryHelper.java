package jp.archeartifacts.main.helpers;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InventoryHelper {
	
	/***
	 * Used to check if a player contains a certain magical item
	 * @param player the player to check
	 * @param material the magical item material to check for
	 * @return true/false if the item was found
	 */
	public static boolean containsMagicalItem(Player player, Material material) {
		Inventory playerInventory = player.getInventory();
		for (ItemStack item : playerInventory.all(material).values()) {
			if ( (ItemStackHelper.isMagicalItem(item)) && (ItemStackHelper.isItemActivated(item)) ) { return true; } 
		}
		return false;
	}
	
	/***
	 * Checks if the entity is wearing a magical item
	 * @param entity the entity to check for
	 * @param material the magical item material to check for
	 * @return true/false if the item was found
	 */
	public static boolean wearingMagicalItem(LivingEntity entity, Material material) {
		ItemStack entityHelmet = entity.getEquipment().getHelmet();
		if (entityHelmet != null) {
			if ((entityHelmet.getType().equals(material)) && (ItemStackHelper.isMagicalItem(entityHelmet))) { return true; }
		}
		return false;
	}

}
