package net.flybywire.createkineticlift.registries;

import net.flybywire.createkineticlift.CreateKineticLift;
import net.flybywire.createkineticlift.Jet.FuelTankBlockEntity;
import net.flybywire.createkineticlift.Jet.TurbofanFrontBlockEntity;
import net.flybywire.createkineticlift.Jet.TurbofanRearBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class KineticBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, CreateKineticLift.MOD_ID);

    public static final RegistryObject<BlockEntityType<TurbofanFrontBlockEntity>> TURBOFAN_FRONT =
            BLOCK_ENTITIES.register("turbofan_front",
                    () -> BlockEntityType.Builder.of(TurbofanFrontBlockEntity::new,
                            KineticBlocks.TURBOFAN_FRONT.get()).build(null));

    public static final RegistryObject<BlockEntityType<TurbofanRearBlockEntity>> TURBOFAN_REAR =
            BLOCK_ENTITIES.register("turbofan_rear",
                    () -> BlockEntityType.Builder.of(TurbofanRearBlockEntity::new,
                            KineticBlocks.TURBOFAN_REAR.get()).build(null));

    public static final RegistryObject<BlockEntityType<FuelTankBlockEntity>> FUEL_TANK =
            BLOCK_ENTITIES.register("fuel_tank",
                    () -> BlockEntityType.Builder.of(FuelTankBlockEntity::new,
                            KineticBlocks.FUEL_TANK.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
