package jp.archeartifacts.main.helpers;

import org.bukkit.EntityEffect;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;

public class LivingEntityHelper {
	
	/**
	 * Determines if the specified entity is damaged or not, as in, not at full health
	 * @param entity the entity's health we are checking
	 * @return true/false if the entity is damaged.
	 */
	public static boolean isDamaged(LivingEntity entity) {
		return (entity.getHealth() < entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
	}

	/**
	 * Adds health to an entity, takes max health into account.
	 * @param livingEntity entity to heal.
	 * @param amount amount to heal.
	 */
	public static void addHealth(LivingEntity entity, double amount) {
		entity.setHealth(Math.max(0, Math.min(entity.getHealth() + amount, entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue())));
	}
	
	/**
	 * Removes health from an entity, ignores armor and no damage ticks and takes into account max and min health values.
	 * @param livingEntity entity to damage.
	 * @param amount the amount of damage.
	 */
	public static void removeHealth(LivingEntity livingEntity, double amount){
		//Make sure entity isn't immortal
		if ( (livingEntity.getHealth() - amount <= 0) && (livingEntity.getScoreboardTags().contains("IMMORTAL")) ) {
			livingEntity.setHealth(.01);
		}
		else { livingEntity.setHealth(Math.max(0, Math.min(livingEntity.getHealth() - amount, livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() - amount))); }
		livingEntity.playEffect(EntityEffect.HURT);
	}
}
