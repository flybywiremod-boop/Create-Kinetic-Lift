package net.flybywire.createkineticlift.avionics;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

public interface IAvionicsActor {

	default AABB getActorBounds() {
		if (!(this instanceof BlockEntity blockEntity))
			return new AABB(0, 0, 0, 1, 1, 1);

		Level level = blockEntity.getLevel();

		if (level == null)
			return new AABB(blockEntity.getBlockPos());

		VoxelShape shape = blockEntity.getBlockState().getShape(level, blockEntity.getBlockPos());

		if (shape.isEmpty())
			return new AABB(blockEntity.getBlockPos());

		return shape.bounds().move(blockEntity.getBlockPos());
	}
}
