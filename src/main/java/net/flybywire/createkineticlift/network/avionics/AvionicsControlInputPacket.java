package net.flybywire.createkineticlift.network.avionics;

import net.flybywire.createkineticlift.avionics.AvionicsNetworkManager;
import net.flybywire.createkineticlift.avionics.ControlInput;
import net.flybywire.createkineticlift.avionics.IAvionicsSource;
import net.flybywire.createkineticlift.avionics.PeripheralControl;
import net.flybywire.createkineticlift.network.CKLPackets;

import io.netty.buffer.ByteBuf;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public record AvionicsControlInputPacket(BlockPos sourcePos, PeripheralControl control, float value)
	implements ServerboundPacketPayload {

	public static final StreamCodec<ByteBuf, AvionicsControlInputPacket> STREAM_CODEC =
		StreamCodec.composite(BlockPos.STREAM_CODEC, AvionicsControlInputPacket::sourcePos,
			PeripheralControl.STREAM_CODEC, AvionicsControlInputPacket::control,
			ByteBufCodecs.FLOAT, AvionicsControlInputPacket::value,
			AvionicsControlInputPacket::new);

	@Override
	public void handle(ServerPlayer player) {
		ServerLevel level = player.serverLevel();

		if (!(level.getBlockEntity(sourcePos) instanceof IAvionicsSource source))
			return;

		if (!source.isControlledBy(player))
			return;

		AvionicsNetworkManager.get(level)
			.setSourceInput(level, sourcePos, new ControlInput(control, value));
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return CKLPackets.AVIONICS_CONTROL_INPUT;
	}
}
