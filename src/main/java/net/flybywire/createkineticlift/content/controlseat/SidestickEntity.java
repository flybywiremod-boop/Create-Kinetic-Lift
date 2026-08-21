package net.flybywire.createkineticlift.content.controlseat;

import net.flybywire.createkineticlift.avionics.AvionicsNetworkManager;
import net.flybywire.createkineticlift.registries.CKLEntityTypes;

import com.simibubi.create.content.contraptions.actors.seat.SeatEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class SidestickEntity extends SeatEntity {

	private static final EntityDataAccessor<BlockPos> SOURCE_POS =
		SynchedEntityData.defineId(SidestickEntity.class, EntityDataSerializers.BLOCK_POS);

	public SidestickEntity(EntityType<?> type, Level level) {
		super(type, level);
		noPhysics = true;
	}

	public SidestickEntity(Level level, BlockPos sourcePos) {
		this(CKLEntityTypes.CONTROL_SEAT.get(), level);
		setSourcePos(sourcePos);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(SOURCE_POS, BlockPos.ZERO);
	}

	public BlockPos getSourcePos() {
		return entityData.get(SOURCE_POS);
	}

	public void setSourcePos(BlockPos sourcePos) {
		entityData.set(SOURCE_POS, sourcePos.immutable());
	}

	@SuppressWarnings("resource")
	@Override
	public void tick() {
		if (level().isClientSide)
			return;

		ServerLevel serverLevel = (ServerLevel) level();
		BlockPos sourcePos = getSourcePos();

		boolean sourcePresent = serverLevel.getBlockState(sourcePos)
			.getBlock() instanceof SidestickBlock
			&& serverLevel.getBlockEntity(sourcePos) instanceof SidestickBlockEntity;

		if (isVehicle() && sourcePresent)
			return;

		AvionicsNetworkManager.get(serverLevel).clearSourceInputs(serverLevel, sourcePos);
		discard();
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putLong("SourcePos", getSourcePos().asLong());
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);

		if (tag.contains("SourcePos"))
			setSourcePos(BlockPos.of(tag.getLong("SourcePos")));
	}
}
