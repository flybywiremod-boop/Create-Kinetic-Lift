package net.flybywire.createkineticlift.avionics;

import net.minecraft.world.entity.player.Player;

public interface IAvionicsSource extends IAvionicsActor {

	default boolean isControlledBy(Player player) {
		return false;
	}
}
