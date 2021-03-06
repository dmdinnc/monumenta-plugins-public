package com.playmonumenta.plugins.events;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.potion.PotionEffect;

public class PotionEffectApplyEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();

	private final Entity mApplier;
	private final LivingEntity mApplied;
	private PotionEffect mEffect;
	private boolean mIsCancelled;

	public PotionEffectApplyEvent(Entity applier, LivingEntity applied, PotionEffect effect) {
		mApplier = applier;
		mApplied = applied;
		mEffect = effect;
	}

	public PotionEffect getEffect() {
		return mEffect;
	}

	public LivingEntity getApplied() {
		return mApplied;
	}

	public Entity getApplier() {
		return mApplier;
	}

	public void setEffect(PotionEffect effect) {
		mEffect = effect;
	}

	@Override
	public boolean isCancelled() {
		return mIsCancelled;
	}

	@Override
	public void setCancelled(boolean arg0) {
		this.mIsCancelled = arg0;
	}

	// Mandatory Event Methods (If you remove these, I'm 99% sure the event will break)

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}
