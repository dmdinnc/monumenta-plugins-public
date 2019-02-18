package com.playmonumenta.plugins.abilities.mage.arcanist;

import java.util.Random;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.abilities.Ability;
import com.playmonumenta.plugins.abilities.AbilityTrigger;
import com.playmonumenta.plugins.classes.Spells;
import com.playmonumenta.plugins.utils.EntityUtils;
import com.playmonumenta.plugins.utils.InventoryUtils;
import com.playmonumenta.plugins.utils.MovementUtils;
import com.playmonumenta.plugins.utils.VectorUtils;

/*
 * Flash Sword: Sprint left clicking with a wand causes a wave
 * of Arcane blades to cut down all the foes in your path. Each
 * enemy within a 5 block cone takes 3 damage 3 times in rapid
 * succession, being knocked back with the last swipe. At level
 * 2 this abilities damage increases to 5 damage 3 times.
 * (CD: 8 seconds)
 */
public class FlashSword extends Ability {

	private static final int FSWORD_1_DAMAGE = 3;
	private static final int FSWORD_2_DAMAGE = 5;
	private static final int FSWORD_SWINGS = 3;
	private static final int FSWORD_RADIUS = 5;
	private static final int FSWORD_COOLDOWN = 20 * 8;
	private static final float FSWORD_KNOCKBACK_SPEED = 0.4f;
	private static final double FSWORD_DOT_ANGLE = 0.33;
	private static final Particle.DustOptions FSWORD_COLOR1 = new Particle.DustOptions(Color.fromRGB(106, 203, 255), 1.0f);
	private static final Particle.DustOptions FSWORD_COLOR2 = new Particle.DustOptions(Color.fromRGB(168, 226, 255), 1.0f);

	public FlashSword(Plugin plugin, World world, Random random, Player player) {
		super(plugin, world, random, player);
		mInfo.scoreboardId = "FlashSword";
		mInfo.linkedSpell = Spells.FSWORD;
		mInfo.cooldown = FSWORD_COOLDOWN;
		mInfo.trigger = AbilityTrigger.LEFT_CLICK;
	}

	@Override
	public boolean cast() {
		int flashSword = getAbilityScore();
		Player player = mPlayer;
		putOnCooldown();
		new BukkitRunnable() {
			int t = 0;
			float pitch = 1.2f;
			int sw = 0;

			@Override
			public void run() {
				t++;
				sw++;
				Vector playerDir = player.getEyeLocation().getDirection().setY(0).normalize();
				for (LivingEntity mob : EntityUtils.getNearbyMobs(player.getLocation(), FSWORD_RADIUS)) {
					Vector toMobVector = mob.getLocation().toVector().subtract(player.getLocation().toVector()).setY(0)
					                     .normalize();
					if (playerDir.dot(toMobVector) > FSWORD_DOT_ANGLE) {
						int damageMult = (flashSword == 1) ? FSWORD_1_DAMAGE : FSWORD_2_DAMAGE;
						mob.setNoDamageTicks(0);
						EntityUtils.damageEntity(mPlugin, mob, damageMult, player);
						if (t >= FSWORD_SWINGS) {
							MovementUtils.KnockAway(player, mob, FSWORD_KNOCKBACK_SPEED);
						}
					}
				}

				if (t >= FSWORD_SWINGS) {
					pitch = 1.45f;
				}
				player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 0.8f);
				player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_SHOOT, 1.0f, pitch);
				Location loc = player.getLocation();
				new BukkitRunnable() {
					final int i = sw;
					double roll;
					double d = 45;
					boolean init = false;

					@Override
					public void run() {
						if (!init) {
							if (i % 2 == 0) {
								roll = -8;
								d = 45;
							} else {
								roll = 8;
								d = 135;
							}
							init = true;
						}
						if (i % 2 == 0) {
							Vector vec;
							for (double r = 1; r < 5; r += 0.5) {
								for (double degree = d; degree < d + 30; degree += 5) {
									double radian1 = Math.toRadians(degree);
									vec = new Vector(Math.cos(radian1) * r, 0, Math.sin(radian1) * r);
									vec = VectorUtils.rotateZAxis(vec, roll);
									vec = VectorUtils.rotateXAxis(vec, -loc.getPitch());
									vec = VectorUtils.rotateYAxis(vec, loc.getYaw());

									Location l = loc.clone().add(0, 1.25, 0).add(vec);
									mWorld.spawnParticle(Particle.REDSTONE, l, 1, 0.1, 0.1, 0.1, FSWORD_COLOR1);
									mWorld.spawnParticle(Particle.REDSTONE, l, 1, 0.1, 0.1, 0.1, FSWORD_COLOR2);
								}
							}

							d += 30;
						} else {
							Vector vec;
							for (double r = 1; r < 5; r += 0.5) {
								for (double degree = d; degree > d - 30; degree -= 5) {
									double radian1 = Math.toRadians(degree);
									vec = new Vector(Math.cos(radian1) * r, 0, Math.sin(radian1) * r);
									vec = VectorUtils.rotateZAxis(vec, roll);
									vec = VectorUtils.rotateXAxis(vec, -loc.getPitch());
									vec = VectorUtils.rotateYAxis(vec, loc.getYaw());

									Location l = loc.clone().add(0, 1.25, 0).add(vec);
									mWorld.spawnParticle(Particle.REDSTONE, l, 1, 0.1, 0.1, 0.1, FSWORD_COLOR1);
									mWorld.spawnParticle(Particle.REDSTONE, l, 1, 0.1, 0.1, 0.1, FSWORD_COLOR2);
								}
							}
							d -= 30;
						}

						if ((d >= 135 && i % 2 == 0) || (d <= 45 && i % 2 > 0)) {
							this.cancel();
						}
					}

				}.runTaskTimer(mPlugin, 0, 1);
				if (t >= FSWORD_SWINGS) {
					this.cancel();
				}
			}

		}.runTaskTimer(mPlugin, 0, 7);
		return true;
	}

	@Override
	public boolean runCheck() {
		ItemStack mHand = mPlayer.getInventory().getItemInMainHand();
		ItemStack oHand = mPlayer.getInventory().getItemInOffHand();
		return mPlayer.isSprinting() && (mHand != null && (InventoryUtils.isWandItem(mHand))
		                                 || (oHand != null && InventoryUtils.isWandItem(oHand)));
	}

}