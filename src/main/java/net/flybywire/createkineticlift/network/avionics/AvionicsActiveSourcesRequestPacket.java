package net.flybywire.createkineticlift.network.avionics;

import net.flybywire.createkineticlift.avionics.AvionicsNetworkManager;
import net.flybywire.createkineticlift.network.CKLPackets;

import io.netty.buffer.ByteBuf;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public record AvionicsActiveSourcesRequestPacket() implements ServerboundPacketPayload {

	public static final StreamCodec<ByteBuf, AvionicsActiveSourcesRequestPacket> STREAM_CODEC =
		StreamCodec.unit(new AvionicsActiveSourcesRequestPacket());

	@Override
	public void handle(ServerPlayer player) {
		ServerLevel level = player.serverLevel();
		AvionicsActiveSourcesPacket.sendTo(player, AvionicsNetworkManager.get(level).getActiveSources(level));
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return CKLPackets.AVIONICS_ACTIVE_SOURCES_REQUEST;
	}
}
