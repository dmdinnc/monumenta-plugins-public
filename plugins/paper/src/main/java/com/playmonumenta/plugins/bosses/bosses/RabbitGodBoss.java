package com.playmonumenta.plugins.bosses.bosses;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import com.playmonumenta.plugins.bosses.BossBarManager;
import com.playmonumenta.plugins.bosses.BossBarManager.BossHealthAction;
import com.playmonumenta.plugins.bosses.SpellManager;
import com.playmonumenta.plugins.bosses.spells.Spell;
import com.playmonumenta.plugins.bosses.spells.SpellBaseCharge;
import com.playmonumenta.plugins.bosses.spells.SpellBaseLaser;
import com.playmonumenta.plugins.bosses.spells.SpellBaseParticleAura;
import com.playmonumenta.plugins.bosses.spells.SpellConditionalTeleport;
import com.playmonumenta.plugins.bosses.spells.SpellPlayerAction;
import com.playmonumenta.plugins.bosses.spells.cluckingop.SpellEruption;
import com.playmonumenta.plugins.bosses.spells.cluckingop.SpellFluffPools;
import com.playmonumenta.plugins.bosses.spells.cluckingop.SpellFluffingDeath;
import com.playmonumenta.plugins.bosses.spells.cluckingop.SpellOmegaLeap;
import com.playmonumenta.plugins.utils.BossUtils;
import com.playmonumenta.plugins.utils.FastUtils;
import com.playmonumenta.plugins.utils.PlayerUtils;
import com.playmonumenta.plugins.utils.SerializationUtils;

public class RabbitGodBoss extends BossAbilityGroup {
	public static final String identityTag = "boss_rabbitgod";
	public static final int detectionRange = 30;

	private final Location mSpawnLoc;
	private final Location mEndLoc;

	private boolean mPhase2;

	public static BossAbilityGroup deserialize(Plugin plugin, LivingEntity boss) throws Exception {
		return SerializationUtils.statefulBossDeserializer(boss, identityTag, (spawnLoc, endLoc) -> {
			return new RabbitGodBoss(plugin, boss, spawnLoc, endLoc);
		});
	}

	@Override
	public String serialize() {
		return SerializationUtils.statefulBossSerializer(mSpawnLoc, mEndLoc);
	}

