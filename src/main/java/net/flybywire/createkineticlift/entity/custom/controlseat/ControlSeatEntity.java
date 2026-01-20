package net.flybywire.createkineticlift.entity.custom.controlseat;

import net.flybywire.createkineticlift.CreateKineticLift;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ControlSeatEntity extends Entity {
    public ControlSeatEntity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    protected void defineSynchedData() {

    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {

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
        }
    }

}
