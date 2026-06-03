package net.flybywire.createkineticlift.content.turbofan;

import org.jetbrains.annotations.NotNull;

import net.flybywire.createkineticlift.registries.CKLBlocks;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.equipment.wrench.IWrenchable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;

public class TurbofanExhaustBlock extends AbstractTurbofanCoreBlock implements IWrenchable {

	public static final MapCodec<TurbofanExhaustBlock> CODEC = simpleCodec(TurbofanExhaustBlock::new);

	public TurbofanExhaustBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder
			.add(FACING);
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

		return super.getStateForPlacement(context)
			.setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	@Override
	protected @NotNull MapCodec<? extends HorizontalDirectionalBlock> codec() {
		return CODEC;
	}

	@Override
	public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
		super.onPlace(state, level, pos, oldState, isMoving);
		if (!level.getBlockTicks()
			.hasScheduledTick(pos, this))
			level.scheduleTick(pos, this, 1);
	}

	protected static BlockPos localToWorld(BlockPos pos, Direction facing, int lx, int ly, int lz) {
		Direction right = facing.getClockWise();
		return pos.relative(right, lx)
			.relative(Direction.UP, ly)
			.relative(facing, lz);
	}

	@Override
	public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		Direction facing = state.getValue(FACING);

		for (int lx = -1; lx <= 1; lx++) {
			for (int ly = -1; ly <= 1; ly++) {
				for (int lz = -1; lz <= 1; lz++) {
					BlockPos offset = new BlockPos(lx, ly, lz);
					if (offset.equals(BlockPos.ZERO))
						continue;
					if (lz != -1 && (lx != 0 || ly != 0))
						continue;

					BlockPos targetPos = localToWorld(pos, facing, lx, ly, lz);

					level.setBlockAndUpdate(targetPos, CKLBlocks.TURBOFAN_STRUCTURAL.get().defaultBlockState()
						.setValue(FACING, facing));

					if (level.getBlockEntity(targetPos) instanceof TurbofanStructuralBlockEntity be) {

						Vec3i localOffset = targetPos.subtract(pos);
						be.setLocalOffset(localOffset);

						be.setCorePos(pos);
						be.setLocalOffset(localOffset);
					}
				}
			}
		}
	}
}
