package com.playmonumenta.plugins.abilities.scout.ranger;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.abilities.Ability;
import com.playmonumenta.plugins.abilities.AbilityTrigger;
import com.playmonumenta.plugins.classes.Spells;
import com.playmonumenta.plugins.classes.magic.MagicType;
import com.playmonumenta.plugins.utils.EntityUtils;
import com.playmonumenta.plugins.utils.InventoryUtils;
import com.playmonumenta.plugins.utils.MovementUtils;
import com.playmonumenta.plugins.utils.ZoneUtils;
import com.playmonumenta.plugins.utils.ZoneUtils.ZoneProperty;
import com.playmonumenta.scriptedquests.utils.MessagingUtils;

public class TacticalManeuver extends Ability {

	private static final int TACTICAL_MANEUVER_1_MAX_CHARGES = 2;
	private static final int TACTICAL_MANEUVER_2_MAX_CHARGES = 3;
	private static final int TACTICAL_MANEUVER_1_COOLDOWN = 20 * 12;
	private static final int TACTICAL_MANEUVER_2_COOLDOWN = 20 * 10;
	private static final int TACTICAL_MANEUVER_RADIUS = 3;
	private static final int TACTICAL_DASH_DAMAGE = 16;
	private static final int TACTICAL_DASH_STUN_DURATION = 20 * 1;
	private static final int TACTICAL_LEAP_DAMAGE = 8;
	private static final float TACTICAL_LEAP_KNOCKBACK_SPEED = 0.5f;

	private final int mMaxCharges;

	private int mCharges;
	private boolean mWasOnCooldown = false;

	public TacticalManeuver(Plugin plugin, World world, Player player) {
		super(plugin, world, player, "Tactical Maneuver");
		mInfo.mLinkedSpell = Spells.TACTICAL_MANEUVER;
		mInfo.mScoreboardId = "TacticalManeuver";
		mInfo.mShorthandName = "TM";
		mInfo.mDescriptions.add("Sprint right click to dash forward, dealing the first enemy hit 16 damage, and stunning it and all enemies in a 3 block radius for 1 second. Shift right click to leap backwards, dealing enemies in a 3 block radius 8 damage and knocking them away. Only triggers with non-trident melee weapons. Cooldown: 12 seconds. Charges: 2.");
		mInfo.mDescriptions.add("Cooldown decreased to 10 seconds and Charges increased to 3.");
		mInfo.mCooldown = getAbilityScore() == 1 ? TACTICAL_MANEUVER_1_COOLDOWN : TACTICAL_MANEUVER_2_COOLDOWN;
		mInfo.mTrigger = AbilityTrigger.RIGHT_CLICK;
		mInfo.mIgnoreCooldown = true;

		mMaxCharges = getAbilityScore() == 1 ? TACTICAL_MANEUVER_1_MAX_CHARGES : TACTICAL_MANEUVER_2_MAX_CHARGES;
		mCharges = mMaxCharges;
	}

