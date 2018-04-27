package pe.project.classes;

import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import pe.project.Plugin;
import pe.project.managers.potion.PotionManager.PotionID;
import pe.project.utils.EntityUtils;
import pe.project.utils.InventoryUtils;
import pe.project.utils.MovementUtils;
import pe.project.utils.ParticleUtils;
import pe.project.utils.PlayerUtils;
import pe.project.utils.ScoreboardUtils;
import pe.project.utils.particlelib.ParticleEffect;

/*
	CounterStrike
	Frenzy
	Riposte				[formerly Obliteration]
	DefensiveLine
	BruteForce
	Toughness
	WeaponMastery
*/

public class WarriorClass extends BaseClass {
	private static final float COUNTER_STRIKE_RADIUS = 5.0f;

	private static final int FRENZY_DURATION = 5 * 20;

	private static final int RIPOSTE_COOLDOWN = 10 * 20;
	private static final int RIPOSTE_SWORD_EFFECT_LEVEL = 1;
	private static final int RIPOSTE_SWORD_DURATION = 5 * 20;
	private static final int RIPOSTE_AXE_DURATION = 3 * 20;
	private static final int RIPOSTE_AXE_EFFECT_LEVEL = 6;
	private static final double RIPOSTE_SQRADIUS = 6.25;	//radius = 2.5, this is it squared
	private static final float RIPOSTE_KNOCKBACK_SPEED = 0.15f;

	private static final Integer DEFENSIVE_LINE_DURATION = 10 * 20;
	private static final float DEFENSIVE_LINE_RADIUS = 8.0f;
	private static final Integer DEFENSIVE_LINE_1_COOLDOWN = 60 * 20;
	private static final Integer DEFENSIVE_LINE_2_COOLDOWN = 30 * 20;

	private static final float BRUTE_FORCE_RADIUS = 2.0f;
	private static final Integer BRUTE_FORCE_1_DAMAGE = 3;
	private static final Integer BRUTE_FORCE_2_DAMAGE = 7;
	private static final float BRUTE_FORCE_KNOCKBACK_SPEED = 0.5f;

	private static final double PASSIVE_KNOCKBACK_RESISTANCE = 0.2;

	public WarriorClass(Plugin plugin, Random random) {
		super(plugin, random);
	}

	@Override
	public void setupClassPotionEffects(Player player) {
		ItemStack mainHand = player.getInventory().getItemInMainHand();
		_testItemInHand(player, mainHand);
		_testToughness(player);
	}

