package net.flybywire.createkineticlift.avionics;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import net.flybywire.createkineticlift.avionics.PeripheralControl.InputType;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class AvionicsNetworkManager {

	private static final String CONNECTIONS_KEY = "Connections";
	private static final String SOURCE_KEY = "Source";
	private static final String PERIPHERAL_KEY = "Peripheral";

	public static final int MAX_PERIPHERALS_PER_SOURCE = 64;

	private final Map<Long, Set<Long>> peripheralsBySource = new HashMap<>();
	private final Map<Long, Set<Long>> sourcesByPeripheral = new HashMap<>();
	private final Map<Long, Map<PeripheralControl, Set<Long>>> routeCache = new HashMap<>();
	private final Map<Long, PeripheralInputNode> inputNodes = new HashMap<>();

	private final Runnable dirtyMarker;

	AvionicsNetworkManager(Runnable dirtyMarker) {
		this.dirtyMarker = dirtyMarker;
	}

	public static AvionicsNetworkManager get(ServerLevel level) {
		return AvionicsNetworkSavedData.get(level);
	}

	public ConnectionResult addConnection(ServerLevel level, BlockPos sourcePos, BlockPos peripheralPos) {
		validateConnections(level);

		if (sourcePos.equals(peripheralPos))
			return ConnectionResult.FAIL_SAME_BLOCK;

		if (!(level.getBlockEntity(sourcePos) instanceof IAvionicsSource))
			return ConnectionResult.FAIL_INVALID_SOURCE;

		BlockEntity blockEntity = level.getBlockEntity(peripheralPos);
		if (!(blockEntity instanceof IAvionicsPeripheral peripheral))
			return ConnectionResult.FAIL_NOT_PERIPHERAL;

		if (peripheral.getSupportedControls().isEmpty())
			return ConnectionResult.FAIL_NO_CONTROLS;

		long sourceKey = sourcePos.asLong();
		long peripheralKey = peripheralPos.asLong();

		Set<Long> peripherals = peripheralsBySource.computeIfAbsent(sourceKey, ignored -> new HashSet<>());

		if (peripherals.size() >= MAX_PERIPHERALS_PER_SOURCE)
			return ConnectionResult.FAIL_TOO_MANY_PERIPHERALS;

		if (peripherals.contains(peripheralKey))
			return ConnectionResult.FAIL_EXISTS;

		addConnectionInternal(sourceKey, peripheralKey);
		invalidateRoutesFromSource(sourceKey);
		dirtyMarker.run();

		return ConnectionResult.OK;
	}

	public boolean removeConnection(ServerLevel level, BlockPos sourcePos, BlockPos peripheralPos) {
		validateConnections(level);

		long sourceKey = sourcePos.asLong();
		long peripheralKey = peripheralPos.asLong();

		if (!hasConnectionRaw(sourcePos, peripheralPos))
			return false;

		removeConnectionInternal(level, sourceKey, peripheralKey);
		invalidateRoutesFromSource(sourceKey);
		dirtyMarker.run();

		return true;
	}

	private boolean hasConnectionRaw(BlockPos sourcePos, BlockPos peripheralPos) {
		return peripheralsBySource
			.getOrDefault(sourcePos.asLong(), Set.of())
			.contains(peripheralPos.asLong());
	}

	public Set<BlockPos> getConnectionsFrom(ServerLevel level, BlockPos sourcePos) {
		validateConnections(level);
		return getConnectionsFromRaw(sourcePos);
	}

	private Set<BlockPos> getConnectionsFromRaw(BlockPos sourcePos) {
		Set<Long> peripheralKeys = peripheralsBySource.get(sourcePos.asLong());

		if (peripheralKeys == null || peripheralKeys.isEmpty())
			return Set.of();

		Set<BlockPos> result = new HashSet<>();

		for (long peripheralKey : peripheralKeys)
			result.add(BlockPos.of(peripheralKey));

		return Set.copyOf(result);
	}

	public Set<BlockPos> getActiveSources(ServerLevel level) {
		validateConnections(level);

		if (peripheralsBySource.isEmpty())
			return Set.of();

		Set<BlockPos> result = new HashSet<>();

		for (Map.Entry<Long, Set<Long>> entry : peripheralsBySource.entrySet()) {
			if (entry.getValue().isEmpty())
				continue;

			BlockPos sourcePos = BlockPos.of(entry.getKey());

			if (!level.isLoaded(sourcePos))
				continue;

			if (level.getBlockEntity(sourcePos) instanceof IAvionicsSource)
				result.add(sourcePos);
		}

		return Set.copyOf(result);
	}

	public void setSourceInput(ServerLevel level, BlockPos sourcePos, ControlInput input) {
		validateConnections(level);

		long sourceKey = sourcePos.asLong();
		Set<Long> route = getRoute(level, sourceKey, input.control());

		if (route.isEmpty())
			return;

		if (input.control().getInputType() == InputType.PRESS) {
			if (input.value() <= 0.0f)
				return;

			for (long peripheralKey : route)
				sendToPeripheral(level, peripheralKey, input);

			return;
		}

		for (long peripheralKey : route) {
			PeripheralInputNode node = inputNodes.computeIfAbsent(peripheralKey, ignored -> new PeripheralInputNode());

			ControlUpdate update = node.setInput(sourceKey, input);

			if (update.changed())
				sendToPeripheral(level, peripheralKey, new ControlInput(input.control(), update.effectiveValue()));

			if (node.isEmpty())
				inputNodes.remove(peripheralKey);
		}
	}

	public void clearSourceInputs(ServerLevel level, BlockPos sourcePos) {
		validateConnections(level);

		long sourceKey = sourcePos.asLong();
		Set<Long> peripherals = peripheralsBySource.get(sourceKey);

		if (peripherals == null)
			return;

		for (long peripheralKey : peripherals)
			clearSourceFromPeripheral(level, sourceKey, peripheralKey);
	}

	public void invalidatePeripheral(ServerLevel level, BlockPos peripheralPos) {
		validateConnections(level);

		long peripheralKey = peripheralPos.asLong();
		Set<Long> sources = sourcesByPeripheral.get(peripheralKey);

		if (sources == null)
			return;

		for (long sourceKey : sources)
			invalidateRoutesFromSource(sourceKey);
	}

	private void validateConnections(ServerLevel level) {
		boolean changed = false;

		Iterator<Map.Entry<Long, Set<Long>>> sourceIterator = peripheralsBySource.entrySet().iterator();

		while (sourceIterator.hasNext()) {
			Map.Entry<Long, Set<Long>> sourceEntry = sourceIterator.next();
			long sourceKey = sourceEntry.getKey();

			if (isInvalidLoadedSource(level, sourceKey)) {
				for (long peripheralKey : sourceEntry.getValue()) {
					removeSourceReference(peripheralKey, sourceKey);
					clearSourceFromPeripheral(level, sourceKey, peripheralKey);
				}

				sourceIterator.remove();
				routeCache.remove(sourceKey);
				changed = true;
				continue;
			}

			Iterator<Long> peripheralIterator = sourceEntry.getValue().iterator();
			boolean sourceChanged = false;

			while (peripheralIterator.hasNext()) {
				long peripheralKey = peripheralIterator.next();

				if (!isInvalidLoadedPeripheral(level, peripheralKey))
					continue;

				peripheralIterator.remove();
				removeSourceReference(peripheralKey, sourceKey);
				clearSourceFromPeripheral(level, sourceKey, peripheralKey);
				sourceChanged = true;
				changed = true;
			}

			if (sourceEntry.getValue().isEmpty()) {
				sourceIterator.remove();
				routeCache.remove(sourceKey);
				changed = true;
			} else if (sourceChanged) {
				invalidateRoutesFromSource(sourceKey);
			}
		}

		Iterator<Map.Entry<Long, Set<Long>>> peripheralIterator = sourcesByPeripheral.entrySet().iterator();

		while (peripheralIterator.hasNext()) {
			Map.Entry<Long, Set<Long>> peripheralEntry = peripheralIterator.next();
			long peripheralKey = peripheralEntry.getKey();

			peripheralEntry.getValue().removeIf(sourceKey -> !peripheralsBySource
				.getOrDefault(sourceKey, Set.of())
				.contains(peripheralKey));

			if (isInvalidLoadedPeripheral(level, peripheralKey))
				clearPeripheralInputs(level, peripheralKey);

			if (isInvalidLoadedPeripheral(level, peripheralKey) || peripheralEntry.getValue().isEmpty()) {
				peripheralIterator.remove();
				changed = true;
			}
		}

		if (changed)
			dirtyMarker.run();
	}

	private boolean isInvalidLoadedSource(ServerLevel level, long sourceKey) {
		BlockPos sourcePos = BlockPos.of(sourceKey);
		return level.isLoaded(sourcePos) && !(level.getBlockEntity(sourcePos) instanceof IAvionicsSource);
	}

	private boolean isInvalidLoadedPeripheral(ServerLevel level, long peripheralKey) {
		BlockPos peripheralPos = BlockPos.of(peripheralKey);
		return level.isLoaded(peripheralPos) && !(level.getBlockEntity(peripheralPos) instanceof IAvionicsPeripheral);
	}

	private void addConnectionInternal(long sourceKey, long peripheralKey) {
		peripheralsBySource
			.computeIfAbsent(sourceKey, ignored -> new HashSet<>())
			.add(peripheralKey);

		sourcesByPeripheral
			.computeIfAbsent(peripheralKey, ignored -> new HashSet<>())
			.add(sourceKey);
	}

	private void removeConnectionInternal(ServerLevel level, long sourceKey, long peripheralKey) {
		Set<Long> peripherals = peripheralsBySource.get(sourceKey);

		if (peripherals != null) {
			peripherals.remove(peripheralKey);

			if (peripherals.isEmpty())
				peripheralsBySource.remove(sourceKey);
		}

		removeSourceReference(peripheralKey, sourceKey);
		clearSourceFromPeripheral(level, sourceKey, peripheralKey);
	}

	private void removeSourceReference(long peripheralKey, long sourceKey) {
		Set<Long> sources = sourcesByPeripheral.get(peripheralKey);

		if (sources == null)
			return;

		sources.remove(sourceKey);

		if (sources.isEmpty())
			sourcesByPeripheral.remove(peripheralKey);
	}

	private Set<Long> getRoute(ServerLevel level, long sourceKey, PeripheralControl control) {
		Map<PeripheralControl, Set<Long>> sourceRoutes = routeCache.get(sourceKey);

		if (sourceRoutes != null) {
			Set<Long> cached = sourceRoutes.get(control);

			if (cached != null)
				return cached;
		}

		Set<Long> resolved = new HashSet<>();
		boolean complete = true;

		for (long peripheralKey : peripheralsBySource.getOrDefault(sourceKey, Set.of())) {
			BlockEntity blockEntity = level.getBlockEntity(BlockPos.of(peripheralKey));

			if (!(blockEntity instanceof IAvionicsPeripheral peripheral)) {
				complete = false;
				continue;
			}

			if (peripheral.supportsControl(control))
				resolved.add(peripheralKey);
		}

		Set<Long> result = Set.copyOf(resolved);

		if (complete) {
			routeCache
				.computeIfAbsent(sourceKey, ignored -> new EnumMap<>(PeripheralControl.class))
				.put(control, result);
		}

		return result;
	}

	private void sendToPeripheral(ServerLevel level, long peripheralKey, ControlInput input) {
		BlockPos peripheralPos = BlockPos.of(peripheralKey);
		BlockEntity blockEntity = level.getBlockEntity(peripheralPos);

		if (!(blockEntity instanceof IAvionicsPeripheral peripheral)) {
			invalidatePeripheral(level, peripheralPos);
			return;
		}

		if (!peripheral.supportsControl(input.control())) {
			invalidatePeripheral(level, peripheralPos);
			return;
		}

		peripheral.receiveControl(input);
	}

	private void clearSourceFromPeripheral(ServerLevel level, long sourceKey, long peripheralKey) {
		PeripheralInputNode node = inputNodes.get(peripheralKey);

		if (node == null)
			return;

		Map<PeripheralControl, Float> changedControls = node.clearSource(sourceKey);

		for (Map.Entry<PeripheralControl, Float> entry : changedControls.entrySet())
			sendToPeripheral(level, peripheralKey, new ControlInput(entry.getKey(), entry.getValue()));

		if (node.isEmpty())
			inputNodes.remove(peripheralKey);
	}

	private void clearPeripheralInputs(ServerLevel level, long peripheralKey) {
		PeripheralInputNode node = inputNodes.remove(peripheralKey);

		if (node == null)
			return;

		for (Map.Entry<PeripheralControl, Float> entry : node.clearAll().entrySet())
			sendToPeripheral(level, peripheralKey, new ControlInput(entry.getKey(), entry.getValue()));
	}

	private void invalidateRoutesFromSource(long sourceKey) {
		routeCache.remove(sourceKey);
	}

	public CompoundTag save(CompoundTag tag) {
		ListTag connections = new ListTag();

		for (Map.Entry<Long, Set<Long>> sourceEntry : peripheralsBySource.entrySet()) {
			for (long peripheralKey : sourceEntry.getValue()) {
				CompoundTag connection = new CompoundTag();
				connection.putLong(SOURCE_KEY, sourceEntry.getKey());
				connection.putLong(PERIPHERAL_KEY, peripheralKey);
				connections.add(connection);
			}
		}

		tag.put(CONNECTIONS_KEY, connections);
		return tag;
	}

	public void load(CompoundTag tag) {
		peripheralsBySource.clear();
		sourcesByPeripheral.clear();
		routeCache.clear();
		inputNodes.clear();

		if (!tag.contains(CONNECTIONS_KEY, Tag.TAG_LIST))
			return;

		ListTag connections = tag.getList(CONNECTIONS_KEY, Tag.TAG_COMPOUND);

		for (Tag entry : connections) {
			if (!(entry instanceof CompoundTag connection))
				continue;

			if (!connection.contains(SOURCE_KEY, Tag.TAG_LONG)
				|| !connection.contains(PERIPHERAL_KEY, Tag.TAG_LONG))
				continue;

			long sourceKey = connection.getLong(SOURCE_KEY);
			long peripheralKey = connection.getLong(PERIPHERAL_KEY);

			if (sourceKey == peripheralKey)
				continue;

			addConnectionInternal(sourceKey, peripheralKey);
		}
	}

	public enum ConnectionResult {
		OK,
		FAIL_EXISTS,
		FAIL_INVALID_SOURCE,
		FAIL_NOT_PERIPHERAL,
		FAIL_NO_CONTROLS,
		FAIL_TOO_MANY_PERIPHERALS,
		FAIL_SAME_BLOCK;

		public boolean isSuccess() {
			return this == OK;
		}
	}

	private static final class PeripheralInputNode {

		private final Map<PeripheralControl, Map<Long, InputSample>> inputs = new EnumMap<>(PeripheralControl.class);
		private final Map<PeripheralControl, Float> effectiveValues = new EnumMap<>(PeripheralControl.class);

		private long sequence;

		public ControlUpdate setInput(long sourceKey, ControlInput input) {
			PeripheralControl control = input.control();

			if (control.getInputType() == InputType.PRESS)
				return new ControlUpdate(true, input.value());

			float previousValue = effectiveValues.getOrDefault(control, 0.0f);
			Map<Long, InputSample> controlInputs = inputs.computeIfAbsent(control, ignored -> new HashMap<>());

			switch (control.getInputType()) {
				case HELD -> {
					float value = Math.max(0.0f, input.value());

					if (value <= 0.0f)
						controlInputs.remove(sourceKey);
					else
						controlInputs.put(sourceKey, new InputSample(value, ++sequence));
				}
				case AXIS -> controlInputs.put(sourceKey, new InputSample(input.value(), ++sequence));
				case PRESS -> {
				}
			}

			float effectiveValue = computeEffectiveValue(control, controlInputs);

			if (controlInputs.isEmpty()) {
				inputs.remove(control);
				effectiveValues.remove(control);
			} else {
				effectiveValues.put(control, effectiveValue);
			}

			return new ControlUpdate(Float.compare(previousValue, effectiveValue) != 0, effectiveValue);
		}

		public Map<PeripheralControl, Float> clearSource(long sourceKey) {
			Map<PeripheralControl, Float> changed = new EnumMap<>(PeripheralControl.class);
			Iterator<Map.Entry<PeripheralControl, Map<Long, InputSample>>> iterator = inputs.entrySet().iterator();

			while (iterator.hasNext()) {
				Map.Entry<PeripheralControl, Map<Long, InputSample>> entry = iterator.next();

				PeripheralControl control = entry.getKey();
				Map<Long, InputSample> controlInputs = entry.getValue();

				float previousValue = effectiveValues.getOrDefault(control, 0.0f);

				if (controlInputs.remove(sourceKey) == null)
					continue;

				float effectiveValue = computeEffectiveValue(control, controlInputs);

				if (controlInputs.isEmpty()) {
					iterator.remove();
					effectiveValues.remove(control);
				} else {
					effectiveValues.put(control, effectiveValue);
				}

				if (Float.compare(previousValue, effectiveValue) != 0)
					changed.put(control, effectiveValue);
			}

			return changed;
		}

		public Map<PeripheralControl, Float> clearAll() {
			Map<PeripheralControl, Float> changed = new EnumMap<>(PeripheralControl.class);

			for (Map.Entry<PeripheralControl, Float> entry : effectiveValues.entrySet()) {
				if (Float.compare(entry.getValue(), 0.0f) != 0)
					changed.put(entry.getKey(), 0.0f);
			}

			inputs.clear();
			effectiveValues.clear();

			return changed;
		}

		public boolean isEmpty() {
			return inputs.isEmpty();
		}

		private float computeEffectiveValue(PeripheralControl control, Map<Long, InputSample> controlInputs) {
			if (controlInputs.isEmpty())
				return 0.0f;

			return switch (control.getInputType()) {
				case HELD -> {
					float maximum = 0.0f;

					for (InputSample sample : controlInputs.values())
						maximum = Math.max(maximum, sample.value());

					yield maximum;
				}
				case AXIS -> {
					InputSample latest = null;

					for (InputSample sample : controlInputs.values()) {
						if (latest == null || sample.sequence() > latest.sequence())
							latest = sample;
					}

					yield latest == null ? 0.0f : latest.value();
				}
				case PRESS -> 0.0f;
			};
		}
	}

	private record InputSample(float value, long sequence) {
	}

	private record ControlUpdate(boolean changed, float effectiveValue) {
	}
}
