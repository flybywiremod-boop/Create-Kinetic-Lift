package net.flybywire.createkineticlift.network.avionics;

import java.util.HashSet;
import java.util.Set;

import net.flybywire.createkineticlift.content.cable.ComputerCableSourceOutlineState;
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

public record AvionicsActiveSourcesPacket(Set<BlockPos> sources) implements ClientboundPacketPayload {

	private static final int MAX_LINKED_SOURCES = 1024;

	private static final StreamCodec<ByteBuf, Set<BlockPos>> SOURCES_CODEC =
		ByteBufCodecs.collection(HashSet::new, BlockPos.STREAM_CODEC, MAX_LINKED_SOURCES);

	public static final StreamCodec<ByteBuf, AvionicsActiveSourcesPacket> STREAM_CODEC =
		StreamCodec.composite(SOURCES_CODEC, AvionicsActiveSourcesPacket::sources, AvionicsActiveSourcesPacket::new);

	public AvionicsActiveSourcesPacket {
		sources = Set.copyOf(sources);
	}

	public static void sendTo(ServerPlayer player, Set<BlockPos> sources) {
		CatnipServices.NETWORK.sendToClient(player, new AvionicsActiveSourcesPacket(sources));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void handle(LocalPlayer player) {
		ComputerCableSourceOutlineState.setActiveSources(sources);
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return CKLPackets.AVIONICS_ACTIVE_SOURCES;
	}
}
