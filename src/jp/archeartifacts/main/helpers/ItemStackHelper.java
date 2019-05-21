package jp.archeartifacts.main.helpers;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import io.netty.util.internal.ThreadLocalRandom;
import jp.archeartifacts.main.ArcheArtifacts;

public class ItemStackHelper {
	
	/**
	 * Damages an ItemStack by a specified amount of durability.
	 * @param item The item to remove durability from.
	 * @param durability the amount of durability to remove.
	 * @return returns a boolean on whether or not the item was destroyed after taking damage.
	 */
	public static boolean damageItem(ItemStack item, int durability) {
		
		ItemMeta itemMeta = item.getItemMeta();
		if (itemMeta instanceof Damageable) {
			int unbreakingLevel = itemMeta.getEnchantLevel(Enchantment.DURABILITY);
			Damageable itemDamage = (Damageable)itemMeta;
			double unbreakingChance = (EnchantmentTarget.ARMOR.includes(item) ? (60 + (40 / (unbreakingLevel + 1))) : (100 / (unbreakingLevel + 1)) );
			if (ThreadLocalRandom.current().nextDouble(100.0) < unbreakingChance) { itemDamage.setDamage(durability + itemDamage.getDamage()); }
			item.setItemMeta(itemMeta);
			if (itemDamage.getDamage() > item.getType().getMaxDurability()) {
				item.setAmount(0);
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Determines if the specified item is a magical item
	 * @param item the item to check
	 * @return true/false if the item is magical
	 */
	public static boolean isMagicalItem(ItemStack item) {
		PersistentDataContainer dataContainer = item.getItemMeta().getPersistentDataContainer();
		Byte magicalItem = dataContainer.get(new NamespacedKey(ArcheArtifacts.getPlugin(), "MAGICAL_ITEM"), PersistentDataType.BYTE);
		return (magicalItem == null ? false : true);
	}
	
	/**
	 * Checks if the specified item is activated
	 * @param item the item to check
	 * @return true/false depending on if the item is activated
	 */
	public static boolean isItemActivated(ItemStack item) {
		if (itemActivatable(item)) {
			PersistentDataContainer dataContainer = item.getItemMeta().getPersistentDataContainer();
			byte activationState = dataContainer.get(new NamespacedKey(ArcheArtifacts.getPlugin(), "MAGICAL_ACTIVATED"), PersistentDataType.BYTE);
			return (activationState == 0 ? false : true);
		}
		//If the items is not an activatable item, we return true since it is technically always active
		return true;
	}
	
	/**
	 * Adds the activation data to the item if it doesn't exist
	 * @param item the item to add the data to
	 */
	public static void addActivation(ItemStack item) {
		ItemMeta itemMeta = item.getItemMeta();
		PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();
		dataContainer.set(new NamespacedKey(ArcheArtifacts.getPlugin(), "MAGICAL_ACTIVATED"), PersistentDataType.BYTE, (byte)1);
		item.setItemMeta(itemMeta);
	}

	/**
	 * Checks to see if the specified item is activatable or not
	 * @param item the item to check
	 * @return true/false depending on if the item is activatable
	 */
	public static boolean itemActivatable(ItemStack item) {
		return item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(ArcheArtifacts.getPlugin(), "MAGICAL_ACTIVATED"), PersistentDataType.BYTE);
	}
	
	/**
	 * Switches the activation state of the specified item if it exists
	 * @param item the item to switch activation state
	 */
	public static void switchActivation(ItemStack item) {
		if (itemActivatable(item)) {
			ItemMeta itemMeta = item.getItemMeta();
			PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();
			byte activationState = dataContainer.get(new NamespacedKey(ArcheArtifacts.getPlugin(), "MAGICAL_ACTIVATED"), PersistentDataType.BYTE);
			
			//Switch the activationState the to opposite and update the data container
			activationState = (byte)(activationState == 0 ? 1 : 0);
			dataContainer.set(new NamespacedKey(ArcheArtifacts.getPlugin(), "MAGICAL_ACTIVATED"), PersistentDataType.BYTE, activationState);
			
			//Update Item Meta
			List<String> itemLore = itemMeta.getLore();
			itemLore = (itemLore == null ? new ArrayList<>() : itemLore);
			if (itemLore.size() == 0) { itemLore.add(""); }
			ChatColor activationColor = (activationState == 0 ? ChatColor.DARK_GRAY : ChatColor.GOLD);
			String activationString = (activationState == 0 ? ChatColor.RED + "Disabled" : ChatColor.BLUE + "Enabled");
			itemLore.set(0, activationString);
			itemMeta.setLore(itemLore);
			itemMeta.setDisplayName(activationColor + ChatColor.stripColor(itemMeta.getDisplayName()));
			item.setItemMeta(itemMeta);
		}
	}
}
