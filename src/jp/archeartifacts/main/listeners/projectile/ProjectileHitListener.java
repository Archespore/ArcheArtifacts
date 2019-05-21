package jp.archeartifacts.main.listeners.projectile;

import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

public class ProjectileHitListener implements Listener {

	@EventHandler
	public void projectileHitEvent(ProjectileHitEvent event) {
		
		Projectile eventProjectile = event.getEntity();
		Entity eventHitEntity = event.getHitEntity();
		ProjectileSource projectileOwner = eventProjectile.getShooter();
		
		//Firework Arrows, creates fireworks when the projectile hits the ground or entity.
		if (eventProjectile.hasMetadata("FIREWORK")) {
			Firework firework = eventProjectile.getWorld().spawn(eventProjectile.getLocation(), Firework.class);
			FireworkMeta fireworkMeta = firework.getFireworkMeta();
			fireworkMeta.setPower(ThreadLocalRandom.current().nextInt(2));
			fireworkMeta.addEffect(FireworkEffect.builder()
					.flicker(false)
					.trail(true)
					.with(Type.BALL)
					.withColor(Color.fromRGB(ThreadLocalRandom.current().nextInt(256), ThreadLocalRandom.current().nextInt(256), ThreadLocalRandom.current().nextInt(256)))
					.withColor(Color.fromRGB(ThreadLocalRandom.current().nextInt(256), ThreadLocalRandom.current().nextInt(256), ThreadLocalRandom.current().nextInt(256)))
					.withFade(Color.fromRGB(ThreadLocalRandom.current().nextInt(256), ThreadLocalRandom.current().nextInt(256), ThreadLocalRandom.current().nextInt(256)))
					.build());
			firework.setFireworkMeta(fireworkMeta);
		}
		
		//Grapple Arrows, pulls the player or entity hit
		if (eventProjectile.hasMetadata("GRAPPLE")) {
			if ( (projectileOwner instanceof LivingEntity) && (eventHitEntity instanceof LivingEntity) ) {
				LivingEntity eventShooter = (LivingEntity)projectileOwner;
				LivingEntity eventHitLiving = (LivingEntity)eventHitEntity;
				Location shooterLocation = eventShooter.getEyeLocation().clone().add(0, .25, 0);
				if ( (eventHitEntity != null) && (eventHitEntity != eventShooter) ) {
					Vector pullDirection = shooterLocation.toVector().subtract(eventHitLiving.getEyeLocation().toVector()).normalize().multiply(1.75);
					eventHitEntity.setVelocity(eventHitEntity.getVelocity().add(pullDirection));
				}
			}
		}
	}
}
