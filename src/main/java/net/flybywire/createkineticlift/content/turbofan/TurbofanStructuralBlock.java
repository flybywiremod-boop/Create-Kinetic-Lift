package net.flybywire.createkineticlift.content.turbofan;

import java.util.HashSet;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.flybywire.createkineticlift.registries.CKLBlockEntityTypes;
import net.flybywire.createkineticlift.registries.CKLBlocks;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.block.render.MultiPosDestructionHandler;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
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
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions;

public class TurbofanStructuralBlock extends HorizontalDirectionalBlock implements IWrenchable, IBE<TurbofanStructuralBlockEntity> {

	public static final MapCodec<TurbofanStructuralBlock> CODEC = simpleCodec(TurbofanStructuralBlock::new);

	public TurbofanStructuralBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(FACING));
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
	public InteractionResult onSneakWrenched(BlockState state, UseOnContext context) {
		BlockPos clickedPos = context.getClickedPos();
		Level level = context.getLevel();

		if (stillValid(level, clickedPos, state)) {
			BlockPos corePos = getCorePos(level, clickedPos);
			context = new UseOnContext(level, context.getPlayer(), context.getHand(), context.getItemInHand(),
				new BlockHitResult(context.getClickLocation(), context.getClickedFace(), corePos,
					context.isInside()));
			state = level.getBlockState(corePos);
		}

		return IWrenchable.super.onSneakWrenched(state, context);
	}

	@Override
	public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
		return CKLBlocks.TURBOFAN_INTAKE.asStack();
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

	public boolean stillValid(BlockGetter level, BlockPos pos, BlockState state) {
		if (!state.is(this)) return false;

		BlockPos corePos = getCorePos(level, pos);
		if (corePos == null) return false;

		BlockState coreState = level.getBlockState(corePos);
		if (!(coreState.getBlock() instanceof AbstractTurbofanCoreBlock)) return false;

		return state.getValue(FACING) == coreState.getValue(FACING);
	}

	@Override
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
		if (stillValid(level, pos, state))
			level.destroyBlock(getCorePos(level, pos), true);
	}

	@Override
	public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
		if (stillValid(level, pos, state)) {
			BlockPos corePos = getCorePos(level, pos);
			level.destroyBlockProgress(corePos.hashCode(), corePos, -1);
			if (!level.isClientSide() && player.isCreative())
				level.destroyBlock(corePos, false);
		}
		return super.playerWillDestroy(level, pos, state, player);
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
				BlockPos targetPos = bhr.getBlockPos();
				TurbofanStructuralBlock turbofanStructuralBlock = CKLBlocks.TURBOFAN_STRUCTURAL.get();
				if (turbofanStructuralBlock.stillValid(level, targetPos, state))
					manager.crack(turbofanStructuralBlock.getCorePos(level, targetPos), bhr.getDirection());
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
			HashSet<BlockPos> set = new HashSet<>();
			set.add(turbofanStructuralBlock.getCorePos(level, pos));
			return set;
		}
	}
}
