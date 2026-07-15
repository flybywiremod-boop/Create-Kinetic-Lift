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

public record CableRemoveConnectionPacket(BlockPos sourcePos, BlockPos clickedPos,
										  int sessionId) implements ServerboundPacketPayload {

	public static final StreamCodec<ByteBuf, CableRemoveConnectionPacket> STREAM_CODEC =
		StreamCodec.composite(BlockPos.STREAM_CODEC, CableRemoveConnectionPacket::sourcePos,
			BlockPos.STREAM_CODEC, CableRemoveConnectionPacket::clickedPos,
			ByteBufCodecs.VAR_INT, CableRemoveConnectionPacket::sessionId,
			CableRemoveConnectionPacket::new);

	@Override
	public void handle(ServerPlayer player) {
		ServerLevel level = player.serverLevel();

		if (!isValidSource(level)) {
			sync(player, level);
			return;
		}

		if (!player.canInteractWithBlock(clickedPos, 1)) {
			sync(player, level);
			return;
		}

		BlockPos peripheralPos = AvionicsHelper.getPeripheralBlockEntityPos(level, clickedPos);

		if (peripheralPos != null)
			AvionicsNetworkManager.get(level).removeConnection(level, sourcePos, peripheralPos);

		sync(player, level);
	}

	private boolean isValidSource(ServerLevel level) {
		return sourcePos.equals(AvionicsHelper.getSourceBlockEntityPos(level, sourcePos));
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
		return CKLPackets.CABLE_REMOVE_CONNECTION;
	}
}