	public RabbitGodBoss(Plugin plugin, LivingEntity boss, Location spawnLoc, Location endLoc) {
		super(plugin, identityTag, boss);
		mSpawnLoc = spawnLoc;
		mEndLoc = endLoc;
		mPhase2 = false;
		mBoss.setRemoveWhenFarAway(false);
		World world = mBoss.getWorld();

		SpellBaseCharge charge = new SpellBaseCharge(plugin, mBoss, 25, 10, false, 12, 6,
			(Player player) -> {
				boss.getWorld().spawnParticle(Particle.VILLAGER_ANGRY, boss.getLocation(), 50, 2, 2, 2, 0);
				boss.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 4));
				boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_WITHER_AMBIENT, 1.5f, 0.5f);
			},
			// Warning particles
			(Location loc) -> {
				loc.getWorld().spawnParticle(Particle.CLOUD, loc, 1, 1, 1, 1, 0);
			},
			// Charge attack sound/particles at boss location
			(Player player) -> {
				boss.getWorld().spawnParticle(Particle.SMOKE_LARGE, boss.getLocation(), 100, 2, 2, 2, 0);
				boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1f, 0.5f);
				boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1f, 1f);
				boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_PIG_DEATH, 1f, 0.85f);
			},
			// Attack hit a player
			(Player player) -> {
				player.getWorld().spawnParticle(Particle.SWEEP_ATTACK, player.getLocation(), 20, 1, 1, 1, 0);
				BossUtils.bossDamage(boss, player, 1);
			},
			// Attack particles
			(Location loc) -> {
				loc.getWorld().spawnParticle(Particle.SWEEP_ATTACK, loc, 2, 0.75, 0.75, 0.75, 0);
			},
			// Ending particles on boss
			() -> {
				boss.getWorld().spawnParticle(Particle.SMOKE_LARGE, boss.getLocation(), 75, 2, 2, 2, 0);
			}
		);

		SpellBaseLaser laser = new SpellBaseLaser(plugin, mBoss, 60, 60, false, false, 20,
			// Tick action per player
			(Player player, int ticks, boolean blocked) -> {
				player.playSound(player.getLocation(), Sound.UI_TOAST_IN, 2, 0.5f + (ticks / 50f) * 1.5f);
				boss.getLocation().getWorld().playSound(boss.getLocation(), Sound.UI_TOAST_IN, 2, 0.5f + (ticks / 100f) * 1.5f);
				player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 2, 0.5f + (ticks / 50f) * 1.5f);
				boss.getLocation().getWorld().playSound(boss.getLocation(), Sound.UI_TOAST_IN, 2, 0.5f + (ticks / 100f) * 1.5f);
			},
			// Particles generated by the laser
			(Location loc) -> {
				loc.getWorld().spawnParticle(Particle.SMOKE_LARGE, loc, 1, 0.02, 0.02, 0.02, 0);
				loc.getWorld().spawnParticle(Particle.FLAME, loc, 1, 0.02, 0.02, 0.02, 1);
			},
			// TNT generated at the end of the attack
			(Player player, Location loc, boolean blocked) -> {
				world.spawnParticle(Particle.FLAME, loc, 100, 0, 0, 0, 0.175);
				world.spawnParticle(Particle.SMOKE_LARGE, loc, 25, 0, 0, 0, 0.25);
				world.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 0.5f);
				if (!blocked) {
					BossUtils.bossDamage(mBoss, player, 1);
				}
			}
		);

		SpellManager phase1Spells = new SpellManager(Arrays.asList(
			charge,
			laser,
			new SpellOmegaLeap(plugin, mBoss),
			new SpellEruption(plugin, mBoss)
		));

		SpellManager phase2Spells = new SpellManager(Arrays.asList(
			charge,
			new SpellOmegaLeap(plugin, mBoss),
			new SpellFluffPools(plugin, mBoss, detectionRange),
			new SpellFluffingDeath(plugin, mBoss, 15, spawnLoc)
		));
		List<Spell> passiveSpells = Arrays.asList(
			new SpellConditionalTeleport(mBoss, spawnLoc, b -> spawnLoc.distance(b.getLocation()) > 30),
			new SpellBaseParticleAura(boss, 1,
				(LivingEntity mBoss) -> {
					world.spawnParticle(Particle.FLAME, mBoss.getLocation().add(0, mBoss.getHeight() / 2, 0), 5, 0.35, 0.45,
					0.35, 0.1);
				}
			)
		);

		List<Spell> passive2Spells = Arrays.asList(
			new SpellConditionalTeleport(mBoss, spawnLoc, b -> spawnLoc.distance(b.getLocation()) > 30),
			new SpellPlayerAction(mBoss, detectionRange,
				(Player player) -> {
					world.spawnParticle(Particle.SPELL_INSTANT, player.getLocation().add(0, 1, 0), 3, 0.4, 0.4, 0.4, 0);
					world.spawnParticle(Particle.FLAME, player.getLocation().add(0, 1, 0), 2, 0.4, 0.4, 0.4, 0.1);
					world.spawnParticle(Particle.EXPLOSION_NORMAL, player.getLocation(), 6, 0.2, 0, 0.2, 0);
				}
			),
			new SpellBaseParticleAura(boss, 1,
				(LivingEntity mBoss) -> {
					world.spawnParticle(Particle.FLAME, mBoss.getLocation().add(0, mBoss.getHeight() / 2, 0), 5, 0.35, 0.45, 0.35, 0.1);
				}
			)
		);

		Map<Integer, BossHealthAction> events = new HashMap<Integer, BossHealthAction>();

		events.put(100, mBoss -> {
			mBoss.setAI(false);
			mBoss.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 20 * 10, 10));
			Location to = spawnLoc.clone().add(0, 10, 0);
			world.playSound(mBoss.getLocation(), Sound.ENTITY_WITHER_SHOOT, 1, 0f);
			world.spawnParticle(Particle.SPELL_WITCH, mBoss.getLocation(), 70, 0.25, 0.45, 0.25, 0.15);
			world.spawnParticle(Particle.SMOKE_LARGE, mBoss.getLocation(), 35, 0.1, 0.45, 0.1, 0.15);
			world.spawnParticle(Particle.EXPLOSION_NORMAL, mBoss.getLocation(), 25, 0.2, 0, 0.2, 0.1);
			mBoss.teleport(to);
			world.playSound(mBoss.getLocation(), Sound.ENTITY_WITHER_SHOOT, 1, 0f);
			world.spawnParticle(Particle.SPELL_WITCH, to, 70, 0.25, 0.45, 0.25, 0.15);
			world.spawnParticle(Particle.SMOKE_LARGE, to, 35, 0.1, 0.45, 0.1, 0.15);
			world.spawnParticle(Particle.EXPLOSION_NORMAL, to, 25, 0.2, 0, 0.2, 0.1);
			new BukkitRunnable() {
				int mTime = 0;
				double mY = 10;
				@Override
				public void run() {
					mTime++;
					mY -= 0.1;
					if (mTime % 6 == 0) {
						Location loc = spawnLoc.clone().add(FastUtils.randomDoubleInRange(-15, 15), 5 + FastUtils.randomDoubleInRange(-5, 2), FastUtils.randomDoubleInRange(-15, 15));
						world.spawnParticle(Particle.EXPLOSION_NORMAL, loc, 50, 0, 0, 0, 0.175);
						world.spawnParticle(Particle.FLAME, loc, 100, 0, 0, 0, 0.175);
						world.playSound(spawnLoc, Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 1f);
					}

					if (mY <= 0) {
						this.cancel();
						mBoss.setAI(true);
						mBoss.teleport(spawnLoc);
						world.spawnParticle(Particle.FLAME, spawnLoc, 175, 0, 0, 0, 0.175);
						world.spawnParticle(Particle.SMOKE_LARGE, spawnLoc, 50, 0, 0, 0, 0.25);
						world.spawnParticle(Particle.EXPLOSION_NORMAL, spawnLoc, 50, 0, 0, 0, 0.175);
						world.playSound(spawnLoc, Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 0.5f);
						world.playSound(spawnLoc, Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 1f);
						PlayerUtils.executeCommandOnNearbyPlayers(mBoss.getLocation(), detectionRange, "tellraw @s [\"\",{\"text\":\"OINK OINK OINK! OINK OINK OINK, OINK OINK OINK OINK!!! OOIIINNNKKK!!!\",\"color\":\"dark_red\"}]");
						world.playSound(mBoss.getLocation(), Sound.ENTITY_PIG_AMBIENT, 1.5f, 1f);
					}
					mBoss.teleport(mSpawnLoc.clone().add(0, mY, 0));
				}

			}.runTaskTimer(plugin, 0, 1);
		});

		//What an anime protagonist feels like
		events.put(90, mBoss -> {
			String[] dio = new String[] {
				"OINK. OINK. OINK OINK.",
				"OINK OINK. OINK OINK OINK. OINK OINK!",
				"OINK OINK, OINK OINK OINK OINK. OOINKKKK!!"
			};
			changePhase(null, null, null);
			knockback(plugin, 10);
			mBoss.setAI(false);
			mBoss.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 20 * 10, 10));
			Location to = mSpawnLoc;
			world.playSound(mBoss.getLocation(), Sound.ENTITY_WITHER_SHOOT, 1, 0f);
			world.spawnParticle(Particle.SPELL_WITCH, mBoss.getLocation(), 70, 0.25, 0.45, 0.25, 0.15);
			world.spawnParticle(Particle.SMOKE_LARGE, mBoss.getLocation(), 35, 0.1, 0.45, 0.1, 0.15);
			world.spawnParticle(Particle.EXPLOSION_NORMAL, mBoss.getLocation(), 25, 0.2, 0, 0.2, 0.1);
			mBoss.teleport(to);
			world.playSound(mBoss.getLocation(), Sound.ENTITY_WITHER_SHOOT, 1, 0f);
			world.spawnParticle(Particle.SPELL_WITCH, to, 70, 0.25, 0.45, 0.25, 0.15);
			world.spawnParticle(Particle.SMOKE_LARGE, to, 35, 0.1, 0.45, 0.1, 0.15);
			world.spawnParticle(Particle.EXPLOSION_NORMAL, to, 25, 0.2, 0, 0.2, 0.1);
			for (Player player : PlayerUtils.playersInRange(mSpawnLoc, detectionRange)) {
				player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 9999, 20));
				player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 20 * 9999, -5));
			}

			new BukkitRunnable() {
				int mTime = 0;

				@Override
				public void run() {
					world.playSound(mBoss.getLocation(), Sound.ENTITY_PIG_AMBIENT, 1.5f, 1f);
					PlayerUtils.executeCommandOnNearbyPlayers(mSpawnLoc, detectionRange, "tellraw @s [\"\",{\"text\":\"" + dio[mTime].toUpperCase() + "\",\"color\":\"dark_red\"}]");
					mTime++;
					if (mTime == dio.length) {
						this.cancel();
						new BukkitRunnable() {
							double mRotation = 0;
							Location mLoc = mSpawnLoc.clone().add(5, -1.25, 0);
							double mRadius = 5;

							@Override
							public void run() {

								mRadius -= 0.25;
								for (int i = 0; i < 15; i += 1) {
									mRotation += 24;
									double radian1 = Math.toRadians(mRotation);
									mLoc.add(FastUtils.cos(radian1) * mRadius, 0, FastUtils.sin(radian1) * mRadius);
									world.spawnParticle(Particle.SPELL_INSTANT, mLoc, 3, 0.1, 0.1, 0.1, 0.1);
									mLoc.subtract(FastUtils.cos(radian1) * mRadius, 0, FastUtils.sin(radian1) * mRadius);

								}
								if (mRadius <= 0) {
									this.cancel();
									world.spawnParticle(Particle.CLOUD, mLoc, 25, 0, 0, 0, 0.125);
									world.playSound(mLoc, Sound.ENTITY_ILLUSIONER_MIRROR_MOVE, 1, 1);
									Chicken chicken = (Chicken) world.spawnEntity(mLoc, EntityType.CHICKEN);
									chicken.setAI(false);
									chicken.setAdult();
									chicken.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 20 * 999, 10));
									chicken.setCustomName(ChatColor.AQUA + "" + ChatColor.BOLD + "Godly Clucking Spirit");
									chicken.setCustomNameVisible(true);
									world.playSound(chicken.getLocation(), Sound.ENTITY_CHICKEN_HURT, 1, 1);
									PlayerUtils.executeCommandOnNearbyPlayers(spawnLoc, detectionRange, "tellraw @s [\"\",{\"text\":\"[Godly Clucking Spirit] \",\"color\":\"gold\"},{\"text\":\"Cluck Cluck!\",\"color\":\"white\"}]");
									new BukkitRunnable() {

										@Override
										public void run() {
											world.spawnParticle(Particle.SPELL_INSTANT, chicken.getLocation().add(0, 0.25, 0), 1, 0.3, 0.3, 0.3, 0);
											if (chicken.isDead() || !chicken.isValid()) {
												this.cancel();
											}
										}

									}.runTaskTimer(plugin, 0, 1);

									new BukkitRunnable() {
										int mTime = 0;
										@Override
										public void run() {
											if (mTime == 0) {
												world.playSound(mBoss.getLocation(), Sound.ENTITY_PIG_DEATH, 1.5f, 1f);
												PlayerUtils.executeCommandOnNearbyPlayers(mBoss.getLocation(), detectionRange, "tellraw @s [\"\",{\"text\":\"OINK!? OINK OINK OINK!?!\",\"color\":\"dark_red\"}]");
											}
											mTime++;

											if (mTime >= 20 * 4) {
												this.cancel();
												PlayerUtils.executeCommandOnNearbyPlayers(spawnLoc, detectionRange, "tellraw @s [\"\",{\"text\":\"[Godly Clucking Spirit] \",\"color\":\"gold\"},{\"text\":\"Cluck. Cluck Cluck Cluck Cluck. Cluck CLUCK!!!\",\"color\":\"white\"}]");
												world.playSound(chicken.getLocation(), Sound.ENTITY_CHICKEN_HURT, 1, 1);
												world.playSound(chicken.getLocation(), Sound.BLOCK_PORTAL_TRIGGER, 1, 1.35f);
												new BukkitRunnable() {
													double mRotation = 0;
													Location mLoc = spawnLoc.clone().add(5, -1.25, 0);
													double mRadius = 10;

													@Override
													public void run() {
														for (Player player : PlayerUtils.playersInRange(mSpawnLoc, detectionRange)) {
															world.spawnParticle(Particle.SPELL_INSTANT, player.getLocation().add(0, 0.25, 0), 1, 0.3, 0.3, 0.3, 0);
														}
														mRadius -= 0.2;
														for (int i = 0; i < 24; i += 1) {
															mRotation += 15;
															double radian1 = Math.toRadians(mRotation);
															mLoc.add(FastUtils.cos(radian1) * mRadius, 0, FastUtils.sin(radian1) * mRadius);
															world.spawnParticle(Particle.SPELL_INSTANT, mLoc, 3, 0.1, 0.1, 0.1, 0.1);
															mLoc.subtract(FastUtils.cos(radian1) * mRadius, 0, FastUtils.sin(radian1) * mRadius);
														}

														if (mRadius <= 0) {
															this.cancel();
															world.spawnParticle(Particle.CLOUD, chicken.getLocation(), 25, 0, 0, 0, 0.125);
															world.playSound(chicken.getLocation(), Sound.ENTITY_ILLUSIONER_MIRROR_MOVE, 1, 2);
															world.playSound(chicken.getLocation(), Sound.BLOCK_PORTAL_TRAVEL, 2, 2f);
															world.playSound(chicken.getLocation(), Sound.ENTITY_CHICKEN_DEATH, 2, 0.5f);
															chicken.remove();
															changePhase(null, passive2Spells, null);
															for (Player player : PlayerUtils.playersInRange(mSpawnLoc, detectionRange)) {
																world.spawnParticle(Particle.FIREWORKS_SPARK, player.getLocation().add(0, 1, 0), 100, 0, 0, 0, 0.4f);
																player.removePotionEffect(PotionEffectType.SLOW);
																if (player.hasPotionEffect(PotionEffectType.JUMP)) {
																	PotionEffect effect = player.getPotionEffect(PotionEffectType.JUMP);
																	if (effect.getAmplifier() < 0) {
																		player.removePotionEffect(PotionEffectType.JUMP);
																	}
																}
																player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 20 * 999, 9));
																player.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, 20 * 999, 9));
																player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 999, 2));
															}
															mBoss.setAI(true);
															mPhase2 = true;
															mBoss.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
															PlayerUtils.executeCommandOnNearbyPlayers(mBoss.getLocation(), detectionRange, "tellraw @s [\"\",{\"text\":\"Your curse of Clucking is suddenly brought out in power, making you feel extremely powerful!\",\"color\":\"aqua\"}]");
															PlayerUtils.executeCommandOnNearbyPlayers(mBoss.getLocation(), detectionRange, "tellraw @s [\"\",{\"text\":\"You have gained OP AS CLUCK powers!! Cluck em' up!\",\"color\":\"aqua\"}]");
															new BukkitRunnable() {
																int mTime = 0;
																@Override
																public void run() {
																	mTime++;
																	if (mTime == 1) {
																		world.playSound(mBoss.getLocation(), Sound.ENTITY_PIG_AMBIENT, 1.5f, 1f);
																		PlayerUtils.executeCommandOnNearbyPlayers(mBoss.getLocation(), detectionRange, "tellraw @s [\"\",{\"text\":\"OINK!! OINK OINK OINK!!! OINK OINK!!!!!!!\",\"color\":\"dark_red\"}]");
																	} else if (mTime == 2) {
																		changePhase(phase2Spells, passive2Spells, null);
																		this.cancel();
																	}
																}
															}.runTaskTimer(plugin, 20 * 5, 20 * 5);
														}
													}
												}.runTaskTimer(plugin, 0, 1);
											}
										}
									}.runTaskTimer(plugin, 30, 1);
								}
							}
						}.runTaskTimer(plugin, 20 * 3, 1);
					}
				}
			}.runTaskTimer(plugin, 0, 20 * 6);
		});

		BossBarManager bossBar = new BossBarManager(plugin, boss, detectionRange, BarColor.RED, BarStyle.SEGMENTED_10, events);

		super.constructBoss(phase1Spells, passiveSpells, detectionRange, bossBar, 20 * 15);
	}

	private void knockback(Plugin plugin, double r) {
		World world = mBoss.getWorld();
		world.playSound(mBoss.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 2, 1);
		world.playSound(mBoss.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 2, 0.5f);
		world.playSound(mBoss.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 2, 0f);
		new BukkitRunnable() {
			double mRotation = 0;
			Location mLoc = mBoss.getLocation();
			double mRadius = 0;
			double mY = 2.5;
			double mYminus = 0.35;

			@Override
			public void run() {

				mRadius += 1;
				for (int i = 0; i < 15; i += 1) {
					mRotation += 24;
					double radian1 = Math.toRadians(mRotation);
					mLoc.add(FastUtils.cos(radian1) * mRadius, mY, FastUtils.sin(radian1) * mRadius);
					world.spawnParticle(Particle.EXPLOSION_NORMAL, mLoc, 3, 0.1, 0.1, 0.1, 0.1);
					mLoc.subtract(FastUtils.cos(radian1) * mRadius, mY, FastUtils.sin(radian1) * mRadius);

				}
				mY -= mY * mYminus;
				mYminus += 0.02;
				if (mYminus >= 1) {
					mYminus = 1;
				}
				if (mRadius >= r) {
					this.cancel();
				}

			}

		}.runTaskTimer(plugin, 0, 1);
	}

	@Override
	public void bossDamagedByEntity(EntityDamageByEntityEvent event) {
		if (mPhase2) {
			event.setDamage(event.getDamage() * 15);
		}
	}

	@Override
	public void death(EntityDeathEvent event) {
		World world = mBoss.getWorld();
		mBoss.setHealth(800);
		changePhase(null, null, null);
		mBoss.setAI(false);
		mBoss.setInvulnerable(true);
		PlayerUtils.executeCommandOnNearbyPlayers(mBoss.getLocation(), detectionRange, "tellraw @s [\"\",{\"text\":\"OINK! OINK OINK! OINK OINK OINK OINK!?!?!?\",\"color\":\"dark_red\"}]");
		new BukkitRunnable() {
			int mTime = 0;
			@Override
			public void run() {
				mTime++;
				Location loc = mBoss.getLocation().add(FastUtils.randomDoubleInRange(-10, 10), FastUtils.randomDoubleInRange(0, 3), FastUtils.randomDoubleInRange(-10, 10));
				world.spawnParticle(Particle.EXPLOSION_NORMAL, loc, 50, 0, 0, 0, 0.175);
				world.spawnParticle(Particle.FLAME, loc, 100, 0, 0, 0, 0.175);
				world.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 1f);

				if (mTime >= 12) {
					PlayerUtils.executeCommandOnNearbyPlayers(mBoss.getLocation(), detectionRange, "tellraw @s [\"\",{\"text\":\"OOOOOOIIIIIIINNNNNNNNNNKKKKKKKKK!!!!!!!\",\"color\":\"dark_red\"}]");
					world.playSound(mBoss.getLocation(), Sound.ENTITY_PIG_DEATH, 1.5f, 0.75f);
					world.playSound(mBoss.getLocation(), Sound.ENTITY_PIG_DEATH, 1.5f, 1f);
					mBoss.remove();
					this.cancel();
					world.spawnParticle(Particle.FLAME, mBoss.getLocation(), 300, 0, 0, 0, 0.2);
					world.spawnParticle(Particle.SMOKE_LARGE, mBoss.getLocation(), 150, 0, 0, 0, 0.25);
					world.spawnParticle(Particle.EXPLOSION_NORMAL, mBoss.getLocation(), 150, 0, 0, 0, 0.25);
					world.playSound(mBoss.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 0.5f);
					world.playSound(mBoss.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 1f);
					world.playSound(mBoss.getLocation(), Sound.ENTITY_ENDERMAN_DEATH, 1.5f, 0f);
					for (Player player : PlayerUtils.playersInRange(mSpawnLoc, detectionRange)) {
						player.removePotionEffect(PotionEffectType.HEALTH_BOOST);
						player.removePotionEffect(PotionEffectType.ABSORPTION);
						if (player.hasPotionEffect(PotionEffectType.SPEED)) {
							PotionEffect effect = player.getPotionEffect(PotionEffectType.SPEED);
							if (effect.getAmplifier() > 1) {
								player.removePotionEffect(PotionEffectType.SPEED);
							}
						}
					}
					new BukkitRunnable() {

						@Override
						public void run() {
							mEndLoc.getBlock().setType(Material.REDSTONE_BLOCK);
							PlayerUtils.executeCommandOnNearbyPlayers(mBoss.getLocation(), detectionRange, "playsound minecraft:ui.toast.challenge_complete master @s ~ ~ ~ 100 1.15");
							PlayerUtils.executeCommandOnNearbyPlayers(mBoss.getLocation(), detectionRange, "title @s title [\"\",{\"text\":\"VICTORY\",\"color\":\"green\",\"bold\":true}]");
							PlayerUtils.executeCommandOnNearbyPlayers(mBoss.getLocation(), detectionRange, "title @s subtitle [\"\",{\"text\":\"April Clucking Fools\",\"color\":\"dark_green\",\"bold\":true}]");
						}

					}.runTaskLater(mPlugin, 20 * 3);

				}
			}

		}.runTaskTimer(mPlugin, 0, 5);
	}

	@Override
	public void init() {
		int bossTargetHp = 0;
		int playerCount = BossUtils.getPlayersInRangeForHealthScaling(mBoss, detectionRange);
		int hpDelta = 9001;
		int armor = (int)(Math.sqrt(playerCount * 2) - 1);
		while (playerCount > 0) {
			bossTargetHp = bossTargetHp + hpDelta;
			hpDelta = (hpDelta / 2) + 25;
			playerCount--;
		}
		mBoss.getAttribute(Attribute.GENERIC_ARMOR).setBaseValue(armor);
		mBoss.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(bossTargetHp);
		mBoss.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(1);
		mBoss.setHealth(bossTargetHp);
		mBoss.setCustomName(ChatColor.DARK_RED + "" + ChatColor.BOLD + "The Pig God");
		PlayerUtils.executeCommandOnNearbyPlayers(mBoss.getLocation(), detectionRange, "tellraw @s [\"\",{\"text\":\"OINK OINK!!! OINK OINK, OINK OINK OINK!?!?\",\"color\":\"dark_red\"}]");
		PlayerUtils.executeCommandOnNearbyPlayers(mBoss.getLocation(), detectionRange, "title @s title [\"\",{\"text\":\"The Pig God\",\"color\":\"dark_red\",\"bold\":true}]");
		PlayerUtils.executeCommandOnNearbyPlayers(mBoss.getLocation(), detectionRange, "title @s subtitle [\"\",{\"text\":\"A Broken Clucking Boss\",\"color\":\"red\",\"bold\":true}]");
		PlayerUtils.executeCommandOnNearbyPlayers(mBoss.getLocation(), detectionRange, "playsound minecraft:entity.wither.spawn master @s ~ ~ ~ 10 1.25");
	}
}
