package net.flybywire.createkineticlift.content.turbofan;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class TurbofanStructuralBlockEntity extends BlockEntity {

	private BlockPos corePos;
	private Vec3i localOffset;

	public TurbofanStructuralBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public void setCorePos(BlockPos corePos) {
		this.corePos = corePos;
		setChanged();
	}

	public BlockPos getCorePos() {
		return corePos;
	}

	public void setLocalOffset(Vec3i localOffset) {
		this.localOffset = localOffset;
		setChanged();
	}

	public Vec3i getLocalOffset() {
		return localOffset;
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
