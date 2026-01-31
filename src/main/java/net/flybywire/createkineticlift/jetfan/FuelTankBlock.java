package net.flybywire.createkineticlift.jetfan;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.flybywire.createkineticlift.block.entity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.fluids.FluidUtil;
import org.jetbrains.annotations.Nullable;

/**
 * Fuel Tank Block - Hexagonal stackable fuel storage
 *
 * Features:
 * - 2,500,000 mB capacity per block
 * - Stackable vertically (connects to tanks above/below)
 * - Required for turbofan assembly
 * - Hexagonal shape for aircraft aesthetics
 */
public class FuelTankBlock extends BaseEntityBlock implements IWrenchable {

    // Connection properties for stacking
    public static final BooleanProperty TOP = BooleanProperty.create("top");
    public static final BooleanProperty BOTTOM = BooleanProperty.create("bottom");

    // Hexagonal shape (approximated with voxels)
    private static final VoxelShape SHAPE_SINGLE = Shapes.or(
            // Main body (slightly inset from edges for hexagonal look)
            Block.box(2, 0, 2, 14, 16, 14),
            // Corner fills for rounder appearance
            Block.box(1, 0, 4, 2, 16, 12),
            Block.box(14, 0, 4, 15, 16, 12),
            Block.box(4, 0, 1, 12, 16, 2),
            Block.box(4, 0, 14, 12, 16, 15)
    );

    public FuelTankBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(TOP, true)
                .setValue(BOTTOM, true));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE_SINGLE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FuelTankBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        return createTickerHelper(type, ModBlockEntities.FUEL_TANK.get(), FuelTankBlockEntity::tick);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        // Allow fluid container interaction (buckets, etc.)
        if (FluidUtil.interactWithFluidHandler(player, hand, level, pos, hit.getDirection())) {
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        // Wrench can be used to disconnect tanks or show info
        return InteractionResult.PASS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof FuelTankBlockEntity tank) {
                tank.onRemoved();
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();

        boolean hasTop = isTankAt(level, pos.above());
        boolean hasBottom = isTankAt(level, pos.below());

        return this.defaultBlockState()
                .setValue(TOP, !hasTop)
                .setValue(BOTTOM, !hasBottom);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                   LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (direction == Direction.UP) {
            return state.setValue(TOP, !isTankAt(level, pos.above()));
        }
        if (direction == Direction.DOWN) {
            return state.setValue(BOTTOM, !isTankAt(level, pos.below()));
        }
        return state;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TOP, BOTTOM);
    }

    /**
     * Check if there's a fuel tank at the given position
     */
    private boolean isTankAt(LevelAccessor level, BlockPos pos) {
        return level.getBlockState(pos).getBlock() instanceof FuelTankBlock;
    }

    /**
     * Get the total capacity of a tank stack starting from this position
     */
    public static long getStackCapacity(Level level, BlockPos pos) {
        int count = countStackedTanks(level, pos);
        return (long) count * FuelTankBlockEntity.TANK_CAPACITY;
    }

    /**
     * Count how many tanks are stacked together
     */
    public static int countStackedTanks(Level level, BlockPos pos) {
        int count = 1;

        // Count upwards
        BlockPos checkPos = pos.above();
        while (level.getBlockState(checkPos).getBlock() instanceof FuelTankBlock) {
            count++;
            checkPos = checkPos.above();
        }

        // Count downwards
        checkPos = pos.below();
        while (level.getBlockState(checkPos).getBlock() instanceof FuelTankBlock) {
            count++;
            checkPos = checkPos.below();
        }

        return count;
    }

    /**
     * Get the controller (bottom) tank of a stack
     */
    public static BlockPos getControllerPos(Level level, BlockPos pos) {
        BlockPos controllerPos = pos;
        while (level.getBlockState(controllerPos.below()).getBlock() instanceof FuelTankBlock) {
            controllerPos = controllerPos.below();
        }
        return controllerPos;
    }
}
