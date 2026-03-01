package net.flybywire.createkineticlift.registries;

import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.createmod.catnip.math.VoxelShaper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.BiFunction;

import static net.minecraft.core.Direction.UP;

public class KineticShapes {

    public static final VoxelShaper
            CONTROL_SEAT = shape(6, 0, 4, 19, 5, 12)
                    .add(6, 5, 0, 19, 13, 16)
                    .add(0, 0, 0, 6, 16, 16)
                    .forHorizontal(Direction.NORTH),

            INVERTED_CONTROL_SEAT = shape(-3, 0, 4, 10, 5, 12)
                    .add(-3, 5, 0, 10, 13, 16)
                    .add(10, 0, 0, 16, 16, 16)
                    .forHorizontal(Direction.NORTH),


    // for testing purposes until dummy blocks work

//              TURBOFAN_INTAKE = shape(-1, -16, -16, 17, 32, 32)
//                    .add(17, 17, -16, 31, 31, 32)
//                    .add(-15, 17, -16, -1, 31, 32)
//                    .add(-15, -15, -16, -1, -1, 32)
//                    .add(17, -15, -16, 31, -1, 32)
//                    .add(-16, -1, -16, 32, 17, 32)
//                    .forHorizontal(Direction.NORTH);

            TURBOFAN_INTAKE = shape(0, 0, 0, 16, 16, 16)
                    .forHorizontal(Direction.NORTH);

    private static Builder shape(VoxelShape shape) {
        return new Builder(shape);
    }

    private static Builder shape(double x1, double y1, double z1, double x2, double y2, double z2) {
        return shape(cuboid(x1, y1, z1, x2, y2, z2));
    }

    private static VoxelShape cuboid(double x1, double y1, double z1, double x2, double y2, double z2) {
        return Block.box(x1, y1, z1, x2, y2, z2);
    }

    public static class Builder {

        private VoxelShape shape;

        public Builder(VoxelShape shape) {
            this.shape = shape;
        }

        public Builder add(VoxelShape shape) {
            this.shape = Shapes.or(this.shape, shape);
            return this;
        }

        public Builder add(double x1, double y1, double z1, double x2, double y2, double z2) {
            return add(cuboid(x1, y1, z1, x2, y2, z2));
        }

        public Builder erase(double x1, double y1, double z1, double x2, double y2, double z2) {
            this.shape = Shapes.join(shape, cuboid(x1, y1, z1, x2, y2, z2), BooleanOp.ONLY_FIRST);
            return this;
        }

        public VoxelShape build() {
            return shape;
        }

        public VoxelShaper build(BiFunction<VoxelShape, Direction, VoxelShaper> factory, Direction direction) {
            return factory.apply(shape, direction);
        }

        public VoxelShaper build(BiFunction<VoxelShape, Axis, VoxelShaper> factory, Axis axis) {
            return factory.apply(shape, axis);
        }

        public VoxelShaper forDirectional(Direction direction) {
            return build(VoxelShaper::forDirectional, direction);
        }

        public VoxelShaper forAxis() {
            return build(VoxelShaper::forAxis, Axis.Y);
        }

        public VoxelShaper forHorizontalAxis() {
            return build(VoxelShaper::forHorizontalAxis, Axis.Z);
        }

        public VoxelShaper forHorizontal(Direction direction) {
            return build(VoxelShaper::forHorizontal, direction);
        }

        public VoxelShaper forDirectional() {
            return forDirectional(UP);
        }
    }
}
