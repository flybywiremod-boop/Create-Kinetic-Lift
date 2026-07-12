package net.flybywire.createkineticlift.content.turbofan;

import static net.flybywire.createkineticlift.content.turbofan.AbstractTurbofanCoreBlock.ASSEMBLED;

import java.util.HashSet;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.flybywire.createkineticlift.avionics.IAvionicsActorProvider;
import net.flybywire.createkineticlift.registries.CKLBlockEntityTypes;
import net.flybywire.createkineticlift.registries.CKLBlocks;
import net.flybywire.createkineticlift.registries.CKLShapes;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.api.equipment.goggles.IProxyHoveringInformation;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.block.render.MultiPosDestructionHandler;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions;

public class TurbofanStructuralBlock extends HorizontalDirectionalBlock
	implements IWrenchable, IProxyHoveringInformation, IAvionicsActorProvider, IBE<TurbofanStructuralBlockEntity> {

	public static final EnumProperty<TurbofanType> TYPE =
		EnumProperty.create("type", TurbofanType.class);

	public enum TurbofanType implements StringRepresentable {
		INTAKE("intake"),
		EXHAUST("exhaust");

		private final String name;

		TurbofanType(String name) {
			this.name = name;
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}
	}

	public static final MapCodec<TurbofanStructuralBlock> CODEC = simpleCodec(TurbofanStructuralBlock::new);

	public TurbofanStructuralBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(FACING, TYPE));
	}

	// This is pure hell but it works so f this for now
	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		Direction direction = state.getValue(FACING);
		TurbofanType type = state.getValue(TYPE);
		Vec3i localOffset = getLocalOffset(level, pos);

		if (localOffset == null) return super.getShape(state, level, pos, context);

		int lx = localOffset.getX();
		int ly = localOffset.getY();
		int lz = localOffset.getZ();

		String key = lx + ", " + ly;

		if (type == TurbofanType.INTAKE) {
			if (lz == 1) {
				return switch (key) {
					case "1, 1" -> CKLShapes.TURBOFAN_STRUCTURAL_INTAKE_TOP_LEFT_CUT.get(direction);
					case "-1, 1" -> CKLShapes.TURBOFAN_STRUCTURAL_INTAKE_TOP_RIGHT_CUT.get(direction);
					case "1, -1" -> CKLShapes.TURBOFAN_STRUCTURAL_INTAKE_BOTTOM_LEFT_CUT.get(direction);
					case "-1, -1" -> CKLShapes.TURBOFAN_STRUCTURAL_INTAKE_BOTTOM_RIGHT_CUT.get(direction);
					default -> CKLShapes.TURBOFAN_STRUCTURAL_INTAKE_CUT.get(direction);
				};
			} else {
				return switch (key) {
					case "1, 1" -> CKLShapes.TURBOFAN_STRUCTURAL_INTAKE_TOP_LEFT.get(direction);
					case "-1, 1" -> CKLShapes.TURBOFAN_STRUCTURAL_INTAKE_TOP_RIGHT.get(direction);
					case "1, -1" -> CKLShapes.TURBOFAN_STRUCTURAL_INTAKE_BOTTOM_LEFT.get(direction);
					case "-1, -1" -> CKLShapes.TURBOFAN_STRUCTURAL_INTAKE_BOTTOM_RIGHT.get(direction);
					default -> CKLShapes.TURBOFAN_STRUCTURAL_INTAKE.get(direction);
				};
			}
		}
		if (type == TurbofanType.EXHAUST) {
			if (lz == 1) return CKLShapes.TURBOFAN_STRUCTURAL_EXHAUST_TIP.get(direction);

			return switch (key) {
				case "1, 1" -> CKLShapes.TURBOFAN_STRUCTURAL_EXHAUST_TOP_LEFT.get(direction);
				case "0, 1" -> CKLShapes.TURBOFAN_STRUCTURAL_EXHAUST_TOP.get(direction);
				case "-1, 1" -> CKLShapes.TURBOFAN_STRUCTURAL_EXHAUST_TOP_RIGHT.get(direction);
				case "1, 0" -> CKLShapes.TURBOFAN_STRUCTURAL_EXHAUST_LEFT.get(direction);
				case "-1, 0" -> CKLShapes.TURBOFAN_STRUCTURAL_EXHAUST_RIGHT.get(direction);
				case "1, -1" -> CKLShapes.TURBOFAN_STRUCTURAL_EXHAUST_BOTTOM_LEFT.get(direction);
				case "0, -1" -> CKLShapes.TURBOFAN_STRUCTURAL_EXHAUST_BOTTOM.get(direction);
				case "-1, -1" -> CKLShapes.TURBOFAN_STRUCTURAL_EXHAUST_BOTTOM_RIGHT.get(direction);
				default -> super.getShape(state, level, pos, context);
			};
		}
		return super.getShape(state, level, pos, context);
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.INVISIBLE;
	}

	@Override
	public PushReaction getPistonPushReaction(BlockState state) {
		return PushReaction.BLOCK;
	}

	@Override
	public InteractionResult onWrenched(BlockState state, UseOnContext context) {
		return InteractionResult.PASS;
	}

	@Override
	public InteractionResult onSneakWrenched(BlockState state, UseOnContext context) {
		BlockPos clickedPos = context.getClickedPos();
		Level level = context.getLevel();

		if (stillValid(level, clickedPos, state)) {
			BlockPos corePos = getCorePos(level, clickedPos);
			state = level.getBlockState(corePos);
			context = new UseOnContext(level, context.getPlayer(), context.getHand(), context.getItemInHand(),
				new BlockHitResult(context.getClickLocation(), context.getClickedFace(), corePos,
					context.isInside()));
			if (state.getBlock() instanceof AbstractTurbofanCoreBlock abstractTurbofanCoreBlock)
				return abstractTurbofanCoreBlock.onSneakWrenched(state, context);
		}

		return IWrenchable.super.onSneakWrenched(state, context);
	}

	@Override
	public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
		return state.getValue(TYPE) == TurbofanType.INTAKE
			? CKLBlocks.REGULAR_TURBOFAN_INTAKE.asStack()
			: CKLBlocks.REGULAR_TURBOFAN_EXHAUST.asStack();
	}

	@Override
	public boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
		return false;
	}

	@Override
	public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
		return 1.0F;
	}

	@Override
	protected @NotNull MapCodec<? extends HorizontalDirectionalBlock> codec() {
		return CODEC;
	}

	@Override
	public Class<TurbofanStructuralBlockEntity> getBlockEntityClass() {
		return TurbofanStructuralBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends TurbofanStructuralBlockEntity> getBlockEntityType() {
		return CKLBlockEntityTypes.TURBOFAN_STRUCTURAL.get();
	}

	public @Nullable BlockPos getCorePos(BlockGetter level, BlockPos pos) {
		TurbofanStructuralBlockEntity be = getBlockEntity(level, pos);
		return be != null ? be.getCorePos() : null;
	}

	public @Nullable Vec3i getLocalOffset(BlockGetter level, BlockPos pos) {
		TurbofanStructuralBlockEntity be = getBlockEntity(level, pos);
		return be != null ? be.getLocalOffset() : null;
	}

	public boolean stillValid(BlockGetter level, BlockPos pos, BlockState state) {
		if (!state.is(this)) return false;

		BlockPos corePos = getCorePos(level, pos);
		if (corePos == null) return false;

		BlockState coreState = level.getBlockState(corePos);
		if (!(coreState.getBlock() instanceof AbstractTurbofanCoreBlock)) return false;

		return state.getValue(FACING) == coreState.getValue(FACING);
	}

	@Override
	public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
		if (stillValid(level, pos, state)) {
			BlockPos corePos = getCorePos(level, pos);

			if (corePos != null) {
				BlockState coreState = level.getBlockState(corePos);

				if (coreState.getBlock() instanceof AbstractTurbofanCoreBlock abstractTurbofanCoreBlock)
					abstractTurbofanCoreBlock.destroyLinkedCore(level, corePos, coreState, player);
				boolean shouldDrop = !player.isCreative() && player.hasCorrectToolForDrops(coreState);
				level.destroyBlock(corePos, shouldDrop);
			}
		}

		return super.playerWillDestroy(level, pos, state, player);
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
											  Player player, InteractionHand hand, BlockHitResult hitResult) {
		if (!stillValid(level, pos, state))
			return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

		BlockPos corePos = getCorePos(level, pos);

		if (corePos == null)
			return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

		BlockState coreState = level.getBlockState(corePos);

		if (coreState.getBlock() instanceof AbstractTurbofanCoreBlock abstractTurbofanCoreBlock) {
			return abstractTurbofanCoreBlock.useItemOn(stack, coreState, level, corePos, player, hand, hitResult);
		}
		return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}

	@Override
	public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor levelAccessor,
								  BlockPos currentPos, BlockPos facingPos) {
		if (stillValid(levelAccessor, currentPos, state)) {
			BlockPos corePos = getCorePos(levelAccessor, currentPos);
			Block coreBlock = levelAccessor.getBlockState(corePos).getBlock();

			if (!levelAccessor.getBlockTicks()
				.hasScheduledTick(corePos, coreBlock))
				levelAccessor.scheduleTick(corePos, coreBlock, 1);
			return state;
		}
		if (!(levelAccessor instanceof Level level) || level.isClientSide())
			return state;
		if (!level.getBlockTicks()
			.hasScheduledTick(currentPos, this))
			level.scheduleTick(currentPos, this, 1);
		return state;
	}

	@Override
	public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (!stillValid(level, pos, state))
			level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
	}

	@Override
	@Nullable
	public BlockPos getActorBlockEntityPos(Level level, BlockPos clickedPos) {
		BlockState state = level.getBlockState(clickedPos);

		if (!state.is(this))
			return null;

		if (!stillValid(level, clickedPos, state))
			return null;

		BlockPos corePos = getCorePos(level, clickedPos);

		if (corePos == null)
			return null;

		return AbstractTurbofanCoreBlock.getTurbofanActorBlockEntityPos(level, corePos);
	}

	@Override
	public BlockPos getInformationSource(Level level, BlockPos pos, BlockState state) {
		BlockPos actorPos = getActorBlockEntityPos(level, pos);

		if (actorPos != null)
			return actorPos;

		BlockPos corePos = getCorePos(level, pos);

		if (corePos == null)
			return pos;

		BlockState coreState = level.getBlockState(corePos);

		if (coreState.getBlock() instanceof AbstractTurbofanCoreBlock coreBlock)
			return coreBlock.getControllerPos(coreState, corePos);

		return corePos;
	}

	@Override
	public boolean addLandingEffects(BlockState state1, ServerLevel level, BlockPos pos, BlockState state2,
									 LivingEntity entity, int numberOfParticles) {
		return true;
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
				TurbofanStructuralBlock turbofanStructuralBlock = CKLBlocks.TURBOFAN_STRUCTURAL.get();
				if (turbofanStructuralBlock.stillValid(level, pos, state)) {
					manager.crack(turbofanStructuralBlock.getCorePos(level, pos), bhr.getDirection());
					BlockPos corePos = turbofanStructuralBlock.getCorePos(level, pos);
					BlockState coreState = level.getBlockState(corePos);
					if (coreState.getBlock() instanceof AbstractTurbofanCoreBlock abstractTurbofanCoreBlock) {
						boolean assembled = coreState.getValue(ASSEMBLED);
						if (assembled && abstractTurbofanCoreBlock.hasValidConnection(coreState, level, corePos)) {
							Direction direction = coreState.getValue(FACING);
							Direction opposite = direction.getOpposite();
							BlockPos targetPos = corePos.relative(opposite, 3);
							manager.crack(targetPos, bhr.getDirection());
						}
					}
				}
				return true;
			}
			return IClientBlockExtensions.super.addHitEffects(state, level, target, manager);
		}

		@Override
		@Nullable
		public Set<BlockPos> getExtraPositions(ClientLevel level, BlockPos pos, BlockState state, int progress) {
			TurbofanStructuralBlock turbofanStructuralBlock = CKLBlocks.TURBOFAN_STRUCTURAL.get();
			if (!turbofanStructuralBlock.stillValid(level, pos, state))
				return null;
			BlockPos corePos = turbofanStructuralBlock.getCorePos(level, pos);
			BlockState coreState = level.getBlockState(corePos);
			HashSet<BlockPos> set = new HashSet<>();
			if (coreState.getBlock() instanceof AbstractTurbofanCoreBlock abstractTurbofanCoreBlock) {
				boolean assembled = coreState.getValue(ASSEMBLED);
				if (assembled && abstractTurbofanCoreBlock.hasValidConnection(coreState, level, corePos)) {
					Direction direction = coreState.getValue(FACING);
					Direction opposite = direction.getOpposite();
					BlockPos targetPos = corePos.relative(opposite, 3);
					set.add(targetPos);
				}
			}
			set.add(turbofanStructuralBlock.getCorePos(level, pos));
			return set;
		}
	}
}
