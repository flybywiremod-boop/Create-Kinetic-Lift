package net.flybywire.createkineticlift.content.controlseat;

import net.flybywire.createkineticlift.CreateKineticLift;
import net.flybywire.createkineticlift.avionics.PeripheralControl;
import net.flybywire.createkineticlift.network.avionics.AvionicsControlInputPacket;
import net.flybywire.createkineticlift.registries.CKLKeys;

import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = CreateKineticLift.MOD_ID, value = Dist.CLIENT)
public final class ControlSeatInputHandler {

	private static BlockPos activeSourcePos;
	private static float lastThrottleUp = -1.0f;
	private static float lastThrottleDown = -1.0f;
	private static boolean lastReverseTogglePressed;
	private static boolean lastEngineTogglePressed;

	private ControlSeatInputHandler() {
	}

	@SubscribeEvent
	public static void onClientTick(ClientTickEvent.Post event) {
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;

		if (player == null) {
			reset();
			return;
		}

		if (!(player.getVehicle() instanceof ControlSeatEntity seat)) {
			reset();
			return;
		}

		BlockPos sourcePos = seat.getSourcePos();

		if (sourcePos.equals(BlockPos.ZERO)) {
			reset();
			return;
		}

		if (!sourcePos.equals(activeSourcePos)) {
			activeSourcePos = sourcePos;
			lastThrottleUp = -1.0f;
			lastThrottleDown = -1.0f;
			lastReverseTogglePressed = false;
		}

		boolean throttleUpPressed = CKLKeys.THROTTLE_UP.isPressed();
		boolean throttleDownPressed = CKLKeys.THROTTLE_DOWN.isPressed();
		boolean reverseTogglePressed = throttleUpPressed && throttleDownPressed;
		boolean engineTogglePressed = CKLKeys.ENGINE_TOGGLE.isPressed();

		handleEngineToggle(sourcePos, engineTogglePressed);
		handleReverseToggle(sourcePos, reverseTogglePressed);
		handleThrottle(sourcePos, throttleUpPressed, throttleDownPressed);
	}

	private static void handleEngineToggle(BlockPos sourcePos, boolean engineTogglePressed) {
		if (engineTogglePressed && !lastEngineTogglePressed)
			send(sourcePos, PeripheralControl.ENGINE_TOGGLE, 1.0f);

		if (!engineTogglePressed && lastEngineTogglePressed)
			send(sourcePos, PeripheralControl.ENGINE_TOGGLE, 0.0f);

		lastEngineTogglePressed = engineTogglePressed;
	}

	private static void handleReverseToggle(BlockPos sourcePos, boolean reverseTogglePressed) {
		if (reverseTogglePressed && !lastReverseTogglePressed)
			send(sourcePos, PeripheralControl.REVERSE_THRUST_TOGGLE, 1.0f);

		if (!reverseTogglePressed && lastReverseTogglePressed)
			send(sourcePos, PeripheralControl.REVERSE_THRUST_TOGGLE, 0.0f);

		lastReverseTogglePressed = reverseTogglePressed;
	}

	private static void handleThrottle(BlockPos sourcePos, boolean throttleUpPressed, boolean throttleDownPressed) {
		float throttleUp = throttleUpPressed ? 1.0f : 0.0f;
		float throttleDown = throttleDownPressed ? 1.0f : 0.0f;

		if (Float.compare(throttleUp, lastThrottleUp) != 0) {
			lastThrottleUp = throttleUp;
			send(sourcePos, PeripheralControl.THROTTLE_UP, throttleUp);
		}

		if (Float.compare(throttleDown, lastThrottleDown) != 0) {
			lastThrottleDown = throttleDown;
			send(sourcePos, PeripheralControl.THROTTLE_DOWN, throttleDown);
		}
	}

	private static void send(BlockPos sourcePos, PeripheralControl control, float value) {
		CatnipServices.NETWORK.sendToServer(new AvionicsControlInputPacket(sourcePos, control, value));
	}

	private static void reset() {
		activeSourcePos = null;
		lastThrottleUp = -1.0f;
		lastThrottleDown = -1.0f;
		lastReverseTogglePressed = false;
		lastEngineTogglePressed = false;
	}
}
