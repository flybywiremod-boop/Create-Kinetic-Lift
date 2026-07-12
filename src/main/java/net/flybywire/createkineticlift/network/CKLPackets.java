package net.flybywire.createkineticlift.network;

import java.util.Locale;

import net.flybywire.createkineticlift.CreateKineticLift;
import net.flybywire.createkineticlift.network.avionics.AvionicsActiveSourcesPacket;
import net.flybywire.createkineticlift.network.avionics.AvionicsActiveSourcesRequestPacket;
import net.flybywire.createkineticlift.network.avionics.AvionicsControlInputPacket;
import net.flybywire.createkineticlift.network.avionics.AvionicsNetworkSyncPacket;
import net.flybywire.createkineticlift.network.avionics.AvionicsNetworkSyncRequestPacket;
import net.flybywire.createkineticlift.network.avionics.CableAddConnectionPacket;
import net.flybywire.createkineticlift.network.avionics.CableRemoveConnectionPacket;

import net.createmod.catnip.net.base.BasePacketPayload;
import net.createmod.catnip.net.base.CatnipPacketRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public enum CKLPackets implements BasePacketPayload.PacketTypeProvider {

	CABLE_ADD_CONNECTION(CableAddConnectionPacket.class, CableAddConnectionPacket.STREAM_CODEC),
	CABLE_REMOVE_CONNECTION(CableRemoveConnectionPacket.class, CableRemoveConnectionPacket.STREAM_CODEC),

	AVIONICS_NETWORK_SYNC_REQUEST(AvionicsNetworkSyncRequestPacket.class, AvionicsNetworkSyncRequestPacket.STREAM_CODEC),
	AVIONICS_NETWORK_SYNC(AvionicsNetworkSyncPacket.class, AvionicsNetworkSyncPacket.STREAM_CODEC),

	AVIONICS_ACTIVE_SOURCES_REQUEST(AvionicsActiveSourcesRequestPacket.class, AvionicsActiveSourcesRequestPacket.STREAM_CODEC),
	AVIONICS_ACTIVE_SOURCES(AvionicsActiveSourcesPacket.class, AvionicsActiveSourcesPacket.STREAM_CODEC),

	AVIONICS_CONTROL_INPUT(AvionicsControlInputPacket.class, AvionicsControlInputPacket.STREAM_CODEC);

	private static final String NETWORK_VERSION = "1";

	private final CatnipPacketRegistry.PacketType<?> type;

	<T extends BasePacketPayload> CKLPackets(Class<T> packetClass, StreamCodec<? super RegistryFriendlyByteBuf, T> codec) {
		String name = name().toLowerCase(Locale.ROOT);

		type = new CatnipPacketRegistry.PacketType<>(
			new CustomPacketPayload.Type<>(CreateKineticLift.asResource(name)), packetClass, codec);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends CustomPacketPayload> CustomPacketPayload.Type<T> getType() {
		return (CustomPacketPayload.Type<T>) type.type();
	}

	public static void register() {
		CatnipPacketRegistry registry = new CatnipPacketRegistry(CreateKineticLift.MOD_ID, NETWORK_VERSION);

		for (CKLPackets packet : values())
			registry.registerPacket(packet.type);

		registry.registerAllPackets();
	}
}
