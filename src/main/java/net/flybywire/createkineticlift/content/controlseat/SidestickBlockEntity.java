package net.flybywire.createkineticlift.content.controlseat;

import java.util.List;

import net.flybywire.createkineticlift.avionics.IAvionicsSource;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class SidestickBlockEntity extends SmartBlockEntity implements IAvionicsSource {

	public SidestickBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
	}

	@Override
	public boolean isControlledBy(Player player) {
		return player.getVehicle() instanceof SidestickEntity seat
			&& worldPosition.equals(seat.getSourcePos());
	}
}
