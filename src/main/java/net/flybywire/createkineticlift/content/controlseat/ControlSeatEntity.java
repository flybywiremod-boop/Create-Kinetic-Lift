package net.flybywire.createkineticlift.content.controlseat;

import com.simibubi.create.content.contraptions.actors.seat.SeatEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Control Seat Entity - Aircraft control interface
 *
 * Keybinds (MSFS-style):
 * - Thrust increase/decrease: Slowly like MSFS
 */

public class ControlSeatEntity extends SeatEntity {

    public ControlSeatEntity(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        return new Vec3(this.getX(), this.getBoundingBox().maxY + 0.2f, this.getZ());
    }
}
