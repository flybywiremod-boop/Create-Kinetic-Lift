package net.flybywire.createkineticlift.content.cable;

import net.flybywire.createkineticlift.CreateKineticLift;
import net.flybywire.createkineticlift.avionics.AvionicsHelper;
import net.flybywire.createkineticlift.avionics.IAvionicsSource;
import net.flybywire.createkineticlift.network.avionics.AvionicsNetworkSyncRequestPacket;
import net.flybywire.createkineticlift.network.avionics.CableAddConnectionPacket;
import net.flybywire.createkineticlift.network.avionics.CableRemoveConnectionPacket;

import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

@EventBusSubscriber(modid = CreateKineticLift.MOD_ID, value = Dist.CLIENT)
public final class ComputerCableHandler {

	private static final int RIGHT_CLICK_COOLDOWN = 5;

	private ComputerCableHandler() {
	}

	@SubscribeEvent
	public static void onClientTick(ClientTickEvent.Post event) {
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;
		Level level = minecraft.level;

		if (player == null || level == null) {
			resetClientState();
			return;
		}

		if (!(player.getMainHandItem().getItem() instanceof ComputerCableItem)) {
			resetClientState();
			return;
		}

		ComputerCableSourceOutlineState.tick(player);

		BlockPos selectedSource = ComputerCableEditingState.getSelectedSource();

		if (selectedSource == null)
			return;

		if (!level.isLoaded(selectedSource) || !(level.getBlockEntity(selectedSource) instanceof IAvionicsSource))
			ComputerCableEditingState.reset();
	}

	@SubscribeEvent
	public static void onLevelUnload(LevelEvent.Unload event) {
		resetClientState();
	}

	private static void resetClientState() {
		ComputerCableEditingState.reset();
		ComputerCableSourceOutlineState.clear();
	}

	@SubscribeEvent
	public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		Item heldItem = event.getItemStack().getItem();

		if (!(heldItem instanceof ComputerCableItem))
			return;

		event.setUseBlock(TriState.FALSE);

		if (event.getSide().isServer())
			return;

		if (event.getHand() != InteractionHand.MAIN_HAND)
			return;

		Player player = event.getEntity();

		if (player.isSpectator())
			return;

		if (player.getCooldowns().isOnCooldown(heldItem)) {
			event.setCancellationResult(InteractionResult.CONSUME);
			event.setCanceled(true);
			return;
		}

		CableInteractionResult result = handleRightClick(event.getLevel(), event.getPos());

		if (result.shouldSwing()) {
			player.swing(event.getHand());
			player.getCooldowns().addCooldown(heldItem, RIGHT_CLICK_COOLDOWN);
		}

		event.setCancellationResult(InteractionResult.CONSUME);
		event.setCanceled(true);
	}

	private static CableInteractionResult handleRightClick(Level level, BlockPos clickedPos) {
		BlockPos sourcePos = AvionicsHelper.getSourceBlockEntityPos(level, clickedPos);

		if (sourcePos != null)
			return handleSource(sourcePos);

		BlockPos peripheralPos = AvionicsHelper.getPeripheralBlockEntityPos(level, clickedPos);

		if (peripheralPos != null)
			return handleConnection(clickedPos, peripheralPos);

		if (ComputerCableEditingState.isEditing())
			ComputerCableEditingState.reset();

		return CableInteractionResult.NO_ACTION;
	}

	private static CableInteractionResult handleSource(BlockPos sourcePos) {
		if (ComputerCableEditingState.isSelectedSource(sourcePos))
			ComputerCableEditingState.reset();
		else {
			int sessionId = ComputerCableEditingState.beginEditing(sourcePos);
			CatnipServices.NETWORK.sendToServer(new AvionicsNetworkSyncRequestPacket(sourcePos, sessionId));
		}
		return CableInteractionResult.ACTION;
	}

	private static CableInteractionResult handleConnection(BlockPos clickedPos, BlockPos peripheralPos) {
		if (!ComputerCableEditingState.canEdit())
			return CableInteractionResult.NO_ACTION;

		BlockPos selectedSource = ComputerCableEditingState.getSelectedSource();

		if (selectedSource == null)
			return CableInteractionResult.NO_ACTION;

		int sessionId = ComputerCableEditingState.refresh();

		if (ComputerCableEditingState.hasConnection(peripheralPos))
			CatnipServices.NETWORK.sendToServer(new CableRemoveConnectionPacket(selectedSource, clickedPos, sessionId));
		else
			CatnipServices.NETWORK.sendToServer(new CableAddConnectionPacket(selectedSource, clickedPos, sessionId));

		return CableInteractionResult.ACTION;
	}

	private enum CableInteractionResult {
		NO_ACTION(false),
		ACTION(true);

		private final boolean swing;

		CableInteractionResult(boolean swing) {
			this.swing = swing;
		}

		public boolean shouldSwing() {
			return swing;
		}
	}
}
