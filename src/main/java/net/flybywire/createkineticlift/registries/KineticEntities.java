package net.flybywire.createkineticlift.registries;

import net.flybywire.createkineticlift.CreateKineticLift;
import net.flybywire.createkineticlift.entity.custom.controlseat.ControlSeatEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class KineticEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, CreateKineticLift.MOD_ID);

    public static final RegistryObject<EntityType<ControlSeatEntity>> CONSTROL_SEAT =
            ENTITY_TYPES.register("control_seat", () -> EntityType.Builder.of(ControlSeatEntity::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f).build("control_seat"));


    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
