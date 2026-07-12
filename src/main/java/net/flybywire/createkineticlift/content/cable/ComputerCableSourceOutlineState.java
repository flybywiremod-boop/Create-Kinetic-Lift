package net.flybywire.createkineticlift.content.cable;

import java.util.HashSet;
import java.util.Set;

import net.flybywire.createkineticlift.network.avionics.AvionicsActiveSourcesRequestPacket;

import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;

public final class ComputerCableSourceOutlineState {

	private static final int REQUEST_INTERVAL = 20;

	private static final Set<BlockPos> activeSources = new HashSet<>();

	private static int requestCooldown;
	private static boolean wasHoldingCable;

	private ComputerCableSourceOutlineState() {
	}

	public static void tick(LocalPlayer player) {
		if (!(player.getMainHandItem().getItem() instanceof ComputerCableItem)) {
			clear();
			return;
		}

		if (!wasHoldingCable) {
			wasHoldingCable = true;
			requestCooldown = 0;
		}

		if (requestCooldown > 0) {
			requestCooldown--;
			return;
		}

		requestCooldown = REQUEST_INTERVAL;
		CatnipServices.NETWORK.sendToServer(new AvionicsActiveSourcesRequestPacket());
	}

	public static void setActiveSources(Set<BlockPos> sources) {
		activeSources.clear();

		for (BlockPos source : sources)
			activeSources.add(source.immutable());
	}

	public static Set<BlockPos> getActiveSources() {
		return Set.copyOf(activeSources);
	}

	public static void clear() {
		activeSources.clear();
		requestCooldown = 0;
		wasHoldingCable = false;
	}
}
