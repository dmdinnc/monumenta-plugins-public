package com.playmonumenta.plugins.abilities.rogue;

import java.util.Random;

import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.abilities.Ability;
import com.playmonumenta.plugins.classes.magic.CustomDamageEvent;
import com.playmonumenta.plugins.utils.EntityUtils;
import com.playmonumenta.plugins.utils.InventoryUtils;
import com.playmonumenta.plugins.utils.ScoreboardUtils;

public class RoguePassive extends Ability {

	private static final double PASSIVE_DAMAGE_ELITE_MODIFIER = 2.0;

	public RoguePassive(Plugin plugin, World world, Random random, Player player) {
		super(plugin, world, random, player);
	}

	@Override
	public boolean canUse(Player player) {
		return ScoreboardUtils.getScoreboardValue(player, "Class") == 4;
	}

	@Override
	public void PlayerDealtCustomDamageEvent(CustomDamageEvent event) {
		if (event.getDamager() instanceof Player) {
			Entity damagee = event.getDamaged();

			ItemStack mainHand = mPlayer.getInventory().getItemInMainHand();
			ItemStack offHand = mPlayer.getInventory().getItemInOffHand();
			if (InventoryUtils.isSwordItem(mainHand) && InventoryUtils.isSwordItem(offHand)) {
				//  This test if the damagee is an instance of a Elite.
				if (damagee instanceof LivingEntity && EntityUtils.isElite(event.getDamaged())) {
					event.setDamage(event.getDamage() * PASSIVE_DAMAGE_ELITE_MODIFIER);
				}
			}
		}
	}

//	@Override
//	public boolean LivingEntityDamagedByPlayerEvent(EntityDamageByEntityEvent event) {
//		//  Make sure only players trigger this.
//		if (event.getDamager() instanceof Player) {
//			Entity damagee = event.getEntity();
//
//			ItemStack mainHand = mPlayer.getInventory().getItemInMainHand();
//			ItemStack offHand = mPlayer.getInventory().getItemInOffHand();
//			if (InventoryUtils.isSwordItem(mainHand) && InventoryUtils.isSwordItem(offHand)) {
//				//  This test if the damagee is an instance of a Elite.
//				if (damagee instanceof LivingEntity && EntityUtils.isElite(event.getEntity())) {
//					event.setDamage(event.getDamage() * PASSIVE_DAMAGE_ELITE_MODIFIER);
//				}
//			}
//		}
//		return true;
//	}
}
