package net.flybywire.createkineticlift.content.turbofan;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

import net.flybywire.createkineticlift.registries.CKLSoundEvents;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class TurbofanSoundInstance extends AbstractTickableSoundInstance {

	private static final float IDLE_VOLUME = 0.1f;
	private static final float IDLE_VOLUME_CHANGE_PER_TICK = 0.0025f;
	private static final float STOP_THRUST_THRESHOLD_PN = 0.5f;

	private static final Map<TurbofanBlockEntity, TurbofanSoundInstance> ACTIVE_SOUNDS = new WeakHashMap<>();

	private final WeakReference<TurbofanBlockEntity> blockEntity;

	private float idleVolume;

	private TurbofanSoundInstance(TurbofanBlockEntity be) {
		super(CKLSoundEvents.TURBOFAN_LOOP.getMainEvent(), SoundSource.BLOCKS, SoundInstance.createUnseededRandom());

		blockEntity = new WeakReference<>(be);

		looping = true;
		delay = 0;
		relative = false;
		attenuation = SoundInstance.Attenuation.LINEAR;

		volume = 0.0f;
		pitch = 1.0f;

		updatePosition(be);
	}

	public static void tickSound(TurbofanBlockEntity be) {
		TurbofanSoundInstance sound = ACTIVE_SOUNDS.get(be);

		if (sound != null && sound.isStopped()) {
			ACTIVE_SOUNDS.remove(be);
			sound = null;
		}

		if (sound != null || !shouldStart(be))
			return;

		sound = new TurbofanSoundInstance(be);
		ACTIVE_SOUNDS.put(be, sound);

		Minecraft.getInstance()
			.getSoundManager()
			.play(sound);
	}

	@Override
	public void tick() {
		TurbofanBlockEntity be = blockEntity.get();

		if (be == null || be.isRemoved() || be.getLevel() == null || be.getLevel() != Minecraft.getInstance().level) {
			stopAndRemove(be);
			return;
		}

		updatePosition(be);
		updateIdleVolume(be);

		float thrustVolume =
			Mth.clamp(Math.abs(be.getThrustPN()) / TurbofanBlockEntity.MAX_THRUST_PN, 0.0f, 1.0f);

		volume = Math.max(idleVolume, thrustVolume);

		if (!be.isEngineRunning() && Math.abs(be.getThrustPN()) <= STOP_THRUST_THRESHOLD_PN && idleVolume <= 0.0f)
			stopAndRemove(be);
	}

	@Override
	public boolean canStartSilent() {
		return true;
	}

	private void updateIdleVolume(TurbofanBlockEntity be) {
		float targetIdleVolume = be.isEngineRunning() ? IDLE_VOLUME : 0.0f;

		idleVolume +=
			Mth.clamp(targetIdleVolume - idleVolume, -IDLE_VOLUME_CHANGE_PER_TICK, IDLE_VOLUME_CHANGE_PER_TICK);
	}

	private static boolean shouldStart(TurbofanBlockEntity be) {
		return be.isEngineRunning() || Math.abs(be.getThrustPN()) > STOP_THRUST_THRESHOLD_PN;
	}

	private void updatePosition(TurbofanBlockEntity be) {
		Vec3 position = Vec3.atCenterOf(be.getBlockPos());

		x = position.x;
		y = position.y;
		z = position.z;
	}

	private void stopAndRemove(TurbofanBlockEntity be) {
		if (be != null)
			ACTIVE_SOUNDS.remove(be, this);

		stop();
	}
}
