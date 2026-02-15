package net.flybywire.createkineticlift.registries;

import net.flybywire.createkineticlift.CreateKineticLift;
import net.flybywire.createkineticlift.content.controlseat.ControlSeatEntity;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.EntityEntry;



import net.minecraft.world.entity.MobCategory;

public class KineticEntities {
    private static final CreateRegistrate REGISTRATE = CreateKineticLift.REGISTRATE;

    public static final EntityEntry<ControlSeatEntity> CONTROL_SEAT = REGISTRATE
            .<ControlSeatEntity>entity("control_seat", ControlSeatEntity::new, MobCategory.MISC)
            .properties(b -> b
                    .fireImmune()
                    .sized(0.25f, 0.35f)
                    .clientTrackingRange(5)
                    .updateInterval(Integer.MAX_VALUE)
                    .setShouldReceiveVelocityUpdates(false)
            )
            .renderer(() -> ControlSeatEntity.Render::new)
            .register();


    public static void register() {
    }
}
