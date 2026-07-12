package net.flybywire.createkineticlift.registries;

import java.util.function.BiConsumer;

import org.lwjgl.glfw.GLFW;

import net.flybywire.createkineticlift.CreateKineticLift;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

@EventBusSubscriber(modid = CreateKineticLift.MOD_ID, value = Dist.CLIENT)
public enum CKLKeys {

	THROTTLE_UP("throttle_up", GLFW.GLFW_KEY_SPACE, "Throttle Up"),
	THROTTLE_DOWN("throttle_down", GLFW.GLFW_KEY_LEFT_CONTROL, "Throttle Down"),
	ENGINE_TOGGLE("engine_toggle", GLFW.GLFW_KEY_F, "Toggle Engine");

	private static final String CATEGORY = "key.categories." + CreateKineticLift.MOD_ID;

	private KeyMapping keybind;
	private final String description;
	private final int key;
	private final String translation;

	CKLKeys(String description, int key, String translation) {
		this.description = "key." + CreateKineticLift.MOD_ID + "." + description;
		this.key = key;
		this.translation = translation;
	}

	public static void provideLang(BiConsumer<String, String> consumer) {
		consumer.accept(CATEGORY, "Create: Kinetic Lift");

		for (CKLKeys key : values())
			consumer.accept(key.description, key.translation);
	}

	@SubscribeEvent
	public static void register(RegisterKeyMappingsEvent event) {
		for (CKLKeys key : values()) {
			key.keybind = new KeyMapping(key.description, key.key, CATEGORY);
			event.register(key.keybind);
		}
	}

	public KeyMapping getKeybind() {
		return keybind;
	}

	public boolean isPressed() {
		return keybind.isDown();
	}

	public String getBoundKey() {
		return keybind.getTranslatedKeyMessage()
			.getString()
			.toUpperCase();
	}

	public boolean matches(int key, int scanCode) {
		return keybind.matches(key, scanCode);
	}

	public static boolean matchesAny(int key, int scanCode) {
		for (CKLKeys cklKey : values()) {
			if (cklKey.matches(key, scanCode))
				return true;
		}

		return false;
	}

	public static boolean isKeyDown(int key) {
		return InputConstants.isKeyDown(Minecraft.getInstance()
			.getWindow()
			.getWindow(), key);
	}
}
