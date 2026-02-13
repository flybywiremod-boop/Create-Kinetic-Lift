package net.flybywire.createkineticlift.jet;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.flybywire.createkineticlift.registries.KineticBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Rear Turbofan Block - Exhaust/Thrust side
 *
 * Features:
 * - Reverse thrust (spoiler) capability when R key pressed
 * - Needs front part for assembly
 * - When running, emits exhaust particles
 * - Thrust reverser animation
 */
public class TurbofanRearBlock extends BaseEntityBlock implements IWrenchable {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty ASSEMBLED = BooleanProperty.create("assembled");
    public static final BooleanProperty REVERSE_THRUST = BooleanProperty.create("reverse_thrust");

    private static final VoxelShape SHAPE_NORTH = Block.box(0, 0, 0, 16, 16, 14);
    private static final VoxelShape SHAPE_SOUTH = Block.box(0, 0, 2, 16, 16, 16);
    private static final VoxelShape SHAPE_EAST = Block.box(2, 0, 0, 16, 16, 16);
    private static final VoxelShape SHAPE_WEST = Block.box(0, 0, 0, 14, 16, 16);

    public TurbofanRearBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(ASSEMBLED, false)
                .setValue(REVERSE_THRUST, false));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case NORTH -> SHAPE_NORTH;
            case SOUTH -> SHAPE_SOUTH;
            case EAST -> SHAPE_EAST;
            case WEST -> SHAPE_WEST;
            default -> Shapes.block();
        };
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TurbofanRearBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        return createTickerHelper(type, KineticBlockEntities.TURBOFAN_REAR.get(), TurbofanRearBlockEntity::tick);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide()) {
            BlockEntity entity = level.getBlockEntity(pos);
            if (entity instanceof TurbofanRearBlockEntity) {
                boolean currentReverse = state.getValue(REVERSE_THRUST);
                level.setBlock(pos, state.setValue(REVERSE_THRUST, !currentReverse), 3);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState rotated = state.setValue(FACING, state.getValue(FACING).getClockWise());
        level.setBlock(pos, rotated, 3);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            Direction facing = state.getValue(FACING);
            BlockPos frontPos = pos.relative(facing);
            BlockState frontState = level.getBlockState(frontPos);
            if (frontState.getBlock() instanceof TurbofanFrontBlock && frontState.getValue(TurbofanFrontBlock.ASSEMBLED)) {
                level.setBlock(frontPos, frontState.setValue(TurbofanFrontBlock.ASSEMBLED, false), 3);
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(ASSEMBLED, false)
                .setValue(REVERSE_THRUST, false);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ASSEMBLED, REVERSE_THRUST);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    public boolean hasFrontPart(Level level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(FACING);
        BlockPos frontPos = pos.relative(facing);
        BlockState frontState = level.getBlockState(frontPos);

        if (frontState.getBlock() instanceof TurbofanFrontBlock) {
            return frontState.getValue(TurbofanFrontBlock.FACING) == facing;
        }
        return false;
    }

    public void setReverseThrust(Level level, BlockPos pos, BlockState state, boolean reverse) {
        if (state.getValue(ASSEMBLED)) {
            level.setBlock(pos, state.setValue(REVERSE_THRUST, reverse), 3);
        }
    }
}
