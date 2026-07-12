package net.flybywire.createkineticlift.avionics;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public enum PeripheralControl {

	THROTTLE_UP(InputType.HELD),
	THROTTLE_DOWN(InputType.HELD),
	REVERSE_THRUST_TOGGLE(InputType.PRESS),
	ENGINE_TOGGLE(InputType.PRESS);

	private final InputType inputType;

	PeripheralControl(InputType inputType) {
		this.inputType = inputType;
	}

	public InputType getInputType() {
		return inputType;
	}

	public enum InputType {
		HELD,
		PRESS,
		AXIS
	}

	public static final StreamCodec<ByteBuf, PeripheralControl> STREAM_CODEC =
		ByteBufCodecs.idMapper(id -> values()[id], PeripheralControl::ordinal);
}
