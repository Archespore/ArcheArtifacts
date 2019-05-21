package jp.archeartifacts.main.listeners.entity;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.metadata.FixedMetadataValue;

import jp.archeartifacts.main.ArcheArtifacts;
import jp.archeartifacts.main.helpers.InventoryHelper;

public class EntityShootBowListeners implements Listener {

	@EventHandler
	public void entityShootBowEvent(EntityShootBowEvent event) {
		//Variable gathering
		LivingEntity eventShooter = event.getEntity();
		Entity eventProjectile = event.getProjectile();
		
		if ( (eventShooter instanceof Player) && (eventProjectile instanceof Projectile) ) {
			Player playerShooter = (Player)eventShooter;
			Projectile shooterProjectile = (Projectile) eventProjectile;
			
			//Arrow Fireworks, causes fireworks when an arrow lands.
			if (InventoryHelper.containsMagicalItem(playerShooter, Material.PRISMARINE_CRYSTALS)) {
				shooterProjectile.setMetadata("FIREWORK", new FixedMetadataValue(ArcheArtifacts.getPlugin(), true));
			}
			
			//Grapple Hook, arrows pull the player, or hit mobs to the player
			if (InventoryHelper.containsMagicalItem(playerShooter, Material.LEAD)) {
				shooterProjectile.setMetadata("GRAPPLE", new FixedMetadataValue(ArcheArtifacts.getPlugin(), true));
				shooterProjectile.setVelocity(shooterProjectile.getVelocity().multiply(.8));
			}
		}
	}
}