	@Override
	public void cast(Action action) {
		if (mCharges == 0 || ZoneUtils.hasZoneProperty(mPlayer, ZoneProperty.NO_MOBILITY_ABILITIES)) {
			return;
		}

		ItemStack inMainHand = mPlayer.getInventory().getItemInMainHand();
		ItemStack inOffHand = mPlayer.getInventory().getItemInOffHand();
		if (InventoryUtils.isBowItem(inMainHand) || InventoryUtils.isBowItem(inOffHand) || InventoryUtils.isPotionItem(inMainHand) || inMainHand.getType().isBlock()
				|| inMainHand.getType().isEdible() || inMainHand.getType() == Material.TRIDENT || inMainHand.getType() == Material.COMPASS) {
			return;
		}

		if (mPlayer.isSprinting()) {
			mCharges--;
			MessagingUtils.sendActionBarMessage(mPlayer, "Tactical Maneuver Charges: " + mCharges);

			mWorld.spawnParticle(Particle.SMOKE_NORMAL, mPlayer.getLocation(), 63, 0.25, 0.1, 0.25, 0.2);
			mWorld.spawnParticle(Particle.CLOUD, mPlayer.getLocation(), 20, 0.25, 0.1, 0.25, 0.125);
			mWorld.playSound(mPlayer.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1, 2);
			mWorld.playSound(mPlayer.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1, 1.7f);
			Vector dir = mPlayer.getLocation().getDirection();
			mPlayer.setVelocity(dir.setY(dir.getY() * 0.5 + 0.4));

			new BukkitRunnable() {
				@Override
				public void run() {
					if (mPlayer.isOnGround() || mPlayer.getLocation().getBlock().getType() == Material.WATER
					    || mPlayer.getLocation().getBlock().getType() == Material.LAVA || mPlayer.isDead() || !mPlayer.isOnline()) {
						this.cancel();
						return;
					}

					mWorld.spawnParticle(Particle.SMOKE_NORMAL, mPlayer.getLocation(), 5, 0.25, 0.1, 0.25, 0.1);

					for (LivingEntity le : EntityUtils.getNearbyMobs(mPlayer.getLocation().clone().add(mPlayer.getVelocity().normalize()), 2, mPlayer)) {
						if (!le.isDead()) {
							EntityUtils.damageEntity(mPlugin, le, TACTICAL_DASH_DAMAGE, mPlayer, null, true, mInfo.mLinkedSpell);
							for (LivingEntity e : EntityUtils.getNearbyMobs(le.getLocation(), TACTICAL_MANEUVER_RADIUS)) {
								EntityUtils.applyStun(mPlugin, TACTICAL_DASH_STUN_DURATION, e);
							}

							mWorld.spawnParticle(Particle.SMOKE_NORMAL, mPlayer.getLocation(), 63, 0.25, 0.1, 0.25, 0.2);
							mWorld.spawnParticle(Particle.CLOUD, mPlayer.getLocation(), 20, 0.25, 0.1, 0.25, 0.125);
							mWorld.playSound(mPlayer.getLocation(), Sound.ITEM_SHIELD_BREAK, 2.0f, 0.5f);

							this.cancel();
							break;
						}
					}
				}
			}.runTaskTimer(mPlugin, 5, 1);
		} else if (mPlayer.isSneaking()) {
			mCharges--;
			MessagingUtils.sendActionBarMessage(mPlayer, "Tactical Maneuver Charges: " + mCharges);

			for (LivingEntity le : EntityUtils.getNearbyMobs(mPlayer.getLocation(), TACTICAL_MANEUVER_RADIUS, mPlayer)) {
				EntityUtils.damageEntity(mPlugin, le, TACTICAL_LEAP_DAMAGE, mPlayer, MagicType.PHYSICAL, true, mInfo.mLinkedSpell);
				MovementUtils.knockAway(mPlayer, le, TACTICAL_LEAP_KNOCKBACK_SPEED);
			}

			mWorld.playSound(mPlayer.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1, 2);
			mWorld.playSound(mPlayer.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_SHOOT, 1, 1.2f);
			mWorld.spawnParticle(Particle.CLOUD, mPlayer.getLocation(), 15, 0.1f, 0, 0.1f, 0.125f);
			mWorld.spawnParticle(Particle.EXPLOSION_NORMAL, mPlayer.getLocation(), 10, 0.1f, 0, 0.1f, 0.15f);
			mWorld.spawnParticle(Particle.SMOKE_NORMAL, mPlayer.getLocation(), 25, 0.1f, 0, 0.1f, 0.15f);
			mPlayer.setVelocity(mPlayer.getLocation().getDirection().setY(0).normalize().multiply(-1.65).setY(0.65));
		}
	}

	@Override
	public void periodicTrigger(boolean fourHertz, boolean twoHertz, boolean oneSecond, int ticks) {
		// If the skill is somehow on cooldown when charges are full, take it off cooldown
		if (mCharges == mMaxCharges
				&& mPlugin.mTimers.isAbilityOnCooldown(mPlayer.getUniqueId(), mInfo.mLinkedSpell)) {
			mPlugin.mTimers.removeCooldown(mPlayer.getUniqueId(), mInfo.mLinkedSpell);
		}

		// Increment charges if last check was on cooldown, and now is off cooldown.
		if (mCharges < mMaxCharges && mWasOnCooldown
				&& !mPlugin.mTimers.isAbilityOnCooldown(mPlayer.getUniqueId(), mInfo.mLinkedSpell)) {
			mCharges++;
			MessagingUtils.sendActionBarMessage(mPlayer, "Tactical Maneuver Charges: " + mCharges);
		}

		// Put on cooldown if charges can still be gained
		if (mCharges < mMaxCharges
				&& !mPlugin.mTimers.isAbilityOnCooldown(mPlayer.getUniqueId(), mInfo.mLinkedSpell)) {
			putOnCooldown();
		}

		mWasOnCooldown = mPlugin.mTimers.isAbilityOnCooldown(mPlayer.getUniqueId(), mInfo.mLinkedSpell);
	}
}