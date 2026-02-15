package net.flybywire.createkineticlift.registries;

import net.flybywire.createkineticlift.CreateKineticLift;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class KineticCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CreateKineticLift.MOD_ID);

    public static final ResourceKey<CreativeModeTab> TAB_KEY = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB,
            CreateKineticLift.asResource("create_tab")
    );

    public static final RegistryObject<CreativeModeTab> CREATE_TAB = CREATIVE_MODE_TABS.register("create_tab" ,
            () -> CreativeModeTab.builder()
                    .icon(() -> KineticBlocks.CONTROL_SEAT.asStack())
                    .title(Component.translatable("itemGroup.createkineticlift.create_tab"))
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}