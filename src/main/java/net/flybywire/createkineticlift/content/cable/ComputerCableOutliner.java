package net.flybywire.createkineticlift.content.cable;

import java.util.Set;

import net.flybywire.createkineticlift.CreateKineticLift;
import net.flybywire.createkineticlift.avionics.AvionicsHelper;
import net.flybywire.createkineticlift.avionics.IAvionicsActor;
import net.flybywire.createkineticlift.avionics.IAvionicsPeripheral;
import net.flybywire.createkineticlift.avionics.IAvionicsSource;

import net.createmod.catnip.data.Pair;
import net.createmod.catnip.outliner.Outliner;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = CreateKineticLift.MOD_ID, value = Dist.CLIENT)
public final class ComputerCableOutliner {

	private static final int ACTIVE_SOURCE_COLOR = 0x1E5FAF;
	private static final int SELECTED_SOURCE_COLOR = 0x6EC6FF;
	private static final int LINKED_PERIPHERAL_COLOR = 0x55FF88;
	private static final int HOVERED_PERIPHERAL_COLOR = 0xFFE45C;
	private static final int WIRE_COLOR = 0x55FF88;

	private ComputerCableOutliner() {
	}

	@SubscribeEvent
	public static void onClientTick(ClientTickEvent.Post event) {
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;
		Level level = minecraft.level;

		if (player == null || level == null)
			return;

		ItemStack mainHand = player.getMainHandItem();

		if (!(mainHand.getItem() instanceof ComputerCableItem))
			return;

		if (!ComputerCableEditingState.isEditing()) {
			drawActiveSources(level);
			return;
		}

		BlockPos selectedSource = ComputerCableEditingState.getSelectedSource();

		if (selectedSource == null || !isSourceStillValid(level, selectedSource))
			return;

		drawActorBox(level, selectedSource, SELECTED_SOURCE_COLOR, Pair.of("computerCableSelectedSource", selectedSource));

		if (ComputerCableEditingState.isAwaitingSync())
			return;

		Set<BlockPos> connections = ComputerCableEditingState.getConfirmedConnections();

		for (BlockPos peripheralPos : connections)
			drawConnection(level, selectedSource, peripheralPos);

		drawHoveredPeripheral(minecraft, level, connections);
	}

	private static void drawActiveSources(Level level) {
		for (BlockPos sourcePos : ComputerCableSourceOutlineState.getActiveSources()) {
			if (!isSourceStillValid(level, sourcePos))
				continue;

			drawActorBox(level, sourcePos, ACTIVE_SOURCE_COLOR, Pair.of("computerCableActiveSource", sourcePos));
		}
	}

	private static void drawConnection(Level level, BlockPos sourcePos, BlockPos peripheralPos) {
		if (!isSourceStillValid(level, sourcePos) || !isPeripheralStillValid(level, peripheralPos))
			return;

		AABB sourceBounds = getActorBounds(level, sourcePos);
		AABB peripheralBounds = getActorBounds(level, peripheralPos);

		drawBox(peripheralBounds, LINKED_PERIPHERAL_COLOR, Pair.of("computerCableLinkedPeripheral", peripheralPos));

		Outliner.getInstance()
			.showLine(Pair.of("computerCableWire", Pair.of(sourcePos, peripheralPos)), sourceBounds.getCenter(), peripheralBounds.getCenter())
			.colored(WIRE_COLOR)
			.lineWidth(1 / 16f);
	}

	private static void drawHoveredPeripheral(Minecraft minecraft, Level level, Set<BlockPos> connections) {
		if (!(minecraft.hitResult instanceof BlockHitResult hitResult))
			return;

		if (hitResult.getType() != HitResult.Type.BLOCK)
			return;

		BlockPos peripheralPos = AvionicsHelper.getPeripheralBlockEntityPos(level, hitResult.getBlockPos());

		if (peripheralPos == null || connections.contains(peripheralPos))
			return;

		drawActorBox(level, peripheralPos, HOVERED_PERIPHERAL_COLOR, Pair.of("computerCableHoveredPeripheral", peripheralPos));
	}

	private static void drawActorBox(Level level, BlockPos actorPos, int color, Object slot) {
		drawBox(getActorBounds(level, actorPos), color, slot);
	}

	private static void drawBox(AABB box, int color, Object slot) {
		Outliner.getInstance()
			.showAABB(slot, box)
			.colored(color)
			.lineWidth(1 / 16f);
	}

	private static boolean isSourceStillValid(Level level, BlockPos sourcePos) {
		return level.getBlockEntity(sourcePos) instanceof IAvionicsSource;
	}

	private static boolean isPeripheralStillValid(Level level, BlockPos peripheralPos) {
		return level.getBlockEntity(peripheralPos) instanceof IAvionicsPeripheral;
	}

	private static AABB getActorBounds(Level level, BlockPos actorPos) {
		BlockEntity blockEntity = level.getBlockEntity(actorPos);

		if (blockEntity instanceof IAvionicsActor actor)
			return actor.getActorBounds();

		return new AABB(actorPos);
	}
}
