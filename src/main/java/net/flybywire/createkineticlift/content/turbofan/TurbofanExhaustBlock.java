package net.flybywire.createkineticlift.content.turbofan;

import org.jetbrains.annotations.NotNull;

import net.flybywire.createkineticlift.content.turbofan.TurbofanStructuralBlock.TurbofanType;
import net.flybywire.createkineticlift.registries.CKLBlocks;
import net.flybywire.createkineticlift.registries.CKLShapes;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TurbofanExhaustBlock extends AbstractTurbofanCoreBlock {

	public static final MapCodec<TurbofanExhaustBlock> CODEC = simpleCodec(TurbofanExhaustBlock::new);

	public TurbofanExhaustBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState()
			.setValue(ASSEMBLED, false));
	}

	@Override
	protected @NotNull MapCodec<? extends HorizontalDirectionalBlock> codec() {
		return CODEC;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		Direction direction = state.getValue(FACING);

		return CKLShapes.TURBOFAN_EXHAUST.get(direction);
	}

	@Override
	public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		boolean assembled = state.getValue(ASSEMBLED);
		if (!assembled) {
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
							.setValue(FACING, facing)
							.setValue(TurbofanStructuralBlock.TYPE, TurbofanType.EXHAUST));

						if (level.getBlockEntity(targetPos) instanceof TurbofanStructuralBlockEntity be) {
							be.setCorePos(pos);
							be.setLocalOffset(offset);
						}
					}
				}
			}
		}
	}
}
