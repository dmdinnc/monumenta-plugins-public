package com.playmonumenta.plugins.utils;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.destroystokyo.paper.MaterialSetTag;
import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.classes.Spells;
import com.playmonumenta.plugins.events.AbilityCastEvent;
import com.playmonumenta.plugins.potion.PotionManager.PotionID;

public class PlayerUtils {

	public static boolean isFullyCooled(Player player) {
		return player.getCooledAttackStrength(0) == 1;
	}

	public static boolean isCritical(Player player) {
		return isFullyCooled(player) &&
			   player.getFallDistance() > 0.0F &&
		       !player.isOnGround() &&
		       !player.isInsideVehicle() &&
		       !player.hasPotionEffect(PotionEffectType.BLINDNESS) &&
		       player.getLocation().getBlock().getType() != Material.LADDER &&
		       player.getLocation().getBlock().getType() != Material.VINE;
	}

	public static void callAbilityCastEvent(Player player, Spells spell) {
		AbilityCastEvent event = new AbilityCastEvent(player, spell);
		Bukkit.getPluginManager().callEvent(event);
	}

	public static void awardStrike(Plugin plugin, Player player, String reason) {
		int strikes = ScoreboardUtils.getScoreboardValue(player, "Strikes");
		strikes++;
		ScoreboardUtils.setScoreboardValue(player, "Strikes", strikes);

		Location loc = player.getLocation();
		String oobLoc = "[" + (int)loc.getX() + ", " + (int)loc.getY() + ", " + (int)loc.getZ() + "]";

		player.sendMessage(ChatColor.RED + "WARNING: " + reason);
		player.sendMessage(ChatColor.RED + "Location: " + oobLoc);
		player.sendMessage(ChatColor.YELLOW + "This is an automated message generated by breaking a game rule.");
		player.sendMessage(ChatColor.YELLOW + "You have been teleported to spawn and given slowness for 5 minutes.");
		player.sendMessage(ChatColor.YELLOW + "There is no further punishment, but please do follow the rules.");

		plugin.mPotionManager.addPotion(player, PotionID.APPLIED_POTION,
		                                new PotionEffect(PotionEffectType.SLOW, 5 * 20 * 60, 3, false, true));

		player.teleport(player.getWorld().getSpawnLocation());
	}

	public static List<Player> playersInRange(Player player, double radius, boolean includeSourcePlayer) {
		return playersInRange(player, radius, includeSourcePlayer, false);
	}

	public static List<Player> playersInRange(Player player, double radius, boolean includeSourcePlayer, boolean includeNonTargetable) {
		List<Player> players = playersInRange(player.getLocation(), radius, includeNonTargetable);
		if (!includeSourcePlayer) {
			players.removeIf(p -> (p == player));
		} else {
			if (!players.contains(player)) {
				players.add(player);
			}
		}
		return players;
	}

	public static List<Player> playersInRange(Location loc, double range) {
		return playersInRange(loc, range, false);
	}

	public static List<Player> playersInRange(Location loc, double range, boolean includeNonTargetable) {
		List<Player> players = new ArrayList<Player>();

		for (Player player : Bukkit.getServer().getOnlinePlayers()) {
			if (player.getLocation().distance(loc) < range && player.getGameMode() != GameMode.SPECTATOR && player.getHealth() > 0) {
				if (includeNonTargetable || !AbilityUtils.isStealthed(player)) {
					players.add(player);
				}
			}
		}

		return players;
	}

	public static void healPlayer(Player player, double healAmount) {
		if (!player.isDead()) {
			EntityRegainHealthEvent event = new EntityRegainHealthEvent(player, healAmount, EntityRegainHealthEvent.RegainReason.CUSTOM);
			Bukkit.getPluginManager().callEvent(event);
			if (!event.isCancelled()) {
				double newHealth = Math.min(player.getHealth() + event.getAmount(), player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
				player.setHealth(newHealth);
			}
		}
	}

	public static Location getRightSide(Location location, double distance) {
		float angle = location.getYaw() / 60;
		return location.clone().subtract(new Vector(FastUtils.cos(angle), 0, FastUtils.sin(angle)).normalize().multiply(distance));
	}

	/* Command should use @s for targeting selector */
	private static String getExecuteCommandOnNearbyPlayers(Location loc, int radius, String command) {
		String executeCmd = "execute as @a[x=" + (int)loc.getX() +
		                    ",y=" + (int)loc.getY() +
		                    ",z=" + (int)loc.getZ() +
		                    ",distance=.." + radius + "] at @s run ";
		return executeCmd + command;
	}

	public static void executeCommandOnNearbyPlayers(Location loc, int radius, String command) {
		Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),
		                                   getExecuteCommandOnNearbyPlayers(loc, radius, command));
	}

	// If player is considered to be in the air
	public static boolean notOnGround(Player player) {
		Material playerFeetMaterial = player.getLocation().getBlock().getType();
		// Accounts for all climbables including 1.16 vines & scaffolding
		boolean playerInClimbable = MaterialSetTag.CLIMBABLE.isTagged(playerFeetMaterial);

		// Use Entity#isOnGround() value calculated by the server
		// instead of deprecated Player#isOnGround() where value is controlled by the client.
		boolean playerOnGround = ((Entity)player).isOnGround();

		return (
			!playerInClimbable
			&& !playerOnGround
		);
	}
}
