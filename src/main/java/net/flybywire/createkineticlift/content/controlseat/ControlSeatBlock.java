package net.flybywire.createkineticlift.content.controlseat;

import com.simibubi.create.AllItems;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.contraptions.actors.seat.SeatBlock;
import com.simibubi.create.content.contraptions.actors.seat.SeatEntity;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.flybywire.createkineticlift.registries.KineticEntities;
import net.flybywire.createkineticlift.registries.KineticShapes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.util.FakePlayer;

import java.util.List;

public class ControlSeatBlock extends SeatBlock implements IWrenchable {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty INVERTED = BooleanProperty.create("inverted");

    public ControlSeatBlock(Properties pProperties) {
        super(pProperties, DyeColor.BLUE);
        registerDefaultState(defaultBlockState()
                .setValue(FACING, Direction.NORTH)
                .setValue(INVERTED, false)
                .setValue(WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder
                .add(FACING)
                .add(INVERTED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(INVERTED, false);
    }


    // Removes waterlogging logic for now because it's weird
    @Override
    public FluidState getFluidState(BlockState state) {
        return net.minecraft.world.level.material.Fluids.EMPTY.defaultFluidState();
    }
    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level,
                                  BlockPos currentPos, BlockPos neighborPos) {
        return state;
    }

    // Removes inherited bouncing
    @Override
    public void fallOn(Level pLevel, BlockState pState, BlockPos pPos, Entity pEntity, float pFallDistance) {
        pEntity.causeFallDamage(pFallDistance, 1.0F, pEntity.damageSources().fall());
    }
    @Override
    public void updateEntityAfterFallOn(BlockGetter pLevel, Entity pEntity) {
        pEntity.setDeltaMovement(pEntity.getDeltaMovement().multiply(1.0D, 0.0D, 1.0D));
        pEntity.setDeltaMovement(pEntity.getDeltaMovement().multiply(1.0D, 0.0D, 1.0D));
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    public static void sitDown(Level world, BlockPos pos, Entity entity, BlockState state) {
        if (world.isClientSide)
            return;

        ControlSeatEntity seat = KineticEntities.CONTROL_SEAT.get().create(world);
        if (seat == null) return;

        Direction direction = state.getValue(FACING);
        boolean inverted = state.getValue(INVERTED);

        double px = 1.0 / 16.0;

        double up = 1 * px;
        double back = 2 * px;
        double left = -5 * px;

        Vec3 entityPos = pos.getCenter().add(0, -0.25 + up, 0);

        Direction finalDir = inverted ? direction.getOpposite() : direction;

        entityPos = switch (finalDir) {
            case NORTH -> entityPos.add(-left, 0, back);
            case SOUTH -> entityPos.add(left, 0, -back);
            case EAST  -> entityPos.add(-back, 0, -left);
            case WEST  -> entityPos.add(back, 0, left);
            default    -> entityPos;
        };

        seat.setPos(entityPos.x, entityPos.y, entityPos.z);
        world.addFreshEntity(seat);
        entity.startRiding(seat, true);

        if (entity instanceof TamableAnimal ta)
            ta.setInSittingPose(true);
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult p_225533_6_) {
        if (player.isShiftKeyDown() || player instanceof FakePlayer)
            return InteractionResult.PASS;

        if (!world.isClientSide) {
            if (player.getItemInHand(hand).is(AllItems.WRENCH.asItem())) {
                world.setBlock(pos, state.cycle(INVERTED), 3);
                AllSoundEvents.WRENCH_ROTATE.playOnServer(world, pos, 1.0F, world.random.nextFloat() + 0.5F);
                return InteractionResult.SUCCESS;
            }
        }

        List<SeatEntity> seats = world.getEntitiesOfClass(SeatEntity.class, new AABB(pos));
        if (!seats.isEmpty()) {
            SeatEntity seatEntity = seats.get(0);
            List<Entity> passengers = seatEntity.getPassengers();
            if (!passengers.isEmpty() && passengers.get(0) instanceof Player)
                return InteractionResult.PASS;
            if (!world.isClientSide) {
                seatEntity.ejectPassengers();
                player.startRiding(seatEntity);
            }
            return InteractionResult.SUCCESS;
        }

        if (world.isClientSide)
            return InteractionResult.SUCCESS;
        ControlSeatBlock.sitDown(world, pos, getLeashed(world, player).or(player), state);
        return InteractionResult.SUCCESS;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (state.getValue(INVERTED)) {
            return KineticShapes.INVERTED_CONTROL_SEAT.get(state.getValue(FACING));
        }
        return KineticShapes.CONTROL_SEAT.get(state.getValue(FACING));
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (state.getValue(INVERTED)) {
            return KineticShapes.INVERTED_CONTROL_SEAT.get(state.getValue(FACING));
        }
        return KineticShapes.CONTROL_SEAT.get(state.getValue(FACING));
    }
}