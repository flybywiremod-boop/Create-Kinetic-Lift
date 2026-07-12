package net.flybywire.createkineticlift.content.turbofan;

import org.jetbrains.annotations.NotNull;

import net.flybywire.createkineticlift.content.turbofan.TurbofanStructuralBlock.TurbofanType;
import net.flybywire.createkineticlift.registries.CKLBlockEntityTypes;
import net.flybywire.createkineticlift.registries.CKLBlocks;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.foundation.block.IBE;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class TurbofanIntakeBlock extends AbstractTurbofanCoreBlock implements IBE<TurbofanBlockEntity> {

	public static final MapCodec<TurbofanIntakeBlock> CODEC = simpleCodec(TurbofanIntakeBlock::new);

	public TurbofanIntakeBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState()
			.setValue(ASSEMBLED, false));
	}

	@Override
	protected @NotNull MapCodec<? extends HorizontalDirectionalBlock> codec() {
		return CODEC;
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

						BlockPos targetPos = localToWorld(pos, facing, lx, ly, lz);

						level.setBlockAndUpdate(targetPos, CKLBlocks.TURBOFAN_STRUCTURAL.get().defaultBlockState()
							.setValue(FACING, facing)
							.setValue(TurbofanStructuralBlock.TYPE, TurbofanType.INTAKE));

						if (level.getBlockEntity(targetPos) instanceof TurbofanStructuralBlockEntity be) {
							be.setCorePos(pos);
							be.setLocalOffset(offset);
						}
					}
				}
			}
		}
	}

	@Override
	public Class<TurbofanBlockEntity> getBlockEntityClass() {
		return TurbofanBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends TurbofanBlockEntity> getBlockEntityType() {
		return CKLBlockEntityTypes.TURBOFAN_INTAKE.get();
	}
}
