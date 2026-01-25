package net.flybywire.createkineticlift.item;

import net.flybywire.createkineticlift.CreateKineticLift;
import net.flybywire.createkineticlift.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.awt.*;

public class ModCreativeModTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CreateKineticLift.MOD_ID);

    public static final RegistryObject<CreativeModeTab> CREATE_TAB = CREATIVE_MODE_TABS.register("create_tab" ,
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModBlocks.CONTROL_CHAIR.get()))
            .title(Component.translatable("Create: Kinetic Lift"))
            .displayItems((pParameters, pOutput) -> {
                pOutput.accept(ModItems.SAPPHIRE.get());
                pOutput.accept(ModItems.RAW_SAPPHIRE.get());

                pOutput.accept(Items.DIAMOND);

                pOutput.accept(ModBlocks.CONTROL_CHAIR.get());



            })
            .build());


    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
