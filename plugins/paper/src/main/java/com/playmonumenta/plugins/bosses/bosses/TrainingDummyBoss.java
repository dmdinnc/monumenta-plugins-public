package com.playmonumenta.plugins.bosses.bosses;

import java.util.Arrays;

import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.Plugin;

import com.playmonumenta.plugins.bosses.SpellManager;
import com.playmonumenta.plugins.bosses.spells.SpellRunAction;

public class TrainingDummyBoss extends BossAbilityGroup {
	public static final String identityTag = "boss_training_dummy";
	public static final int detectionRange = 25;

	public static BossAbilityGroup deserialize(Plugin plugin, LivingEntity boss) throws Exception {
		return new TrainingDummyBoss(plugin, boss);
	}

	public TrainingDummyBoss(Plugin plugin, LivingEntity boss) throws Exception {
		super(plugin, identityTag, boss);
		SpellManager activeSpells = new SpellManager(Arrays.asList(
			new SpellRunAction(() -> {
				boss.setHealth(boss.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
			}, 60 * 20)
		));

		super.constructBoss(activeSpells, null, detectionRange, null);
		boss.setRemoveWhenFarAway(false);
	}

	@Override
	public void bossDamagedByEntity(EntityDamageByEntityEvent event) {
		Entity damager = event.getDamager();
		double damage = event.getFinalDamage();

		// Damage smaller than this is only meant to tag the mob as damaged by a player
		if (damage < 0.01) {
			return;
		}

		if (damager instanceof Player) {
			Player player = (Player) damager;
			player.sendMessage(ChatColor.GOLD + "Damage: " + ChatColor.RED + damage);
		}

		if (damager instanceof Projectile) {
			Projectile projectile = (Projectile) damager;

			if (projectile.getShooter() instanceof Player) {
				Player player = (Player) projectile.getShooter();
				player.sendMessage(ChatColor.GOLD + "Damage: " + ChatColor.RED + damage);
			}
		}
	}
}
