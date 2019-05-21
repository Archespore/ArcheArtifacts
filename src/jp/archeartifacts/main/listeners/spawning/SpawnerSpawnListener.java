package jp.archeartifacts.main.listeners.spawning;

import org.bukkit.NamespacedKey;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import jp.archeartifacts.main.ArcheArtifacts;

public class SpawnerSpawnListener implements Listener {

	@EventHandler
	public void spawnerSpawnEvent(SpawnerSpawnEvent event) {
		//Get spawner information
		CreatureSpawner spawner = event.getSpawner();
		PersistentDataContainer dataContainer = spawner.getPersistentDataContainer();
		Byte reliquarySpawner = dataContainer.get(new NamespacedKey(ArcheArtifacts.getPlugin(), "RELIQUARY_SPAWNER"), PersistentDataType.BYTE);
		

		Entity spawnedEntity = event.getEntity();
		if (spawnedEntity instanceof LivingEntity) {
			LivingEntity livingEntity = (LivingEntity)spawnedEntity;
			livingEntity.setMetadata("SPAWNER_MOB", new FixedMetadataValue(ArcheArtifacts.getPlugin(), true));
			//If this is a reliquary spawner, give the entity resistance
			if (reliquarySpawner != null) { livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 0), true); }
		}
	}
}
