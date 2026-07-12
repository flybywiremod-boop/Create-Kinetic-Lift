package net.flybywire.createkineticlift.avionics;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public final class AvionicsHelper {

	private AvionicsHelper() {
	}

	@Nullable
	public static BlockPos getActorBlockEntityPos(Level level, BlockPos pos) {
		BlockState state = level.getBlockState(pos);

		if (!(state.getBlock() instanceof IAvionicsActorProvider provider))
			return null;

		return provider.getActorBlockEntityPos(level, pos);
	}

	@Nullable
	public static BlockPos getSourceBlockEntityPos(Level level, BlockPos pos) {
		BlockPos actorPos = getActorBlockEntityPos(level, pos);

		if (actorPos == null)
			return null;

		return level.getBlockEntity(actorPos) instanceof IAvionicsSource ? actorPos : null;
	}

	@Nullable
	public static BlockPos getPeripheralBlockEntityPos(Level level, BlockPos pos) {
		BlockPos actorPos = getActorBlockEntityPos(level, pos);

		if (actorPos == null)
			return null;

		return level.getBlockEntity(actorPos) instanceof IAvionicsPeripheral ? actorPos : null;
	}
}
