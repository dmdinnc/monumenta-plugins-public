package com.playmonumenta.plugins.abilities.mage;

import java.util.Random;

import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Stray;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.abilities.Ability;
import com.playmonumenta.plugins.classes.Spells;
import com.playmonumenta.plugins.classes.magic.MagicType;
import com.playmonumenta.plugins.utils.EntityUtils;
import com.playmonumenta.plugins.utils.PotionUtils;

public class ElementalArrows extends Ability {

	private static final int ELEMENTAL_ARROWS_1_DAMAGE = 1;
	private static final int ELEMENTAL_ARROWS_2_DAMAGE = 3;
	private static final int ELEMENTAL_ARROWS_BONUS_DAMAGE = 8;
	private static final int ELEMENTAL_ARROWS_DURATION = 20 * 6;
	private static final double ELEMENTAL_ARROWS_RADIUS = 3.0;

	public ElementalArrows(Plugin plugin, World world, Random random, Player player) {
		super(plugin, world, random, player);
		mInfo.linkedSpell = Spells.ELEMENTAL_ARROWS;
		mInfo.scoreboardId = "Elemental";
	}

	@Override
	public boolean LivingEntityShotByPlayerEvent(Arrow arrow, LivingEntity damagee, EntityDamageByEntityEvent event) {
		int elementalArrows = getAbilityScore();
		int damage = elementalArrows == 1 ? ELEMENTAL_ARROWS_1_DAMAGE : ELEMENTAL_ARROWS_2_DAMAGE;
		if (arrow.hasMetadata("ElementalArrowsFireArrow")) {
			if (elementalArrows > 1) {
				for (LivingEntity mob : EntityUtils.getNearbyMobs(damagee.getLocation(), ELEMENTAL_ARROWS_RADIUS, damagee)) {
					EntityUtils.damageEntity(mPlugin, mob, damage, mPlayer, MagicType.FIRE, true, mInfo.linkedSpell, false, true);
					mob.setFireTicks(ELEMENTAL_ARROWS_DURATION);
				}
			}
			if (damagee instanceof Stray) {
				damage += ELEMENTAL_ARROWS_BONUS_DAMAGE;
			}

			EntityUtils.damageEntity(mPlugin, damagee, damage, mPlayer, MagicType.FIRE, true, mInfo.linkedSpell, false, true);
			EntityUtils.applyFire(mPlugin, ELEMENTAL_ARROWS_DURATION, damagee);
		} else if (arrow.hasMetadata("ElementalArrowsIceArrow")) {
			if (elementalArrows > 1) {
				for (LivingEntity mob : EntityUtils.getNearbyMobs(damagee.getLocation(), ELEMENTAL_ARROWS_RADIUS, damagee)) {
					EntityUtils.damageEntity(mPlugin, mob, damage, mPlayer, MagicType.ICE, true, mInfo.linkedSpell, false, true);
					PotionUtils.applyPotion(mPlayer, mob, new PotionEffect(PotionEffectType.SLOW, ELEMENTAL_ARROWS_DURATION, 1));
				}
			}
			if (damagee instanceof Blaze) {
				damage += ELEMENTAL_ARROWS_BONUS_DAMAGE;
			}

			EntityUtils.damageEntity(mPlugin, damagee, damage, mPlayer, MagicType.ICE, true, mInfo.linkedSpell, false, true);
			PotionUtils.applyPotion(mPlayer, damagee, new PotionEffect(PotionEffectType.SLOW, ELEMENTAL_ARROWS_DURATION, 1));
		}

		return true;
	}

	@Override
	public boolean PlayerShotArrowEvent(Arrow arrow) {
		if (mPlayer.isSneaking()) {
			arrow.setMetadata("ElementalArrowsIceArrow", new FixedMetadataValue(mPlugin, 0));
			arrow.setFireTicks(0);
			mPlugin.mProjectileEffectTimers.addEntity(arrow, Particle.SNOW_SHOVEL);
		} else {
			arrow.setMetadata("ElementalArrowsFireArrow", new FixedMetadataValue(mPlugin, 0));
			arrow.setFireTicks(ELEMENTAL_ARROWS_DURATION);
			mPlugin.mProjectileEffectTimers.addEntity(arrow, Particle.FLAME);
		}

		return true;
	}
}