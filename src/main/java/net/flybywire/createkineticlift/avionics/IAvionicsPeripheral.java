package net.flybywire.createkineticlift.avionics;

import java.util.Set;

public interface IAvionicsPeripheral extends IAvionicsActor {

	Set<PeripheralControl> getSupportedControls();

	void receiveControl(ControlInput input);

	default boolean supportsControl(PeripheralControl control) {
		return getSupportedControls().contains(control);
	}
}
