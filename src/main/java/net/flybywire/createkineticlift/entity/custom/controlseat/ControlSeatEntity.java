package net.flybywire.createkineticlift.entity.custom.controlseat;

import net.flybywire.createkineticlift.Jet.TurbofanFrontBlockEntity;
import net.flybywire.createkineticlift.Jet.TurbofanRearBlockEntity;
import net.flybywire.createkineticlift.input.AircraftControlInput;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Control Seat Entity - Aircraft control interface
 *
 * Keybinds (MSFS-style):
 * - Thrust increase/decrease: Slowly like MSFS
 * - W: Pitch down (rear flaps)
 * - S: Pitch up (rear flaps)
 * - A: Roll left (sidestick slowly)
 * - D: Roll right (sidestick slowly)
 * - Y: Yaw right (rudder)
 * - X: Yaw left (rudder)
 * - R: Reverse thrust toggle
 * - Flaps: GUI controlled (mouse free when sitting)
 */
public class ControlSeatEntity extends Entity {

    // Synced data for control inputs
    private static final EntityDataAccessor<Float> THRUST_LEVEL = SynchedEntityData.defineId(ControlSeatEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> PITCH_INPUT = SynchedEntityData.defineId(ControlSeatEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> ROLL_INPUT = SynchedEntityData.defineId(ControlSeatEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> YAW_INPUT = SynchedEntityData.defineId(ControlSeatEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> REVERSE_THRUST = SynchedEntityData.defineId(ControlSeatEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> FLAP_LEVEL = SynchedEntityData.defineId(ControlSeatEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> SPOILERS_ACTIVE = SynchedEntityData.defineId(ControlSeatEntity.class, EntityDataSerializers.BOOLEAN);

    // Control smoothing
    private static final float THRUST_CHANGE_RATE = 0.5f; // Per tick, like MSFS slowly
    private static final float INPUT_SMOOTHING = 0.1f; // Sidestick smoothing

    // Flap levels: 0 = retracted, 1-4 = increasing flap extension
    private static final int MAX_FLAP_LEVEL = 4;

    // Search radius for finding turbofan blocks
    private static final int TURBOFAN_SEARCH_RADIUS = 32;

    public ControlSeatEntity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(THRUST_LEVEL, 0.0f);
        this.entityData.define(PITCH_INPUT, 0.0f);
        this.entityData.define(ROLL_INPUT, 0.0f);
        this.entityData.define(YAW_INPUT, 0.0f);
        this.entityData.define(REVERSE_THRUST, false);
        this.entityData.define(FLAP_LEVEL, 0);
        this.entityData.define(SPOILERS_ACTIVE, false);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.entityData.set(THRUST_LEVEL, tag.getFloat("ThrustLevel"));
        this.entityData.set(FLAP_LEVEL, tag.getInt("FlapLevel"));
        this.entityData.set(REVERSE_THRUST, tag.getBoolean("ReverseThrust"));
        this.entityData.set(SPOILERS_ACTIVE, tag.getBoolean("SpoilersActive"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putFloat("ThrustLevel", this.entityData.get(THRUST_LEVEL));
        tag.putInt("FlapLevel", this.entityData.get(FLAP_LEVEL));
        tag.putBoolean("ReverseThrust", this.entityData.get(REVERSE_THRUST));
        tag.putBoolean("SpoilersActive", this.entityData.get(SPOILERS_ACTIVE));
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return passenger instanceof Player;
    }

    @Override
    protected void positionRider(Entity pPassenger, MoveFunction pCallback) {
        if (this.hasPassenger(pPassenger)) {
            pCallback.accept(
                    pPassenger,
                    this.getX(),
                    this.getY(),
                    this.getZ()
            );
        }
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity pPassenger) {
        return new Vec3(this.getX(), this.getBoundingBox().maxY + 0.2f, this.getZ());
    }

    @Override
    public void tick() {
        super.tick();
        this.setDeltaMovement(0, 0, 0);

        if (!isVehicle() && tickCount > 10) {
            this.remove(RemovalReason.DISCARDED);
            return;
        }

        // Process control inputs on server
        if (!level().isClientSide() && isVehicle()) {
            processControlInputs();
            applyToTurbofans();
        }
    }

    /**
     * Called from client-side keybind handler to update control inputs
     */
    public void handleControlInput(AircraftControlInput input) {
        if (level().isClientSide()) {
            // Send to server via packet
            // For now, directly set (will need network packet in full impl)
        }

        switch (input) {
            case THRUST_INCREASE -> adjustThrust(THRUST_CHANGE_RATE);
            case THRUST_DECREASE -> adjustThrust(-THRUST_CHANGE_RATE);
            case PITCH_DOWN -> setPitchInput(1.0f); // W - nose down
            case PITCH_UP -> setPitchInput(-1.0f);  // S - nose up
            case ROLL_LEFT -> setRollInput(-1.0f);  // A
            case ROLL_RIGHT -> setRollInput(1.0f);  // D
            case YAW_LEFT -> setYawInput(-1.0f);    // X
            case YAW_RIGHT -> setYawInput(1.0f);    // Y
            case REVERSE_THRUST_TOGGLE -> toggleReverseThrust();
            case FLAPS_INCREASE -> adjustFlaps(1);
            case FLAPS_DECREASE -> adjustFlaps(-1);
            case SPOILERS_TOGGLE -> toggleSpoilers();
            case PITCH_NEUTRAL -> setPitchInput(0);
            case ROLL_NEUTRAL -> setRollInput(0);
            case YAW_NEUTRAL -> setYawInput(0);
        }
    }

    private void processControlInputs() {
        // Smooth out control inputs over time
        float currentPitch = entityData.get(PITCH_INPUT);
        float currentRoll = entityData.get(ROLL_INPUT);
        float currentYaw = entityData.get(YAW_INPUT);

        // Gradually return to neutral when no input
        if (Math.abs(currentPitch) > 0.01f) {
            entityData.set(PITCH_INPUT, currentPitch * (1 - INPUT_SMOOTHING));
        }
        if (Math.abs(currentRoll) > 0.01f) {
            entityData.set(ROLL_INPUT, currentRoll * (1 - INPUT_SMOOTHING));
        }
        if (Math.abs(currentYaw) > 0.01f) {
            entityData.set(YAW_INPUT, currentYaw * (1 - INPUT_SMOOTHING));
        }
    }

    private void applyToTurbofans() {
        // Find all turbofan front blocks within search radius
        BlockPos centerPos = blockPosition();
        AABB searchBox = new AABB(centerPos).inflate(TURBOFAN_SEARCH_RADIUS);

        float thrustLevel = entityData.get(THRUST_LEVEL);
        boolean reverseThrust = entityData.get(REVERSE_THRUST);

        // Find turbofans on the same VS2 ship (or nearby if not on ship)
        for (BlockPos pos : BlockPos.betweenClosed(
                centerPos.offset(-TURBOFAN_SEARCH_RADIUS, -TURBOFAN_SEARCH_RADIUS, -TURBOFAN_SEARCH_RADIUS),
                centerPos.offset(TURBOFAN_SEARCH_RADIUS, TURBOFAN_SEARCH_RADIUS, TURBOFAN_SEARCH_RADIUS))) {

            BlockEntity be = level().getBlockEntity(pos);
            if (be instanceof TurbofanFrontBlockEntity turbofan) {
                // Set thrust level (0-185 Bps)
                turbofan.setTargetThrust(thrustLevel * TurbofanFrontBlockEntity.MAX_THRUST);

                // Set reverse thrust on rear
                TurbofanRearBlockEntity rear = turbofan.getRearTurbofan();
                if (rear != null) {
                    rear.setReverseThrust(reverseThrust);
                }
            }
        }
    }

    // Control input setters
    public void adjustThrust(float delta) {
        float current = entityData.get(THRUST_LEVEL);
        float newThrust = Math.max(0, Math.min(1, current + delta / 100f));
        entityData.set(THRUST_LEVEL, newThrust);
    }

    public void setPitchInput(float value) {
        entityData.set(PITCH_INPUT, Math.max(-1, Math.min(1, value)));
    }

    public void setRollInput(float value) {
        entityData.set(ROLL_INPUT, Math.max(-1, Math.min(1, value)));
    }

    public void setYawInput(float value) {
        entityData.set(YAW_INPUT, Math.max(-1, Math.min(1, value)));
    }

    public void toggleReverseThrust() {
        entityData.set(REVERSE_THRUST, !entityData.get(REVERSE_THRUST));
    }

    public void adjustFlaps(int delta) {
        int current = entityData.get(FLAP_LEVEL);
        int newLevel = Math.max(0, Math.min(MAX_FLAP_LEVEL, current + delta));
        entityData.set(FLAP_LEVEL, newLevel);
    }

    public void toggleSpoilers() {
        entityData.set(SPOILERS_ACTIVE, !entityData.get(SPOILERS_ACTIVE));
    }

    // Getters for rendering/display
    public float getThrustLevel() {
        return entityData.get(THRUST_LEVEL);
    }

    public float getPitchInput() {
        return entityData.get(PITCH_INPUT);
    }

    public float getRollInput() {
        return entityData.get(ROLL_INPUT);
    }

    public float getYawInput() {
        return entityData.get(YAW_INPUT);
    }

    public boolean isReverseThrustActive() {
        return entityData.get(REVERSE_THRUST);
    }

    public int getFlapLevel() {
        return entityData.get(FLAP_LEVEL);
    }

    public boolean areSpoilersActive() {
        return entityData.get(SPOILERS_ACTIVE);
    }
}
