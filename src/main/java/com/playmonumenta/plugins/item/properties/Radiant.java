package com.playmonumenta.plugins.item.properties;

import com.playmonumenta.plugins.item.properties.ItemPropertyManager.ItemSlot;
import com.playmonumenta.plugins.managers.potion.PotionManager.PotionID;
import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.utils.InventoryUtils;

import java.util.EnumSet;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.World;

public class Radiant implements ItemProperty {
	private static String PROPERTY_NAME = ChatColor.GRAY + "Radiant";

	@Override
	public String getProperty() {
		return PROPERTY_NAME;
	}

	@Override
	public EnumSet<ItemSlot> validSlots() {
		return EnumSet.of(ItemSlot.MAINHAND, ItemSlot.OFFHAND);
	}

	@Override
	public int getLevelFromItem(ItemStack item, Player player) {
		if (InventoryUtils.isSoulboundToPlayer(item, player)) {
			return InventoryUtils.getCustomEnchantLevel(item, getProperty());
		}
		return 0;
	}

	@Override
	public void applyProperty(Plugin plugin, Player player, int level) {
		// Radiant is different from the others - it applies effects only for a short duration
		// and doesn't remove them when you switch off
		plugin.mPotionManager.addPotion(player, PotionID.ITEM, new PotionEffect(PotionEffectType.GLOWING, 600, 0, true, false));
		plugin.mPotionManager.addPotion(player, PotionID.ITEM, new PotionEffect(PotionEffectType.NIGHT_VISION, 600, 0, true, false));
	}

	@Override
	public void tick(Plugin plugin, World world, Player player, int level) {
		applyProperty(plugin, player, level);
	}
}