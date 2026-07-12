package net.flybywire.createkineticlift.network.avionics;

import net.flybywire.createkineticlift.avionics.AvionicsNetworkManager;
import net.flybywire.createkineticlift.avionics.IAvionicsSource;
import net.flybywire.createkineticlift.network.CKLPackets;

import io.netty.buffer.ByteBuf;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public record AvionicsNetworkSyncRequestPacket(BlockPos sourcePos, int sessionId) implements ServerboundPacketPayload {

	public static final StreamCodec<ByteBuf, AvionicsNetworkSyncRequestPacket> STREAM_CODEC =
		StreamCodec.composite(BlockPos.STREAM_CODEC, AvionicsNetworkSyncRequestPacket::sourcePos,
			ByteBufCodecs.VAR_INT, AvionicsNetworkSyncRequestPacket::sessionId,
			AvionicsNetworkSyncRequestPacket::new);

	@Override
	public void handle(ServerPlayer player) {
		ServerLevel level = player.serverLevel();

		if (!(level.getBlockEntity(sourcePos) instanceof IAvionicsSource))
			return;

		AvionicsNetworkManager manager = AvionicsNetworkManager.get(level);
		AvionicsNetworkSyncPacket.sendTo(player, sourcePos, sessionId, manager.getConnectionsFrom(level, sourcePos));
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return CKLPackets.AVIONICS_NETWORK_SYNC_REQUEST;
	}
}
