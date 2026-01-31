package net.flybywire.createkineticlift;

import com.mojang.logging.LogUtils;
import com.simibubi.create.CreateBuildInfo;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipModifier;
import net.createmod.catnip.lang.FontHelper;
import net.flybywire.createkineticlift.block.ModBlocks;
import net.flybywire.createkineticlift.block.entity.ModBlockEntities;
import net.flybywire.createkineticlift.client.renderer.TurbofanFrontRenderer;
import net.flybywire.createkineticlift.entity.ModEntities;
import net.flybywire.createkineticlift.entity.custom.controlseat.ControlSeatEntityRender;
import net.flybywire.createkineticlift.input.ModKeybinds;
import net.flybywire.createkineticlift.item.ModCreativeModTabs;
import net.flybywire.createkineticlift.item.ModItems;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import static com.mojang.text2speech.Narrator.LOGGER;

@Mod(CreateKineticLift.MOD_ID)
public class CreateKineticLift {
    public static final String MOD_ID = "createkineticlift";
    public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(MOD_ID)
            .defaultCreativeTab((ResourceKey<CreativeModeTab>) null)
            .setTooltipModifierFactory(item ->
                    new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE)
                            .andThen(TooltipModifier.mapNull(KineticStats.create(item)))
            );
    public static final Logger LOGGER = LogUtils.getLogger();

    public CreateKineticLift(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        CreateKineticLift.REGISTRATE.registerEventListeners(modEventBus);

        ModCreativeModTabs.register(modEventBus);

        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModEntities.register(modEventBus);

        modEventBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(this);


    }

    private void commonSetup(final FMLCommonSetupEvent event) {
    
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if(event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES)
            event.accept(ModItems.HEADPHONES);


}

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {

    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            // Register entity renderers
            EntityRenderers.register(ModEntities.CONSTROL_SEAT.get(), ControlSeatEntityRender::new);

            // Register block entity renderers
            BlockEntityRenderers.register(ModBlockEntities.TURBOFAN_FRONT.get(), TurbofanFrontRenderer::new);
        }

        @SubscribeEvent
        public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
            ModKeybinds.register(event);
        }
    }
}

