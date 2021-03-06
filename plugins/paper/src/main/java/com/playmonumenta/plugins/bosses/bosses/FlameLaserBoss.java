package com.playmonumenta.plugins.bosses.bosses;

import java.util.Arrays;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.playmonumenta.plugins.bosses.SpellManager;
import com.playmonumenta.plugins.bosses.spells.SpellBaseLaser;
import com.playmonumenta.plugins.utils.BossUtils;

public class FlameLaserBoss extends BossAbilityGroup {
	public static final String identityTag = "boss_flamelaser";
	public static final int detectionRange = 30;

	public static BossAbilityGroup deserialize(Plugin plugin, LivingEntity boss) throws Exception {
		return new FlameLaserBoss(plugin, boss);
	}

	public FlameLaserBoss(Plugin plugin, LivingEntity boss) {
		super(plugin, identityTag, boss);

		SpellManager activeSpells = new SpellManager(Arrays.asList(
			new SpellBaseLaser(plugin, boss, detectionRange, 100, false, false, 160,
					// Tick action per player
					(Player player, int ticks, boolean blocked) -> {
						player.playSound(player.getLocation(), Sound.UI_TOAST_IN, 0.5f, 0.5f + (ticks / 80f) * 1.5f);
						boss.getLocation().getWorld().playSound(boss.getLocation(), Sound.UI_TOAST_IN, 1f, 0.5f + (ticks / 80f) * 1.5f);
						if (ticks == 0) {
							boss.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 110, 4));
						}
					},
					// Particles generated by the laser
					(Location loc) -> {
						loc.getWorld().spawnParticle(Particle.CLOUD, loc, 1, 0.02, 0.02, 0.02, 0);
						loc.getWorld().spawnParticle(Particle.FLAME, loc, 1, 0.04, 0.04, 0.04, 1);
					},
					// Damage generated at the end of the attack
					(Player player, Location loc, boolean blocked) -> {
						loc.getWorld().playSound(loc, Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 1f, 1.5f);
						loc.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, loc, 300, 0.8, 0.8, 0.8, 0);
						if (!blocked) {
							BossUtils.bossDamage(boss, player, 19);
							// Shields don't stop fire!
							player.setFireTicks(4 * 20);
						}
					})
		));

		super.constructBoss(activeSpells, null, detectionRange, null);
	}
}
