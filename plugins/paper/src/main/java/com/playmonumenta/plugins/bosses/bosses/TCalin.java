package com.playmonumenta.plugins.bosses.bosses;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.playmonumenta.plugins.utils.FastUtils;
import com.playmonumenta.plugins.utils.PlayerUtils;
import com.playmonumenta.plugins.utils.BossUtils;
import com.playmonumenta.plugins.utils.MovementUtils;
import com.playmonumenta.plugins.utils.SerializationUtils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import com.playmonumenta.plugins.bosses.BossBarManager;
import com.playmonumenta.plugins.bosses.BossBarManager.BossHealthAction;
import com.playmonumenta.plugins.bosses.SpellManager;
import com.playmonumenta.plugins.bosses.spells.Spell;
import com.playmonumenta.plugins.bosses.spells.SpellBaseAura;
import com.playmonumenta.plugins.bosses.spells.SpellBaseBolt;
import com.playmonumenta.plugins.bosses.spells.SpellBaseCharge;
import com.playmonumenta.plugins.bosses.spells.SpellDelayedAction;

public class TCalin extends BossAbilityGroup {
	public static final String identityTag = "boss_tcalin";
	public static final int detectionRange = 60;

	private final Location mSpawnLoc;
	private final Location mEndLoc;

	public static BossAbilityGroup deserialize(Plugin plugin, LivingEntity boss) throws Exception {
		return SerializationUtils.statefulBossDeserializer(boss, identityTag, (spawnLoc, endLoc) -> new TCalin(plugin, boss, spawnLoc, endLoc));
	}

	@Override
	public String serialize() {
		return SerializationUtils.statefulBossSerializer(mSpawnLoc, mEndLoc);
	}

