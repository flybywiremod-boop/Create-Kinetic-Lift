package net.flybywire.createkineticlift.avionics;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface IAvionicsActorProvider {

	@Nullable
	default BlockPos getActorBlockEntityPos(Level level, BlockPos pos) {
		return level.getBlockEntity(pos) instanceof IAvionicsActor ? pos : null;
	}
}
