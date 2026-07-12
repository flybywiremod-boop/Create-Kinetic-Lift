package net.flybywire.createkineticlift.content.turbofan;

import org.jetbrains.annotations.Nullable;

import net.flybywire.createkineticlift.registries.CKLBlockEntityTypes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public class TurbofanStructuralBlockEntity extends BlockEntity {

	private BlockPos corePos;
	private Vec3i localOffset;

	public TurbofanStructuralBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public static void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.registerBlockEntity(
			Capabilities.FluidHandler.BLOCK,
			CKLBlockEntityTypes.TURBOFAN_STRUCTURAL.get(),
			(blockEntity, side) -> blockEntity.getFuelHandler()
		);
	}

	void setCorePos(BlockPos corePos) {
		this.corePos = corePos;
		setChanged();
		invalidateCapabilities();
	}

	BlockPos getCorePos() {
		return corePos;
	}

	void setLocalOffset(Vec3i localOffset) {
		this.localOffset = localOffset;
		setChanged();
		invalidateCapabilities();
	}

	Vec3i getLocalOffset() {
		return localOffset;
	}

	@Nullable
	private IFluidHandler getFuelHandler() {
		if (!isFuelPort() || level == null || corePos == null)
			return null;

		BlockEntity blockEntity = level.getBlockEntity(corePos);

		if (!(blockEntity instanceof TurbofanBlockEntity turbofanBlockEntity))
			return null;

		return turbofanBlockEntity.getFuelHandler();
	}

	private boolean isFuelPort() {
		if (localOffset == null)
			return false;

		return getBlockState().getValue(TurbofanStructuralBlock.TYPE) == TurbofanStructuralBlock.TurbofanType.INTAKE
			&& localOffset.getX() == 0
			&& localOffset.getY() == 1
			&& localOffset.getZ() == -1;
	}

	public void invalidateCapabilities() {
		if (level != null)
			level.invalidateCapabilities(worldPosition);
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);

		if (corePos != null) {
			tag.putLong("CorePos", corePos.asLong());
		}

		if (localOffset != null) {
			tag.putInt("OffsetX", localOffset.getX());
			tag.putInt("OffsetY", localOffset.getY());
			tag.putInt("OffsetZ", localOffset.getZ());
		}
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);

		corePos = BlockPos.of(tag.getLong("CorePos"));

		localOffset = new Vec3i(
			tag.getInt("OffsetX"),
			tag.getInt("OffsetY"),
			tag.getInt("OffsetZ")
		);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		CompoundTag tag = new CompoundTag();
		this.saveAdditional(tag, registries);
		return tag;
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}
}