	@Override
	public boolean PlayerDamagedByLivingEntityEvent(Player player, LivingEntity damager, double damage) {
		if (!(damager instanceof Player)) {
			//	ABILITY: Counter Strike
			{
				//	If we're not going to succeed in our Random we probably don't want to attempt to grab the scoreboard value anyways.
				if (mRandom.nextFloat() < 0.15f) {
					int counterStrike = ScoreboardUtils.getScoreboardValue(player, "CounterStrike");
					if (counterStrike > 0) {
						Location loc = player.getLocation();
						player.spawnParticle(Particle.SWEEP_ATTACK, loc.getX(), loc.getY() + 1.5D, loc.getZ(), 20, 1.5D, 1.5D, 1.5D);
						player.playSound(loc,  Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK, 0.5f, 0.7f);

						double csDamage = counterStrike == 1 ? 12D : 24D;

						List<Entity> entities = player.getNearbyEntities(COUNTER_STRIKE_RADIUS, COUNTER_STRIKE_RADIUS, COUNTER_STRIKE_RADIUS);
						for(int i = 0; i < entities.size(); i++) {
							Entity e = entities.get(i);
							if(EntityUtils.isHostileMob(e)) {
								EntityUtils.damageEntity(mPlugin, (LivingEntity)e, csDamage, player);
							}
						}
					}
				}
			}

			// ABILITY: Riposte
			{
				if ((player.getLocation()).distanceSquared(damager.getLocation()) < RIPOSTE_SQRADIUS) {
					// currently leaving the scoreboard as Obliteration for back-compatibility
					int riposte = ScoreboardUtils.getScoreboardValue(player, "Obliteration");
					if (riposte > 0) {
						if (!mPlugin.mTimers.isAbilityOnCooldown(player.getUniqueId(), Spells.RIPOSTE)) {
							if (!(damager instanceof Creeper)) {
								ItemStack mainHand = player.getInventory().getItemInMainHand();
								MovementUtils.KnockAway(player, damager, RIPOSTE_KNOCKBACK_SPEED);

								if (InventoryUtils.isAxeItem(mainHand) || InventoryUtils.isSwordItem(mainHand)) {
									World world = player.getWorld();

									if (riposte > 1) {
										if (InventoryUtils.isSwordItem(mainHand)) {
											player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, RIPOSTE_SWORD_DURATION, RIPOSTE_SWORD_EFFECT_LEVEL, true, false));
										}
										else if (InventoryUtils.isAxeItem(mainHand)) {
											damager.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, RIPOSTE_AXE_DURATION, RIPOSTE_AXE_EFFECT_LEVEL, true, false));
										}
									}

									world.playSound(player.getLocation(), "block.anvil.land", 0.5f, 1.5f);
									ParticleUtils.playParticlesInWorld(world, Particle.SWEEP_ATTACK, (player.getLocation()).add(0, 1, 0), 18, 0.75, 0.5, 0.75, 0.001);
									ParticleUtils.playParticlesInWorld(world, Particle.CRIT_MAGIC, (player.getLocation()).add(0, 1, 0), 20, 0.75, 0.5, 0.75, 0.001);
									mPlugin.mTimers.AddCooldown(player.getUniqueId(), Spells.RIPOSTE, RIPOSTE_COOLDOWN);

									return false;
								}
							}
						}
					}
				}
			}
		}
		return true;
	}

	@Override
	public boolean LivingEntityDamagedByPlayerEvent(Player player, LivingEntity damagee, double damage, DamageCause cause) {
		//	BRUTE FORCE!!!
		{
			int bruteForce = ScoreboardUtils.getScoreboardValue(player, "BruteForce");
			if (bruteForce > 0) {
				if (PlayerUtils.isCritical(player) && cause != DamageCause.PROJECTILE) {
					ItemStack mainHand = player.getInventory().getItemInMainHand();
					if (InventoryUtils.isAxeItem(mainHand) || InventoryUtils.isSwordItem(mainHand) || InventoryUtils.isScytheItem(mainHand)) {

						Location loc = damagee.getLocation().add(0, damagee.getHeight() / 2, 0);
						ParticleEffect.EXPLOSION_LARGE.display(0, 0, 0, 1, 1, loc, 40);
						ParticleEffect.EXPLOSION_NORMAL.display(0, 0, 0, 0.135f, 10, loc, 40);
						List<Entity> entities = damagee.getNearbyEntities(BRUTE_FORCE_RADIUS, BRUTE_FORCE_RADIUS, BRUTE_FORCE_RADIUS);
						entities.add(damagee);
						for(int i = 0; i < entities.size(); i++) {
							Entity e = entities.get(i);
							if(EntityUtils.isHostileMob(e)) {
								LivingEntity mob = (LivingEntity)e;

								Integer extraDamage = bruteForce == 1 ? BRUTE_FORCE_1_DAMAGE : BRUTE_FORCE_2_DAMAGE;
								EntityUtils.damageEntity(mPlugin, mob, extraDamage, player);

								MovementUtils.KnockAway(player, mob, BRUTE_FORCE_KNOCKBACK_SPEED);
							}
						}
					}
				}
			}
		}

		return false;
	}

	@Override
	public void PlayerItemHeldEvent(Player player, ItemStack mainHand, ItemStack offHand) {
		_testItemInHand(player, mainHand);
	}

	@Override
	public void PlayerRespawnEvent(Player player) {
		_testToughness(player);

		AttributeInstance att = player.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE);
		att.setBaseValue(0);
		att.setBaseValue(PASSIVE_KNOCKBACK_RESISTANCE);
	}

	@Override
	public void EntityDeathEvent(Player player, LivingEntity killedEntity, DamageCause cause, boolean shouldGenDrops) {
		int frenzy = ScoreboardUtils.getScoreboardValue(player, "Frenzy");
		if (frenzy > 0) {
			ItemStack mainHand = player.getInventory().getItemInMainHand();
			if (!InventoryUtils.isPickaxeItem(mainHand)) {
				int hasteAmp = frenzy == 1 ? 2 : 3;

				mPlugin.mPotionManager.addPotion(player, PotionID.ABILITY_SELF, new PotionEffect(PotionEffectType.FAST_DIGGING, FRENZY_DURATION, hasteAmp, true, false));

				World world = Bukkit.getWorld(player.getWorld().getName());
				Location loc = player.getLocation();
				world.playSound(loc, "entity.polar_bear.hurt", 0.1f, 1.0f);

				if (frenzy > 1) {
					mPlugin.mPotionManager.addPotion(player, PotionID.ABILITY_SELF, new PotionEffect(PotionEffectType.SPEED, FRENZY_DURATION, 0, true, false));
				}
			}
		}
	}

	@Override
	public void PlayerInteractEvent(Player player, Action action, ItemStack itemInHand, Material blockClicked) {
		//	Defensive Line
		{
			//	If we're sneaking and we block with a shield we can attempt to trigger the ability.
			if (player.isSneaking()) {
				ItemStack offHand = player.getInventory().getItemInOffHand();
				ItemStack mainHand = player.getInventory().getItemInMainHand();
				if ((offHand.getType() == Material.SHIELD || mainHand.getType() == Material.SHIELD) && (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)) {
					int defensiveLine = ScoreboardUtils.getScoreboardValue(player, "DefensiveLine");
					if (defensiveLine > 0) {
						if (!mPlugin.mTimers.isAbilityOnCooldown(player.getUniqueId(), Spells.DEFENSIVE_LINE)) {
							List<Entity> entities = player.getNearbyEntities(DEFENSIVE_LINE_RADIUS, DEFENSIVE_LINE_RADIUS, DEFENSIVE_LINE_RADIUS);
							entities.add(player);
							for(int i = 0; i < entities.size(); i++) {
								Entity e = entities.get(i);
								if(e instanceof Player) {
									Player target = (Player)e;
									Location loc = target.getLocation();

									target.playSound(loc, Sound.ITEM_SHIELD_BLOCK, 0.4f, 1.0f);
									boolean self = (target == player);
									mPlugin.mPotionManager.addPotion(target, self ? PotionID.ABILITY_SELF : PotionID.ABILITY_OTHER, new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, DEFENSIVE_LINE_DURATION, 1, true, false));
								}
							}

							ParticleUtils.explodingSphereEffect(mPlugin, player, DEFENSIVE_LINE_RADIUS, Particle.FIREWORKS_SPARK, 1.0f, Particle.CRIT, 1.0f);

							Integer cooldown = defensiveLine == 1 ? DEFENSIVE_LINE_1_COOLDOWN : DEFENSIVE_LINE_2_COOLDOWN;
							mPlugin.mTimers.AddCooldown(player.getUniqueId(), Spells.DEFENSIVE_LINE, cooldown);
						}
					}
				}
			}
		}
	}

	private void _testItemInHand(Player player, ItemStack mainHand) {
		int frenzy = ScoreboardUtils.getScoreboardValue(player, "Frenzy");
		if (frenzy > 0) {
			if (InventoryUtils.isPickaxeItem(mainHand)) {
				mPlugin.mPotionManager.removePotion(player, PotionID.ABILITY_SELF, PotionEffectType.FAST_DIGGING);
				mPlugin.mPotionManager.removePotion(player, PotionID.ABILITY_SELF, PotionEffectType.FAST_DIGGING);
			}
		}

		int weaponMastery = ScoreboardUtils.getScoreboardValue(player, "WeaponMastery");
		if (weaponMastery > 0) {
			mPlugin.mPotionManager.removePotion(player, PotionID.ABILITY_SELF, PotionEffectType.DAMAGE_RESISTANCE);
			mPlugin.mPotionManager.removePotion(player, PotionID.ABILITY_SELF, PotionEffectType.INCREASE_DAMAGE);
			mPlugin.mPotionManager.removePotion(player, PotionID.ABILITY_SELF, PotionEffectType.DAMAGE_RESISTANCE);
			mPlugin.mPotionManager.removePotion(player, PotionID.ABILITY_SELF, PotionEffectType.INCREASE_DAMAGE);

			//	Player has an axe in their mainHands.
			if (InventoryUtils.isAxeItem(mainHand)) {
				int strengthAmp = weaponMastery == 1 ? 0 : 1;
				mPlugin.mPotionManager.addPotion(player, PotionID.ABILITY_SELF, new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 1000000, strengthAmp, true, false));
			}
			//	Player has an sword in their mainHand.
			else if (InventoryUtils.isSwordItem(mainHand)) {
				mPlugin.mPotionManager.addPotion(player, PotionID.ABILITY_SELF, new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 1000000, 0, true, false));
			}
		}
	}

	private void _testToughness(Player player) {
		int toughness = ScoreboardUtils.getScoreboardValue(player, "Toughness");
		if (toughness > 0) {
			int healthBoost = toughness == 1 ? 0 : 1;
			mPlugin.mPotionManager.addPotion(player, PotionID.ABILITY_SELF, new PotionEffect(PotionEffectType.HEALTH_BOOST, 1000000, healthBoost, true, false));
			mPlugin.mPotionManager.addPotion(player, PotionID.ABILITY_SELF, new PotionEffect(PotionEffectType.REGENERATION, 100, 4, true, false));
		}
	}
}
