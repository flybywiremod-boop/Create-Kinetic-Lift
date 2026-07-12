package net.flybywire.createkineticlift.network.avionics;

import java.util.HashSet;
import java.util.Set;

import net.flybywire.createkineticlift.avionics.AvionicsNetworkManager;
import net.flybywire.createkineticlift.content.cable.ComputerCableEditingState;
import net.flybywire.createkineticlift.network.CKLPackets;

import io.netty.buffer.ByteBuf;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public record AvionicsNetworkSyncPacket(BlockPos sourcePos, int sessionId, Set<BlockPos> connections)
	implements ClientboundPacketPayload {

	private static final StreamCodec<ByteBuf, Set<BlockPos>> CONNECTIONS_CODEC =
		ByteBufCodecs.collection(HashSet::new, BlockPos.STREAM_CODEC, AvionicsNetworkManager.MAX_PERIPHERALS_PER_SOURCE);

	public static final StreamCodec<ByteBuf, AvionicsNetworkSyncPacket> STREAM_CODEC =
		StreamCodec.composite(BlockPos.STREAM_CODEC, AvionicsNetworkSyncPacket::sourcePos,
			ByteBufCodecs.VAR_INT, AvionicsNetworkSyncPacket::sessionId,
			CONNECTIONS_CODEC, AvionicsNetworkSyncPacket::connections,
			AvionicsNetworkSyncPacket::new);

	public AvionicsNetworkSyncPacket {
		connections = Set.copyOf(connections);
	}

	public static void sendTo(ServerPlayer player, BlockPos sourcePos, int sessionId, Set<BlockPos> connections) {
		CatnipServices.NETWORK.sendToClient(player, new AvionicsNetworkSyncPacket(sourcePos, sessionId, connections));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void handle(LocalPlayer player) {
		ComputerCableEditingState.applySync(sourcePos, sessionId, connections);
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return CKLPackets.AVIONICS_NETWORK_SYNC;
	}
}
