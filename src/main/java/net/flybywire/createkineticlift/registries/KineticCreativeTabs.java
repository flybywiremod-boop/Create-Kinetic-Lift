package net.flybywire.createkineticlift.registries;

import net.flybywire.createkineticlift.CreateKineticLift;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class KineticCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CreateKineticLift.MOD_ID);

    public static final RegistryObject<CreativeModeTab> CREATE_TAB = CREATIVE_MODE_TABS.register("create_tab" ,
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(KineticBlocks.CONTROL_CHAIR.get()))
                    .title(Component.translatable("Create: Kinetic Lift"))
                    .displayItems((pParameters, pOutput) -> {
                        // Control blocks
                        pOutput.accept(KineticBlocks.CONTROL_CHAIR.get());

                        // Turbofan blocks
                        pOutput.accept(KineticBlocks.TURBOFAN_FRONT.get());
                        pOutput.accept(KineticBlocks.TURBOFAN_REAR.get());

                        // Custom Items
                        // pOutput.accept(KineticItems.HEADPHONES.get());
                        pOutput.accept(KineticItems.RED_CABLE.get());
                    })
                    .build());


    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}

