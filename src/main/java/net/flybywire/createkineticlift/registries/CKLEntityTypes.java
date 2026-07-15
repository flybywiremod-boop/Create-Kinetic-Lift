package net.flybywire.createkineticlift.registries;

import dev.ryanhcode.sable.index.SableTags;

import net.flybywire.createkineticlift.CreateKineticLift;
import net.flybywire.createkineticlift.content.controlseat.ControlSeatEntity;

import com.simibubi.create.content.contraptions.actors.seat.SeatEntity;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.EntityEntry;

import net.minecraft.world.entity.MobCategory;

public class CKLEntityTypes {

	private static final CreateRegistrate REGISTRATE = CreateKineticLift.REGISTRATE;

	public static final EntityEntry<ControlSeatEntity> CONTROL_SEAT = REGISTRATE
		.<ControlSeatEntity>entity("control_seat", ControlSeatEntity::new, MobCategory.MISC)
		.properties(properties -> properties
			.sized(0.25F, 0.35F)
			.setTrackingRange(5)
			.setUpdateInterval(Integer.MAX_VALUE)
			.setShouldReceiveVelocityUpdates(false)
			.fireImmune())
		.tag(SableTags.RETAIN_IN_SUB_LEVEL)
		.renderer(() -> SeatEntity.Render::new)
		.register();

	public static void register() {
	}
}
