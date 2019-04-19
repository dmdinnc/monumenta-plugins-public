package com.playmonumenta.plugins.abilities.alchemist.apothecary;

import java.util.Random;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;

import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.abilities.Ability;
import com.playmonumenta.plugins.abilities.AbilityManager;
import com.playmonumenta.plugins.utils.PlayerUtils;

public class BezoarNonApothecary extends Ability {

	// Increments nearby Apothecary bezoar counters

	public BezoarNonApothecary(Plugin plugin, World world, Random random, Player player) {
		super(plugin, world, random, player);
	}

	@Override
	public boolean canUse(Player player) {
		return true;
	}

	@Override
	public void EntityDeathEvent(EntityDeathEvent event, boolean shouldGenDrops) {
		for (Player player : PlayerUtils.getNearbyPlayers(mPlayer, 12, false)) {
			Beozar bz = (Beozar) AbilityManager.getManager().getPlayerAbility(player, Beozar.class);
			if (bz != null) {
				bz.incrementKills();
			}
		}
	}
}