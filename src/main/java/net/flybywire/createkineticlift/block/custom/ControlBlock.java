package net.flybywire.createkineticlift.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class ControlBlock extends Block {
    public ControlBlock(Properties pProperties) {
        super(pProperties);
    }

    public static final DirectionProperty FACING;

    public static final BooleanProperty INVERTED = BooleanProperty.create("inverted");
    public BlockState rotate(BlockState pState, Rotation pRot) {
        return (BlockState)pState.setValue(FACING, pRot.rotate((Direction)pState.getValue(FACING)));
    }


    private static final VoxelShape EAST = Block.box(0,0,0,19,13,22);
    private static final VoxelShape WEST = Block.box(-3,0,0,16,13,19);
    private static final VoxelShape SOUTH = Block.box(-3,0,-6,16,13,16);
    private static final VoxelShape NORTH = Block.box(0,0,-3,19, 13,19);

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        Direction direction = pState.getValue(FACING);
        return switch (direction) {
            case EAST -> EAST;
            case WEST -> WEST;
            case SOUTH -> SOUTH;
            case NORTH -> NORTH;
            default -> Block.box(0, 0, 0, 1, 1, 1);
        };
    }

    public BlockState mirror(BlockState pState, Mirror pMirror) {
        return pState.rotate(pMirror.getRotation((Direction)pState.getValue(FACING)));
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection().getOpposite()).setValue(INVERTED, false);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING);
        pBuilder.add(INVERTED);
    }

    static {
        FACING = BlockStateProperties.HORIZONTAL_FACING;
    }
}