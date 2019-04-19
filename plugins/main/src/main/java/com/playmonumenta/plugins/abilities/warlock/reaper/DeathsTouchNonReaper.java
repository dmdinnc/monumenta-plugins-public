package com.playmonumenta.plugins.abilities.warlock.reaper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.abilities.Ability;
import com.playmonumenta.plugins.potion.PotionManager.PotionID;

public class DeathsTouchNonReaper extends Ability {

	/*
	 * Allow other players to reap the benefits of a marked enemy.
	 */

	public DeathsTouchNonReaper(Plugin plugin, World world, Random random, Player player) {
		super(plugin, world, random, player);
	}

	@Override
	public boolean canUse(Player player) {
		return true;
	}

	private static List<PotionEffectType> getOppositeEffects(LivingEntity e) {
		List<PotionEffectType> types = new ArrayList<PotionEffectType>();
		for (PotionEffect effect : e.getActivePotionEffects()) {
			if (effect.getType().equals(PotionEffectType.WEAKNESS)) {
				types.add(PotionEffectType.INCREASE_DAMAGE);
			} else if (effect.getType().equals(PotionEffectType.SLOW)) {
				types.add(PotionEffectType.SPEED);
			} else if (effect.getType().equals(PotionEffectType.WITHER) || effect.getType().equals(PotionEffectType.POISON)) {
				types.add(PotionEffectType.REGENERATION);
			} else if (effect.getType().equals(PotionEffectType.SLOW_DIGGING)) {
				types.add(PotionEffectType.FAST_DIGGING);
			} else if (effect.getType().equals(PotionEffectType.BLINDNESS)) {
				types.add(PotionEffectType.NIGHT_VISION);
			}
		}
		if (e.getFireTicks() > 0) {
			types.add(PotionEffectType.FIRE_RESISTANCE);
		}
		return types;
	}

	@Override
	public void EntityDeathEvent(EntityDeathEvent event, boolean shouldGenDrops) {
		if (event.getEntity().hasMetadata("DeathsTouchBuffDuration")) {
			List<PotionEffectType> effects = getOppositeEffects(event.getEntity());
			int duration = event.getEntity().getMetadata("DeathsTouchBuffDuration").get(0).asInt();
			for (PotionEffectType effect : effects) {
				mPlugin.mPotionManager.addPotion(mPlayer, PotionID.ABILITY_SELF, new PotionEffect(effect, duration, 0, true, true));
			}
		}
	}

}