	public TCalin(Plugin plugin, LivingEntity boss, Location spawnLoc, Location endLoc) {
		super(plugin, identityTag, boss);
		mSpawnLoc = spawnLoc;
		mEndLoc = endLoc;
		mBoss.setRemoveWhenFarAway(false);
		World world = mSpawnLoc.getWorld();
		mBoss.addScoreboardTag("Boss");
		SpellBaseCharge charge = new SpellBaseCharge(plugin, mBoss, 20, 25, true,
			(Player player) -> {
				boss.getWorld().spawnParticle(Particle.VILLAGER_ANGRY, boss.getLocation(), 50, 2, 2, 2, 0);
				boss.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 4));
				boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1f, 1.5f);
			},
			// Warning particles
			(Location loc) -> {
				loc.getWorld().spawnParticle(Particle.SMOKE_NORMAL, loc, 1, 1, 1, 1, 0);
			},
			// Charge attack sound/particles at boss location
			(Player player) -> {
				boss.getWorld().spawnParticle(Particle.SMOKE_LARGE, boss.getLocation(), 100, 2, 2, 2, 0);
				boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_WITHER_SHOOT, 1f, 0.5f);
			},
			// Attack hit a player
			(Player player) -> {
				player.getWorld().spawnParticle(Particle.SMOKE_NORMAL, player.getLocation(), 80, 1, 1, 1, 0);
				player.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, player.getLocation(), 20, 1, 1, 1, 0.15);
				boss.getWorld().playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 1f, 0.85f);
				BossUtils.bossDamage(mBoss, player, 14);
				MovementUtils.knockAway(mBoss.getLocation(), player, 0.25f, 0.4f);
			},
			// Attack particles
			(Location loc) -> {
				loc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, loc, 1, 0.02, 0.02, 0.02, 0);
			},
			// Ending particles on boss
			() -> {
				boss.getWorld().spawnParticle(Particle.SMOKE_LARGE, boss.getLocation(), 150, 2, 2, 2, 0);
			}
		);

		SpellBaseBolt bolt = new SpellBaseBolt(plugin, mBoss, (int)(20 * 2.25), 20 * 5, 1.15, detectionRange, 0.5, true, true, 2, 10,
			(Entity entity, int tick) -> {
				float t = tick / 15;
				world.spawnParticle(Particle.EXPLOSION_NORMAL, mBoss.getLocation(), 1, 0.35, 0, 0.35, 0.05);
				world.spawnParticle(Particle.BLOCK_CRACK, mBoss.getLocation().add(0, 1, 0), 5, 0.25, 0.45, 0.25,
				0.5, Material.OAK_LEAVES.createBlockData());
				world.playSound(mBoss.getLocation(), Sound.BLOCK_GRASS_BREAK, 10, t);
				mBoss.removePotionEffect(PotionEffectType.SLOW);
				mBoss.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 2, 1));
			},

			(Entity entity) -> {
				world.playSound(mBoss.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 5, 1f);
				world.spawnParticle(Particle.SMOKE_NORMAL, mBoss.getLocation().add(0, 1, 0), 80, 0.2, 0.45, 0.2, 0.2);
				world.spawnParticle(Particle.EXPLOSION_NORMAL, mBoss.getLocation().add(0, 1, 0), 30, 0.2, 0.45, 0.2,
				0.1);
			},

			(Location loc) -> {
				world.spawnParticle(Particle.BLOCK_DUST, loc, 10, 0.35, 0.35, 0.35, 0.25,
				Material.OAK_LEAVES.createBlockData());
				world.spawnParticle(Particle.EXPLOSION_NORMAL, loc, 2, 0.2, 0.2, 0.2, 0.125);
			},

			(Player player, Location loc, boolean blocked) -> {
				if (!blocked) {
					BossUtils.bossDamage(mBoss, player, 12);
					player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 7, 1));
				}
				world.spawnParticle(Particle.BLOCK_CRACK, loc, 125, 0.35, 0.35, 0.35, 1,
				Material.OAK_LEAVES.createBlockData());
				world.spawnParticle(Particle.SMOKE_LARGE, loc, 50, 0, 0, 0, 0.25);
				world.spawnParticle(Particle.EXPLOSION_NORMAL, loc, 50, 0, 0, 0, 0.25);
				world.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1, 1.25f);
			}
		);

		SpellDelayedAction aoe = new SpellDelayedAction(plugin, mBoss.getLocation(), 20 * 3,
			// Start
			(Location loc) -> {
				mBoss.removePotionEffect(PotionEffectType.SLOW);
				mBoss.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 3, 1));
				new BukkitRunnable() {
					float mJ = 0;
					double mRotation = 0;
					double mRadius = 5;

					@Override
					public void run() {
						Location loc = mBoss.getLocation();
						mJ++;
						world.spawnParticle(Particle.SPELL_WITCH, mBoss.getLocation().add(0, 1, 0), 2, 0.25, 0.45, 0.25);
						world.playSound(mBoss.getLocation(), Sound.ENTITY_WITHER_SPAWN, 3, 0.5f + (mJ / 20));
						for (int i = 0; i < 5; i++) {
							double radian1 = Math.toRadians(mRotation + (72 * i));
							loc.add(FastUtils.cos(radian1) * mRadius, 0, FastUtils.sin(radian1) * mRadius);
							world.spawnParticle(Particle.SPELL_WITCH, loc, 6, 0.25, 0.25, 0.25, 0);
							world.spawnParticle(Particle.SPELL_MOB, loc, 4, 0.25, 0.25, 0.25, 0);
							loc.subtract(FastUtils.cos(radian1) * mRadius, 0, FastUtils.sin(radian1) * mRadius);
						}
						mRotation += 10;
						mRadius -= 0.1;
						if (mRadius <= 0 || mBoss.isDead()) {
							this.cancel();
						}
					}

				}.runTaskTimer(plugin, 0, 1);
			},
			// Warning
			(Location loc) -> {

			},
			// End
			(Location loc) -> {
				world.spawnParticle(Particle.FLAME, mBoss.getLocation().add(0, 1, 0), 200, 0, 0, 0, 0.175);
				world.spawnParticle(Particle.SMOKE_LARGE, mBoss.getLocation().add(0, 1, 0), 75, 0, 0, 0, 0.25);
				world.spawnParticle(Particle.EXPLOSION_NORMAL, mBoss.getLocation().add(0, 1, 0), 75, 0, 0, 0, 0.25);
				knockback(plugin, 5);
				for (Player player : PlayerUtils.playersInRange(mBoss.getLocation(), 5)) {
					BossUtils.bossDamage(mBoss, player, 16);
				}
			}
		);

		SpellManager activeSpells = new SpellManager(Arrays.asList(bolt, charge));

		SpellManager phase2Spells = new SpellManager(Arrays.asList(bolt, charge, aoe));

		List<Spell> passiveSpells = Arrays.asList(
			new SpellBaseAura(mBoss, 8, 5, 8, 10, Particle.FALLING_DUST,
				Material.ANVIL.createBlockData(), (Player player) -> {
					player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 60, 0, true, true));
				}
			)
		);

		Map<Integer, BossHealthAction> events = new HashMap<Integer, BossHealthAction>();
		events.put(100, mBoss -> {
			PlayerUtils.executeCommandOnNearbyPlayers(spawnLoc, detectionRange, "tellraw @s [\"\",{\"text\":\"[T'Calin] \",\"color\":\"gold\"},{\"text\":\"Thank you for foolishly opening the way into this town, hero.\",\"color\":\"white\"}]");
		});

		events.put(50, mBoss -> {
			world.playSound(mBoss.getLocation(), Sound.ENTITY_ILLUSIONER_MIRROR_MOVE, 1, 1f);
			world.spawnParticle(Particle.SPELL_WITCH, mBoss.getLocation().add(0, 1, 0), 70, 0.25, 0.45, 0.25, 0.15);
			world.spawnParticle(Particle.SMOKE_LARGE, mBoss.getLocation().add(0, 1, 0), 35, 0.1, 0.45, 0.1, 0.15);
			world.spawnParticle(Particle.EXPLOSION_NORMAL, mBoss.getLocation(), 25, 0.2, 0, 0.2, 0.1);
			mBoss.setAI(false);
			mBoss.setInvulnerable(true);
			mBoss.teleport(mSpawnLoc);
			world.playSound(mBoss.getLocation(), Sound.ENTITY_ILLUSIONER_MIRROR_MOVE, 1, 0f);
			world.spawnParticle(Particle.SPELL_WITCH, mBoss.getLocation().add(0, 1, 0), 70, 0.25, 0.45, 0.25, 0.15);
			world.spawnParticle(Particle.SMOKE_LARGE, mBoss.getLocation().add(0, 1, 0), 35, 0.1, 0.45, 0.1, 0.15);
			world.spawnParticle(Particle.EXPLOSION_NORMAL, mBoss.getLocation(), 25, 0.2, 0, 0.2, 0.1);
			super.changePhase(null, null, null);
			new BukkitRunnable() {
				float mJ = 0;
				double mRotation = 0;
				double mRadius = 10;
				Location mLoc = mBoss.getLocation();

				@Override
				public void run() {
					mJ++;
					world.spawnParticle(Particle.SPELL_WITCH, mBoss.getLocation().add(0, 1, 0), 2, 0.25, 0.45, 0.25, 0);
					world.playSound(mBoss.getLocation(), Sound.ENTITY_WITHER_SPAWN, 3, 0.5f + (mJ / 20));
					for (int i = 0; i < 5; i++) {
						double radian1 = Math.toRadians(mRotation + (72 * i));
						mLoc.add(FastUtils.cos(radian1) * mRadius, 0, FastUtils.sin(radian1) * mRadius);
						world.spawnParticle(Particle.SPELL_WITCH, mLoc, 6, 0.25, 0.25, 0.25, 0);
						world.spawnParticle(Particle.SPELL_MOB, mLoc, 4, 0.25, 0.25, 0.25, 0);
						mLoc.subtract(FastUtils.cos(radian1) * mRadius, 0, FastUtils.sin(radian1) * mRadius);
					}
					mRotation += 10;
					mRadius -= 0.25;
					if (mRadius <= 0) {
						this.cancel();
						world.playSound(mBoss.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1, 1.5f);
						world.spawnParticle(Particle.SPELL_WITCH, mBoss.getLocation().add(0, 1, 0), 70, 0.25, 0.45,
						                    0.25, 0.15);
						world.spawnParticle(Particle.SMOKE_LARGE, mBoss.getLocation().add(0, 1, 0), 35, 0.1, 0.45, 0.1,
						                    0.10);
						mBoss.setAI(true);
						mBoss.setInvulnerable(false);
						changePhase(null, passiveSpells, null);
						new BukkitRunnable() {

							@Override
							public void run() {
								changePhase(phase2Spells, passiveSpells, null);
							}

						}.runTaskLater(plugin, 20 * 10);
					}
				}

			}.runTaskTimer(plugin, 30, 1);
			PlayerUtils.executeCommandOnNearbyPlayers(spawnLoc, detectionRange, "tellraw @s [\"\",{\"text\":\"[T'Calin] \",\"color\":\"gold\"},{\"text\":\"Know the true power of the Sons of the Forest!\",\"color\":\"white\"}]");
		});

		events.put(50, mBoss -> {
			PlayerUtils.executeCommandOnNearbyPlayers(spawnLoc, detectionRange, "tellraw @s [\"\",{\"text\":\"[T'Calin] \",\"color\":\"gold\"},{\"text\":\"This cannot be!\",\"color\":\"white\"}]");
		});

		BossBarManager bossBar = new BossBarManager(plugin, boss, detectionRange, BarColor.GREEN, BarStyle.SEGMENTED_10, events);

		super.constructBoss(activeSpells, null, detectionRange, bossBar);
	}

	private void knockback(Plugin plugin, double r) {
		World world = mBoss.getWorld();
		world.playSound(mBoss.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 2, 1);
		world.playSound(mBoss.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 2, 0.5f);
		for (Player player : PlayerUtils.playersInRange(mBoss.getLocation(), r)) {
			MovementUtils.knockAway(mBoss.getLocation(), player, 0.4f);
		}
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
					world.spawnParticle(Particle.SPELL_WITCH, mLoc, 2, 0.25, 0.25, 0.25, 0);
					world.spawnParticle(Particle.SMOKE_LARGE, mLoc, 2, 0.1, 0.1, 0.1, 0.1);
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
	public void death(EntityDeathEvent event) {
		PlayerUtils.executeCommandOnNearbyPlayers(mBoss.getLocation(), detectionRange, "playsound minecraft:entity.wither.death master @s ~ ~ ~ 100 0.8");
		mEndLoc.getBlock().setType(Material.REDSTONE_BLOCK);
	}

	@Override
	public void init() {
		int bossTargetHp = 0;
		int playerCount = BossUtils.getPlayersInRangeForHealthScaling(mBoss, detectionRange);
		int hpDelta = 512;
		int armor = (int)(Math.sqrt(playerCount * 2) - 1);
		while (playerCount > 0) {
			bossTargetHp = bossTargetHp + hpDelta;
			hpDelta = hpDelta / 2;
			playerCount--;
		}
		mBoss.getAttribute(Attribute.GENERIC_ARMOR).setBaseValue(armor);
		mBoss.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(bossTargetHp);
		mBoss.setHealth(bossTargetHp);
		PlayerUtils.executeCommandOnNearbyPlayers(mBoss.getLocation(), detectionRange, "title @s title [\"\",{\"text\":\"T'Calin\",\"color\":\"green\",\"bold\":true}]");
		PlayerUtils.executeCommandOnNearbyPlayers(mBoss.getLocation(), detectionRange, "title @s subtitle [\"\",{\"text\":\"Forest Battlemage\",\"color\":\"dark_green\",\"bold\":true}]");
		PlayerUtils.executeCommandOnNearbyPlayers(mBoss.getLocation(), detectionRange, "playsound minecraft:entity.wither.spawn master @s ~ ~ ~ 10 1.25");
	}
}
