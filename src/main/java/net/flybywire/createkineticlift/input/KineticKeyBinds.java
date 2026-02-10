package net.flybywire.createkineticlift.input;

import com.mojang.blaze3d.platform.InputConstants;
import net.flybywire.createkineticlift.CreateKineticLift;
import net.flybywire.createkineticlift.entity.custom.controlseat.ControlSeatEntity;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

public class KineticKeyBinds {
    @Mod.EventBusSubscriber(modid = CreateKineticLift.MOD_ID, value = Dist.CLIENT)
    public class KineticKeybinds {

        private static final String CATEGORY = "key.categories.createkineticlift";

        // Keybind definitions
        public static KeyMapping THRUST_INCREASE;
        public static KeyMapping THRUST_DECREASE;
        public static KeyMapping PITCH_DOWN;
        public static KeyMapping PITCH_UP;
        public static KeyMapping ROLL_LEFT;
        public static KeyMapping ROLL_RIGHT;
        public static KeyMapping YAW_LEFT;
        public static KeyMapping YAW_RIGHT;
        public static KeyMapping REVERSE_THRUST;
        public static KeyMapping FLAPS_UP;
        public static KeyMapping FLAPS_DOWN;
        public static KeyMapping SPOILERS;

        public static void register(RegisterKeyMappingsEvent event) {
            THRUST_INCREASE = new KeyMapping(
                    "key.createkineticlift.thrust_increase",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_KP_ADD,
                    CATEGORY
            );

            THRUST_DECREASE = new KeyMapping(
                    "key.createkineticlift.thrust_decrease",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_KP_SUBTRACT,
                    CATEGORY
            );

            PITCH_DOWN = new KeyMapping(
                    "key.createkineticlift.pitch_down",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_W,
                    CATEGORY
            );

            PITCH_UP = new KeyMapping(
                    "key.createkineticlift.pitch_up",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_S,
                    CATEGORY
            );

            ROLL_LEFT = new KeyMapping(
                    "key.createkineticlift.roll_left",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_A,
                    CATEGORY
            );

            ROLL_RIGHT = new KeyMapping(
                    "key.createkineticlift.roll_right",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_D,
                    CATEGORY
            );

            YAW_LEFT = new KeyMapping(
                    "key.createkineticlift.yaw_left",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_X,
                    CATEGORY
            );

            YAW_RIGHT = new KeyMapping(
                    "key.createkineticlift.yaw_right",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_Y,
                    CATEGORY
            );

            REVERSE_THRUST = new KeyMapping(
                    "key.createkineticlift.reverse_thrust",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_R,
                    CATEGORY
            );

            FLAPS_UP = new KeyMapping(
                    "key.createkineticlift.flaps_up",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_F,
                    CATEGORY
            );

            FLAPS_DOWN = new KeyMapping(
                    "key.createkineticlift.flaps_down",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_V,
                    CATEGORY
            );

            SPOILERS = new KeyMapping(
                    "key.createkineticlift.spoilers",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_B,
                    CATEGORY
            );

            // Register all keybinds
            event.register(THRUST_INCREASE);
            event.register(THRUST_DECREASE);
            event.register(PITCH_DOWN);
            event.register(PITCH_UP);
            event.register(ROLL_LEFT);
            event.register(ROLL_RIGHT);
            event.register(YAW_LEFT);
            event.register(YAW_RIGHT);
            event.register(REVERSE_THRUST);
            event.register(FLAPS_UP);
            event.register(FLAPS_DOWN);
            event.register(SPOILERS);
        }

        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;

            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;

            // Check if player is in a control seat
            Entity vehicle = mc.player.getVehicle();
            if (!(vehicle instanceof ControlSeatEntity controlSeat)) return;

            // Process held keys for continuous input
            if (THRUST_INCREASE.isDown()) {
                controlSeat.handleControlInput(AircraftControlInput.THRUST_INCREASE);
            }
            if (THRUST_DECREASE.isDown()) {
                controlSeat.handleControlInput(AircraftControlInput.THRUST_DECREASE);
            }

            // Pitch control
            if (PITCH_DOWN.isDown()) {
                controlSeat.handleControlInput(AircraftControlInput.PITCH_DOWN);
            } else if (PITCH_UP.isDown()) {
                controlSeat.handleControlInput(AircraftControlInput.PITCH_UP);
            } else {
                controlSeat.handleControlInput(AircraftControlInput.PITCH_NEUTRAL);
            }

            // Roll control
            if (ROLL_LEFT.isDown()) {
                controlSeat.handleControlInput(AircraftControlInput.ROLL_LEFT);
            } else if (ROLL_RIGHT.isDown()) {
                controlSeat.handleControlInput(AircraftControlInput.ROLL_RIGHT);
            } else {
                controlSeat.handleControlInput(AircraftControlInput.ROLL_NEUTRAL);
            }

            // Yaw control
            if (YAW_LEFT.isDown()) {
                controlSeat.handleControlInput(AircraftControlInput.YAW_LEFT);
            } else if (YAW_RIGHT.isDown()) {
                controlSeat.handleControlInput(AircraftControlInput.YAW_RIGHT);
            } else {
                controlSeat.handleControlInput(AircraftControlInput.YAW_NEUTRAL);
            }

            // Toggle inputs (only on press, not hold)
            while (REVERSE_THRUST.consumeClick()) {
                controlSeat.handleControlInput(AircraftControlInput.REVERSE_THRUST_TOGGLE);
            }
            while (FLAPS_UP.consumeClick()) {
                controlSeat.handleControlInput(AircraftControlInput.FLAPS_DECREASE);
            }
            while (FLAPS_DOWN.consumeClick()) {
                controlSeat.handleControlInput(AircraftControlInput.FLAPS_INCREASE);
            }
            while (SPOILERS.consumeClick()) {
                controlSeat.handleControlInput(AircraftControlInput.SPOILERS_TOGGLE);
            }
        }
    }
}

