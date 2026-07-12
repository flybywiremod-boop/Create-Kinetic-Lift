package net.flybywire.createkineticlift.content.cable;

import java.util.HashSet;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;

public final class ComputerCableEditingState {

	private static final Set<BlockPos> confirmedConnections = new HashSet<>();

	@Nullable
	private static BlockPos selectedSource;

	private static int nextSessionId;
	private static int activeSessionId;

	private static boolean awaitingSync;

	private ComputerCableEditingState() {
	}

	public static int beginEditing(BlockPos sourcePos) {
		activeSessionId = ++nextSessionId;
		selectedSource = sourcePos.immutable();

		confirmedConnections.clear();
		awaitingSync = true;

		return activeSessionId;
	}

	public static int refresh() {
		if (selectedSource == null)
			return 0;

		awaitingSync = true;
		return activeSessionId;
	}

	public static boolean isEditing() {
		return selectedSource != null;
	}

	public static boolean isAwaitingSync() {
		return awaitingSync;
	}

	public static boolean canEdit() {
		return isEditing() && !awaitingSync;
	}

	@Nullable
	public static BlockPos getSelectedSource() {
		return selectedSource;
	}

	public static boolean isSelectedSource(BlockPos sourcePos) {
		return selectedSource != null && selectedSource.equals(sourcePos);
	}

	public static boolean hasConnection(BlockPos peripheralPos) {
		return confirmedConnections.contains(peripheralPos);
	}

	public static Set<BlockPos> getConfirmedConnections() {
		return Set.copyOf(confirmedConnections);
	}

	public static void applySync(BlockPos sourcePos, int sessionId, Set<BlockPos> connections) {
		if (selectedSource == null || !selectedSource.equals(sourcePos))
			return;
		if (activeSessionId != sessionId)
			return;

		confirmedConnections.clear();

		for (BlockPos connection : connections)
			confirmedConnections.add(connection.immutable());

		awaitingSync = false;
	}

	public static void reset() {
		selectedSource = null;
		activeSessionId = 0;
		awaitingSync = false;
		confirmedConnections.clear();
	}
}
