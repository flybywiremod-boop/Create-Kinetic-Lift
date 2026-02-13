package net.flybywire.createkineticlift.jet;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.flybywire.createkineticlift.registries.KineticBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

/**
 * Front Turbofan Block - Intake side
 *
 * Features:
 * - Accepts fluid input (turpentine fluid, kerosene, diesel, naphtha)
 * - Needs rear part for assembly
 * - When assembled, creates thrust up to 185 Bps
 * - Blade animation when running (angled + blur at high speed)
 */
public class TurbofanFrontBlock extends BaseEntityBlock implements IWrenchable {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty ASSEMBLED = BooleanProperty.create("assembled");
    public static final BooleanProperty RUNNING = BooleanProperty.create("running");

    private static final VoxelShape SHAPE_NORTH = Block.box(1, 1, 0, 15, 15, 16);
    private static final VoxelShape SHAPE_SOUTH = Block.box(1, 1, 0, 15, 15, 16);
    private static final VoxelShape SHAPE_EAST = Block.box(0, 1, 1, 16, 15, 15);
    private static final VoxelShape SHAPE_WEST = Block.box(0, 1, 1, 16, 15, 15);

    public TurbofanFrontBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(ASSEMBLED, false)
                .setValue(RUNNING, false));
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
        return new TurbofanFrontBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        return createTickerHelper(type, KineticBlockEntities.TURBOFAN_FRONT.get(), TurbofanFrontBlockEntity::tick);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide()) {
            BlockEntity entity = level.getBlockEntity(pos);
            if (entity instanceof TurbofanFrontBlockEntity turbofan) {
                NetworkHooks.openScreen((ServerPlayer) player, turbofan, pos);
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
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof TurbofanFrontBlockEntity turbofan) {
                turbofan.drops();
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(ASSEMBLED, false)
                .setValue(RUNNING, false);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ASSEMBLED, RUNNING);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    public boolean hasRearPart(Level level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(FACING);
        BlockPos rearPos = pos.relative(facing.getOpposite());
        BlockState rearState = level.getBlockState(rearPos);

        if (rearState.getBlock() instanceof TurbofanRearBlock) {
            return rearState.getValue(TurbofanRearBlock.FACING) == facing;
        }
        return false;
    }

    public boolean tryAssemble(Level level, BlockPos pos, BlockState state) {
        if (hasRearPart(level, pos, state)) {
            Direction facing = state.getValue(FACING);
            BlockPos rearPos = pos.relative(facing.getOpposite());
            level.setBlock(pos, state.setValue(ASSEMBLED, true), 3);
            level.setBlock(rearPos, level.getBlockState(rearPos).setValue(TurbofanRearBlock.ASSEMBLED, true), 3);
            return true;
        }
        return false;
    }
}
