package net.flybywire.createkineticlift.registries;

import java.util.function.BiFunction;

import net.createmod.catnip.math.VoxelShaper;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;


public class CKLShapes {

	public static final VoxelShaper
		CONTROL_SEAT =
		shape(6, 0, 4, 19, 5, 12)
			.add(6, 5, 0, 11, 13, 16)
			.add(11, 5, 3, 14, 13, 16)
			.add(14, 5, 0, 19, 13, 16)
			.add(0, 0, 0, 6, 15, 16)
			.forHorizontal(Direction.NORTH),

	INVERTED_CONTROL_SEAT =
		shape(-3, 0, 4, 10, 5, 12)
			.add(2, 5, 0, 10, 13, 16)
			.add(2, 5, 0, 5, 13, 13)
			.add(-3, 5, 0, 2, 13, 16)
			.add(10, 0, 0, 16, 15, 16)
			.forHorizontal(Direction.NORTH),

	TURBOFAN_STRUCTURAL_TOP_LEFT =
		shape(7, 7, 0, 13, 13, 16)
			.add(0, 0, 0, 7, 15, 16)
			.add(7, 0, 0, 15, 7, 16)
			.forHorizontal(Direction.NORTH),

	TURBOFAN_STRUCTURAL_TOP_RIGHT =
		shape(3, 7, 0, 9, 13, 16)
			.add(9, 0, 0, 16, 15, 16)
			.add(1, 0, 0, 9, 7, 16)
			.forHorizontal(Direction.NORTH),

	TURBOFAN_STRUCTURAL_BOTTOM_LEFT =
		shape(7, 3, 0, 13, 9, 16)
			.add(0, 1, 0, 7, 16, 16)
			.add(7, 9, 0, 15, 16, 16)
			.forHorizontal(Direction.NORTH),

	TURBOFAN_STRUCTURAL_BOTTOM_RIGHT =
		shape(3, 3, 0, 9, 9, 16)
			.add(9, 1, 0, 16, 16, 16)
			.add(1, 9, 0, 9, 16, 16)
			.forHorizontal(Direction.NORTH),

	TURBOFAN_STRUCTURAL_TOP_LEFT_CUT =
		shape(7, 7, 8, 13, 13, 16)
			.add(0, 0, 8, 7, 15, 16)
			.add(7, 0, 8, 15, 7, 16)
			.forHorizontal(Direction.NORTH),

	TURBOFAN_STRUCTURAL_TOP_RIGHT_CUT =
		shape(3, 7, 8, 9, 13, 16)
			.add(9, 0, 8, 16, 15, 16)
			.add(1, 0, 8, 9, 7, 16)
			.forHorizontal(Direction.NORTH),

	TURBOFAN_STRUCTURAL_BOTTOM_LEFT_CUT =
		shape(7, 3, 8, 13, 9, 16)
			.add(0, 1, 8, 7, 16, 16)
			.add(7, 9, 8, 15, 16, 16)
			.forHorizontal(Direction.NORTH),

	TURBOFAN_STRUCTURAL_BOTTOM_RIGHT_CUT =
		shape(3, 3, 8, 9, 9, 16)
			.add(9, 1, 8, 16, 16, 16)
			.add(1, 9, 8, 9, 16, 16)
			.forHorizontal(Direction.NORTH),

	TURBOFAN_STRUCTURAL_SIDE_CUT =
		shape(0, 0, 8, 16, 16, 16)
			.forHorizontal(Direction.NORTH);

	private static Builder shape(final VoxelShape shape) {
		return new Builder(shape);
	}

	private static Builder shape(final double x1, final double y1, final double z1, final double x2, final double y2, final double z2) {
		return shape(cuboid(x1, y1, z1, x2, y2, z2));
	}

	private static VoxelShape cuboid(final double x1, final double y1, final double z1, final double x2, final double y2, final double z2) {
		return Block.box(x1, y1, z1, x2, y2, z2);
	}

	public static class Builder {
		private VoxelShape shape;

		public Builder(final VoxelShape shape) {
			this.shape = shape;
		}

		public Builder add(final VoxelShape shape) {
			this.shape = Shapes.or(this.shape, shape);
			return this;
		}

		public Builder add(final double x1, final double y1, final double z1, final double x2, final double y2, final double z2) {
			return this.add(cuboid(x1, y1, z1, x2, y2, z2));
		}

		public Builder erase(final double x1, final double y1, final double z1, final double x2, final double y2, final double z2) {
			this.shape = Shapes.join(this.shape, cuboid(x1, y1, z1, x2, y2, z2), BooleanOp.ONLY_FIRST);
			return this;
		}

		public VoxelShape build() {
			return this.shape;
		}

		public VoxelShaper build(final BiFunction<VoxelShape, Direction, VoxelShaper> factory, final Direction direction) {
			return factory.apply(this.shape, direction);
		}

		public VoxelShaper build(final BiFunction<VoxelShape, Direction.Axis, VoxelShaper> factory, final Direction.Axis axis) {
			return factory.apply(this.shape, axis);
		}

		public VoxelShaper forAxis() {
			return this.build(VoxelShaper::forAxis, Direction.Axis.Y);
		}

		public VoxelShaper forHorizontalAxis() {
			return this.build(VoxelShaper::forHorizontalAxis, Direction.Axis.Z);
		}

		public VoxelShaper forHorizontal(final Direction direction) {
			return this.build(VoxelShaper::forHorizontal, direction);
		}

		public VoxelShaper forDirectional(final Direction direction) {
			return this.build(VoxelShaper::forDirectional, direction);
		}

		public VoxelShaper forDirectional() {
			return this.forDirectional(Direction.UP);
		}
	}
}
