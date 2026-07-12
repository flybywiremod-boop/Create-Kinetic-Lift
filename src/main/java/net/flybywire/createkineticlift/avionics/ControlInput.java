package net.flybywire.createkineticlift.avionics;

public record ControlInput(PeripheralControl control, float value) {

	public ControlInput {
		if (control == null)
			throw new IllegalArgumentException("Control cannot be null");

		if (!Float.isFinite(value))
			throw new IllegalArgumentException("Control value must be finite");

		value = Math.max(-1.0f, Math.min(1.0f, value));
	}
}
