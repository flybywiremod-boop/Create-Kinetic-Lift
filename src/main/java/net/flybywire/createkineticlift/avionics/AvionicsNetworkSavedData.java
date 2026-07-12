package net.flybywire.createkineticlift.avionics;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public final class AvionicsNetworkSavedData extends SavedData {

	private static final String DATA_NAME = "createkineticlift_cable_network";

	private final AvionicsNetworkManager manager;

	private AvionicsNetworkSavedData() {
		manager = new AvionicsNetworkManager(this::setDirty);
	}

	public static SavedData.Factory<AvionicsNetworkSavedData> factory() {
		return new SavedData.Factory<>(AvionicsNetworkSavedData::new, AvionicsNetworkSavedData::load);
	}

	public static AvionicsNetworkManager get(ServerLevel level) {
		AvionicsNetworkSavedData data = level.getDataStorage().computeIfAbsent(factory(), DATA_NAME);

		return data.manager;
	}

	private static AvionicsNetworkSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
		AvionicsNetworkSavedData data = new AvionicsNetworkSavedData();
		data.manager.load(tag);
		return data;
	}

	@Override
	public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
		return manager.save(tag);
	}
}
