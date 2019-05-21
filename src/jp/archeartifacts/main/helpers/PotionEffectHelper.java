package jp.archeartifacts.main.helpers;

import org.bukkit.craftbukkit.v1_14_R1.entity.CraftEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import net.minecraft.server.v1_14_R1.Entity;
import net.minecraft.server.v1_14_R1.EntityLiving;
import net.minecraft.server.v1_14_R1.NBTTagByte;
import net.minecraft.server.v1_14_R1.NBTTagCompound;
import net.minecraft.server.v1_14_R1.NBTTagInt;
import net.minecraft.server.v1_14_R1.NBTTagList;

public class PotionEffectHelper {

	/**
	 * Compares a potion effect to the potion effects on an entity. Calculates
	 * the potion effect that should be applied to the entity based on
	 * comparison.
	 * <p>
	 * <b>Criteria:</b><br>
	 * - If entity already has potion effect, the resulting potion effect will use the higher amplifier.<br>
	 * - If the new potion effect amplifier is higher than existing potion effect, the existing
	 * duration will be reduced and added to the resulting effect.<br>
	 * - If the amplifiers are the same, the resulting potion effect will use the higher duration.<br>
	 * - If entity does not have potion effect, no changes will be made to the resulting potion effect.<br>
	 * </p>
	 * @param newPotionEffect The potion effect you want to compare to the effects on the entity
	 * @param entity The entity whose potion effects you want to compare to
	 * @return The potion effect that results from the comparison
	 */
	public static PotionEffect comparePotionEffect(PotionEffect newPotionEffect, LivingEntity entity) {
		
		if (entity.hasPotionEffect(newPotionEffect.getType())) {

			//Local variables, resultingEffect is not needed, but there to help readability.
			PotionEffect resultingEffect;
			PotionEffect currentEffect = entity.getPotionEffect(newPotionEffect.getType());
			int amplifier, duration;
			int amplifierDifference = (newPotionEffect.getAmplifier() - currentEffect.getAmplifier());
			
			//If the new effect is a higher amplifier, decrease the existing duration and add it to the new effect's duration.
			if (amplifierDifference > 0) {
				duration = (currentEffect.getDuration() / (3 * amplifierDifference)) + newPotionEffect.getDuration();
			}
			//If the effects are the same amplifier, use the higher duration.
			else if (amplifierDifference == 0) {
				duration = Math.max(newPotionEffect.getDuration(), currentEffect.getDuration());
			}
			//If the new effect is a lower amplifier, use the already existing duration.
			else {
				duration = currentEffect.getDuration();
			}
			
			amplifier = Math.max(newPotionEffect.getAmplifier(), currentEffect.getAmplifier());
			resultingEffect = new PotionEffect(newPotionEffect.getType(), duration, amplifier);
			return resultingEffect;
		}
		
		else {
			return newPotionEffect;
		}
	}
	
	/**
	 * Forces a potion effect on an entity. EX. Regeneration on undead mobs.
	 * @param newPotionEffect the potion effect to give to the entity
	 * @param entity the entity to gain the potion effect
	 */
	@SuppressWarnings("deprecation")
	public static void forcePotionEffect(PotionEffect newPotionEffect, LivingEntity entity) {
		
		if (entity instanceof Player) {
			entity.addPotionEffect(newPotionEffect, true);
		}
		else {
			//Get NMS entity
			Entity nmsEntity = ((CraftEntity)entity).getHandle();
			
			//Create a tag compound and copy existing nbt data to new tag
			NBTTagCompound entityTag = new NBTTagCompound();
			nmsEntity.c(entityTag);
			
			//Create a new tag list that will contain nbt data
			NBTTagList modifiers = new NBTTagList();
			
			//Applies the regeneration potion effect
			NBTTagCompound Potion = new NBTTagCompound();
			Potion.set("Id", new NBTTagByte((byte) newPotionEffect.getType().getId()));
			Potion.set("Amplifier", new NBTTagByte((byte) newPotionEffect.getAmplifier()));
			Potion.set("Duration", new NBTTagInt(newPotionEffect.getDuration()));
			modifiers.add(Potion);
			
			//Set the new tag compound to the existing entity.
			entityTag.set("ActiveEffects", modifiers);
			EntityLiving entityLiving = (EntityLiving) nmsEntity;
			entityLiving.a(entityTag);
		}
	}
}
