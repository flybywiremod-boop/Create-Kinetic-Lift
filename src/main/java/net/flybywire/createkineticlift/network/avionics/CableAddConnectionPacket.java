package net.flybywire.createkineticlift.network.avionics;

import java.util.Set;

import net.flybywire.createkineticlift.avionics.AvionicsHelper;
import net.flybywire.createkineticlift.avionics.AvionicsNetworkManager;
import net.flybywire.createkineticlift.network.CKLPackets;

import io.netty.buffer.ByteBuf;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public record CableAddConnectionPacket(BlockPos sourcePos, BlockPos clickedPos, int sessionId)
	implements ServerboundPacketPayload {

	public static final StreamCodec<ByteBuf, CableAddConnectionPacket> STREAM_CODEC =
		StreamCodec.composite(BlockPos.STREAM_CODEC, CableAddConnectionPacket::sourcePos,
			BlockPos.STREAM_CODEC, CableAddConnectionPacket::clickedPos,
			ByteBufCodecs.VAR_INT, CableAddConnectionPacket::sessionId,
			CableAddConnectionPacket::new);

	@Override
	public void handle(ServerPlayer player) {
		ServerLevel level = player.serverLevel();

		if (!isValidSource(player, level)) {
			sync(player, level);
			return;
		}

		if (!player.canInteractWithBlock(clickedPos, 1)) {
			sync(player, level);
			return;
		}

		BlockPos peripheralPos = AvionicsHelper.getPeripheralBlockEntityPos(level, clickedPos);

		if (peripheralPos != null && !sourcePos.equals(peripheralPos))
			AvionicsNetworkManager.get(level).addConnection(level, sourcePos, peripheralPos);

		sync(player, level);
	}

	private boolean isValidSource(ServerPlayer player, ServerLevel level) {
		return player.canInteractWithBlock(sourcePos, 1)
			&& sourcePos.equals(AvionicsHelper.getSourceBlockEntityPos(level, sourcePos));
	}

	private void sync(ServerPlayer player, ServerLevel level) {
		AvionicsNetworkManager manager = AvionicsNetworkManager.get(level);

		Set<BlockPos> connections = AvionicsHelper.getSourceBlockEntityPos(level, sourcePos) == null
			? Set.of()
			: manager.getConnectionsFrom(level, sourcePos);

		AvionicsNetworkSyncPacket.sendTo(player, sourcePos, sessionId, connections);
		AvionicsActiveSourcesPacket.sendTo(player, manager.getActiveSources(level));
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return CKLPackets.CABLE_ADD_CONNECTION;
	}
}
