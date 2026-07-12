package net.flybywire.createkineticlift.content.turbofan;

import static net.flybywire.createkineticlift.content.turbofan.TurbofanBlockEntity.MAX_BLADES;
import static net.flybywire.createkineticlift.content.turbofan.TurbofanBlockEntity.REQUIRED_ASSEMBLY_STAGE;

import java.util.HashSet;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import net.flybywire.createkineticlift.avionics.IAvionicsActor;
import net.flybywire.createkineticlift.avionics.IAvionicsActorProvider;
import net.flybywire.createkineticlift.content.turbofan.blades.TurbofanBladeItem;
import net.flybywire.createkineticlift.registries.CKLBlocks;

import com.simibubi.create.AllItems;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.Create;
import com.simibubi.create.api.equipment.goggles.IProxyHoveringInformation;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.render.MultiPosDestructionHandler;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;

public abstract class AbstractTurbofanCoreBlock extends HorizontalDirectionalBlock
	implements IWrenchable, IProxyHoveringInformation, IAvionicsActorProvider {

	public static final BooleanProperty ASSEMBLED = BooleanProperty.create("assembled");

	public AbstractTurbofanCoreBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder
			.add(FACING, ASSEMBLED);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockPos pos = context.getClickedPos();

		for (int x = -1; x <= 1; x++) {
			for (int y = -1; y <= 1; y++) {
				for (int z = -1; z <= 1; z++) {
					BlockPos offset = new BlockPos(x, y, z);
					if (offset.equals(BlockPos.ZERO))
						continue;
					BlockState occupiedState = context.getLevel()
						.getBlockState(pos.offset(offset));
					if (!occupiedState.canBeReplaced())
						return null;
				}
			}
		}

		BlockState state = super.getStateForPlacement(context);
		Direction horizontalDirection = context.getHorizontalDirection();
		Player player = context.getPlayer();

		state = state.setValue(FACING, horizontalDirection.getOpposite()).setValue(ASSEMBLED, false);

		if (player != null && player.isShiftKeyDown())
			state = state.setValue(FACING, horizontalDirection).setValue(ASSEMBLED, false);

		return state;
	}

	@Override
	public PushReaction getPistonPushReaction(BlockState state) {
		return PushReaction.BLOCK;
	}

	@Override
	public InteractionResult onWrenched(BlockState state, UseOnContext context) {
		return InteractionResult.PASS;
	}

	protected static BlockPos localToWorld(BlockPos pos, Direction facing, int lx, int ly, int lz) {
		Direction right = facing.getClockWise();
		return pos.relative(right, lx)
			.relative(Direction.UP, ly)
			.relative(facing, lz);
	}

	@Override
	public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
		super.onPlace(state, level, pos, oldState, isMoving);
		if (!level.getBlockTicks()
			.hasScheduledTick(pos, this))
			level.scheduleTick(pos, this, 1);
	}

	@Override
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.hasBlockEntity() && (!state.is(newState.getBlock()) || !newState.hasBlockEntity())) {
			if (!level.isClientSide) {
				BlockEntity blockEntity = level.getBlockEntity(pos);
				if (blockEntity instanceof TurbofanBlockEntity turbofanBlockEntity) {
					ItemStack blades = turbofanBlockEntity.bladeInventory.extractItem(0, turbofanBlockEntity.getBladeCount(), false);
					if (!blades.isEmpty()) {
						Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), blades);
					}
				}
			}
		}
		super.onRemove(state, level, pos, newState, isMoving);
	}

	protected void destroyLinkedCore(Level level, BlockPos pos, BlockState state, Player player) {
		if (level.isClientSide())
			return;

		if (!state.getValue(ASSEMBLED) || !hasValidConnection(state, level, pos))
			return;

		Direction direction = state.getValue(FACING);
		BlockPos targetPos = pos.relative(direction.getOpposite(), 3);
		BlockState targetState = level.getBlockState(targetPos);

		if (!(targetState.getBlock() instanceof AbstractTurbofanCoreBlock))
			return;

		level.destroyBlockProgress(targetPos.hashCode(), targetPos, -1);
		boolean shouldDrop = !player.isCreative() && player.hasCorrectToolForDrops(targetState);
		level.destroyBlock(targetPos, shouldDrop);
	}

	@Override
	public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
		destroyLinkedCore(level, pos, state, player);
		return super.playerWillDestroy(level, pos, state, player);
	}

	@Override
	public InteractionResult onSneakWrenched(BlockState state, UseOnContext context) {
		boolean assembled = state.getValue(ASSEMBLED);

		Level level = context.getLevel();
		BlockPos pos = context.getClickedPos();

		if (assembled && hasValidConnection(state, level, pos)) {
			Direction direction = state.getValue(FACING);
			BlockPos targetPos = pos.relative(direction.getOpposite(), 3);
			BlockState targetState = level.getBlockState(targetPos);
			Player player = context.getPlayer();

			if ((level instanceof ServerLevel serverLevel)) {
				BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(level, targetPos, level.getBlockState(targetPos), player);
				NeoForge.EVENT_BUS.post(event);
				if (!event.isCanceled()) {
					if (player != null && !player.isCreative()) {
						Block.getDrops(targetState, serverLevel, targetPos, level.getBlockEntity(targetPos), player, context.getItemInHand())
							.forEach(itemStack -> {
								player.getInventory()
									.placeItemBackInInventory(itemStack);
							});
					}

					targetState.spawnAfterBreak(serverLevel, targetPos, ItemStack.EMPTY, true);
					level.destroyBlock(targetPos, false);
					AllSoundEvents.WRENCH_REMOVE.playOnServer(level, targetPos, 1, Create.RANDOM.nextFloat() * .5f + .5f);
				}
			}
		}
		return IWrenchable.super.onSneakWrenched(state, context);
	}

	protected boolean hasValidConnection(BlockState state, BlockGetter level, BlockPos pos) {
		Boolean assembled = state.getValue(ASSEMBLED);

		Direction direction = state.getValue(FACING);
		Direction opposite = direction.getOpposite();

		BlockPos targetPos = pos.relative(opposite, 3);
		BlockState targetState = level.getBlockState(targetPos);

		return targetState.getBlock() instanceof AbstractTurbofanCoreBlock && targetState.getBlock() != state.getBlock()
			&& targetState.getValue(ASSEMBLED) == assembled && targetState.getValue(FACING) == opposite;
	}

	protected BlockPos getControllerPos(BlockState state, BlockPos pos) {
		if (this instanceof TurbofanIntakeBlock)
			return pos;

		Direction direction = state.getValue(FACING);
		Direction opposite = direction.getOpposite();

		return pos.relative(opposite, 3);
	}

	@Override
	public BlockPos getInformationSource(Level level, BlockPos pos, BlockState state) {
		return hasValidConnection(state, level, pos) ? getControllerPos(state, pos) : pos;
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
											  Player player, InteractionHand hand, BlockHitResult hitResult) {
		if (!hasValidConnection(state, level, pos))
			return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

		ItemStack heldItem = player.getItemInHand(hand);
		boolean assembled = state.getValue(ASSEMBLED);
		BlockPos masterPos = getControllerPos(state, pos);
		BlockState masterState = level.getBlockState(masterPos);

		if (!assembled) {
			return heldItem.getItem() == AllItems.PRECISION_MECHANISM.get()
				? tryAssemble(heldItem, level, masterPos, player, hitResult)
				: ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		}

		if (heldItem.getItem() instanceof TurbofanBladeItem || heldItem.isEmpty())
			return bladeController(heldItem, masterState, level, masterPos, player, hand, hitResult);

		return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
	}

	private ItemInteractionResult tryAssemble(ItemStack stack, Level level,
											  BlockPos pos, Player player, BlockHitResult hitResult) {
		BlockEntity blockEntity = level.getBlockEntity(pos);

		if (blockEntity instanceof TurbofanBlockEntity turbofanBlockEntity) {
			int assemblyStage = turbofanBlockEntity.getAssemblyStage();

			if (assemblyStage < REQUIRED_ASSEMBLY_STAGE) {
				if (level.isClientSide)
					return ItemInteractionResult.sidedSuccess(true);

				if (!player.getAbilities().instabuild)
					stack.shrink(1);

				turbofanBlockEntity.incrementAssemblyStage();

				BlockPos soundEventPos = hitResult.getBlockPos();
				AllSoundEvents.WRENCH_ROTATE.playOnServer(level, soundEventPos, 1, Create.RANDOM.nextFloat() + .5f);

				return ItemInteractionResult.SUCCESS;
			}
		}
		return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}

	private ItemInteractionResult bladeController(ItemStack stack, BlockState state, Level level, BlockPos pos,
												  Player player, InteractionHand hand, BlockHitResult hitResult) {
		Direction facing = state.getValue(FACING);
		Direction clickedFace = hitResult.getDirection();
		BlockPos soundEventPos = hitResult.getBlockPos();

		if (clickedFace != facing)
			return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		if (!state.is(CKLBlocks.REGULAR_TURBOFAN_INTAKE.get()))
			return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		if (hand != InteractionHand.MAIN_HAND)
			return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

		BlockEntity blockEntity = level.getBlockEntity(pos);

		if (!(blockEntity instanceof TurbofanBlockEntity turbofanBlockEntity))
			return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

		boolean insertingBlade = stack.getItem() instanceof TurbofanBladeItem;
		boolean removingBlade = stack.isEmpty();

		if (!insertingBlade && !removingBlade)
			return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

		if (!turbofanBlockEntity.canInteractWithBlades())
			return ItemInteractionResult.FAIL;

		if (insertingBlade && turbofanBlockEntity.getBladeCount() < MAX_BLADES) {
			if (level.isClientSide)
				return ItemInteractionResult.sidedSuccess(true);

			if (turbofanBlockEntity.addBlade(stack)) {
				if (!player.getAbilities().instabuild)
					stack.shrink(1);

				level.playSound(null, soundEventPos, SoundEvents.ARMOR_EQUIP_IRON.value(), SoundSource.BLOCKS, 1,
					Create.RANDOM.nextFloat() + .125f);

				return ItemInteractionResult.SUCCESS;
			}
		}

		if (removingBlade && turbofanBlockEntity.getBladeCount() > 0) {
			if (level.isClientSide)
				return ItemInteractionResult.sidedSuccess(true);

			ItemStack removedBlade = turbofanBlockEntity.removeBlade();

			if (!removedBlade.isEmpty()) {
				if (!player.getAbilities().instabuild)
					player.getInventory().placeItemBackInInventory(removedBlade);

				level.playSound(null, soundEventPos, SoundEvents.ARMOR_EQUIP_IRON.value(), SoundSource.BLOCKS, 1,
					Create.RANDOM.nextFloat() + .125f);

				return ItemInteractionResult.SUCCESS;
			}
		}

		return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}

	@Nullable
	static BlockPos getTurbofanActorBlockEntityPos(Level level, BlockPos corePos) {
		BlockState coreState = level.getBlockState(corePos);

		if (!(coreState.getBlock() instanceof AbstractTurbofanCoreBlock coreBlock))
			return null;

		if (!coreState.getValue(ASSEMBLED) || !coreBlock.hasValidConnection(coreState, level, corePos))
			return null;

		BlockPos controllerPos = coreBlock.getControllerPos(coreState, corePos);

		return level.getBlockEntity(controllerPos) instanceof IAvionicsActor ? controllerPos : null;
	}

	@Override
	@Nullable
	public BlockPos getActorBlockEntityPos(Level level, BlockPos clickedPos) {
		BlockState state = level.getBlockState(clickedPos);

		if (!state.is(this))
			return null;

		return getTurbofanActorBlockEntityPos(level, clickedPos);
	}

	public static class RenderProperties implements IClientBlockExtensions, MultiPosDestructionHandler {

		@Override
		public boolean addDestroyEffects(BlockState state, Level level, BlockPos pos, ParticleEngine manager) {
			return true;
		}

		@Override
		public boolean addHitEffects(BlockState state, Level level, HitResult target, ParticleEngine manager) {
			if (target instanceof BlockHitResult bhr) {
				BlockPos pos = bhr.getBlockPos();
				if (state.getBlock() instanceof AbstractTurbofanCoreBlock abstractTurbofanCoreBlock) {
					boolean assembled = state.getValue(ASSEMBLED);
					if (assembled && abstractTurbofanCoreBlock.hasValidConnection(state, level, pos)) {
						Direction direction = state.getValue(FACING);
						Direction opposite = direction.getOpposite();
						BlockPos targetPos = pos.relative(opposite, 3);
						manager.crack(targetPos, bhr.getDirection());
						return true;
					}
				}
			}
			return IClientBlockExtensions.super.addHitEffects(state, level, target, manager);
		}

		@Override
		@Nullable
		public Set<BlockPos> getExtraPositions(ClientLevel level, BlockPos pos, BlockState state, int progress) {
			if (state.getBlock() instanceof AbstractTurbofanCoreBlock abstractTurbofanCoreBlock) {
				boolean assembled = state.getValue(ASSEMBLED);
				if (!assembled || !abstractTurbofanCoreBlock.hasValidConnection(state, level, pos))
					return null;
				Direction direction = state.getValue(FACING);
				Direction opposite = direction.getOpposite();
				BlockPos targetPos = pos.relative(opposite, 3);
				HashSet<BlockPos> set = new HashSet<>();
				set.add(targetPos);
				return set;
			}
			return null;
		}
	}
}
