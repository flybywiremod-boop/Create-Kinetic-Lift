package net.flybywire.createkineticlift.content.controlseat;

import java.util.List;

import net.flybywire.createkineticlift.registries.CKLShapes;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.actors.seat.SeatBlock;
import com.simibubi.create.content.contraptions.actors.seat.SeatEntity;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.utility.BlockHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import net.neoforged.neoforge.common.util.FakePlayer;

public class ControlSeatBlock extends SeatBlock implements IWrenchable {

	public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
	public static final BooleanProperty INVERTED = BooleanProperty.create("inverted");

	public ControlSeatBlock(Properties properties) {
		super(properties, DyeColor.BLUE);
		registerDefaultState(defaultBlockState()
			.setValue(FACING, Direction.NORTH)
			.setValue(INVERTED, false)
			.setValue(WATERLOGGED, false));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder
			.add(FACING)
			.add(INVERTED);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return withWater(super.getStateForPlacement(context), context)
			.setValue(FACING, context.getHorizontalDirection().getOpposite())
			.setValue(INVERTED, false);
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror) {
		return state.rotate(mirror.getRotation(state.getValue(FACING)));
	}

	@Override
	public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
		entity.causeFallDamage(fallDistance, 1.0F, entity.damageSources().fall());
	}

	@Override
	public void updateEntityAfterFallOn(BlockGetter level, Entity entity) {
		entity.setDeltaMovement(entity.getDeltaMovement().multiply((double) 1.0F, (double) 0.0F, (double) 1.0F));
		entity.setDeltaMovement(entity.getDeltaMovement().multiply((double) 1.0F, (double) 0.0F, (double) 1.0F));
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		if (state.getValue(INVERTED)) {
			return CKLShapes.INVERTED_CONTROL_SEAT.get(state.getValue(FACING));
		}
		return CKLShapes.CONTROL_SEAT.get(state.getValue(FACING));
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		if (state.getValue(INVERTED)) {
			return CKLShapes.INVERTED_CONTROL_SEAT.get(state.getValue(FACING));
		}
		return CKLShapes.CONTROL_SEAT.get(state.getValue(FACING));
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
		if (player.isShiftKeyDown() || player instanceof FakePlayer)
			return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		if (stack.is(AllItems.WRENCH.asItem()))
			return net.minecraft.world.ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

		DyeColor color = DyeColor.getColor(stack);
		if (color != null && color != this.color) {
			if (level.isClientSide)
				return ItemInteractionResult.SUCCESS;
			BlockState newState = BlockHelper.copyProperties(state, AllBlocks.SEATS.get(color)
				.getDefaultState());
			level.setBlockAndUpdate(pos, newState);
			return ItemInteractionResult.SUCCESS;
		}

		List<SeatEntity> seats = level.getEntitiesOfClass(SeatEntity.class, new AABB(pos));
		if (!seats.isEmpty()) {
			SeatEntity seatEntity = seats.get(0);
			List<Entity> passengers = seatEntity.getPassengers();
			if (!passengers.isEmpty() && passengers.get(0) instanceof Player)
				return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
			if (!level.isClientSide) {
				seatEntity.ejectPassengers();
				player.startRiding(seatEntity);
			}
			return ItemInteractionResult.SUCCESS;
		}

		if (level.isClientSide)
			return ItemInteractionResult.SUCCESS;
		sitDown(state, level, pos, getLeashed(level, player).or(player));
		return ItemInteractionResult.SUCCESS;
	}

	public static void sitDown(BlockState state, Level level, BlockPos pos, Entity entity) {
		if (level.isClientSide)
			return;
		Boolean inverted = state.getValue(INVERTED);
		Direction direction = state.getValue(FACING);
		Direction lateral = inverted ? direction.getCounterClockWise() : direction.getClockWise();

		Vec3 entityPos = pos.getCenter()
			.relative(lateral, 0.28125)
			.relative(Direction.DOWN, 0.1875);

		SeatEntity seat = new SeatEntity(level);
		seat.setPos(entityPos.x, entityPos.y, entityPos.z);
		level.addFreshEntity(seat);
		entity.startRiding(seat, true);
		if (entity instanceof TamableAnimal ta)
			ta.setInSittingPose(true);
	}

	public InteractionResult onWrenched(BlockState state, UseOnContext context) {
		Level level = context.getLevel();
		if (level.isClientSide)
			return InteractionResult.SUCCESS;
		BlockPos pos = context.getClickedPos();
		level.setBlock(pos, state.cycle(INVERTED), Block.UPDATE_ALL);
		AllSoundEvents.WRENCH_ROTATE.playOnServer(level, pos, 1, Create.RANDOM.nextFloat() + .5f);

		List<SeatEntity> seats = level.getEntitiesOfClass(SeatEntity.class, new AABB(pos));
		if (!seats.isEmpty()) {
			SeatEntity seatEntity = seats.get(0);
			seatEntity.discard();
		}
		return InteractionResult.SUCCESS;
	}
}
