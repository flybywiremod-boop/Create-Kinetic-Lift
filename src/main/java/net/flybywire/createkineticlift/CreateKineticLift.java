package net.flybywire.createkineticlift;

import org.slf4j.Logger;

import net.flybywire.createkineticlift.foundation.config.CKLConfigs;
import net.flybywire.createkineticlift.network.CKLPackets;
import net.flybywire.createkineticlift.registries.CKLBlockEntityTypes;
import net.flybywire.createkineticlift.registries.CKLBlocks;
import net.flybywire.createkineticlift.registries.CKLCreativeTab;
import net.flybywire.createkineticlift.registries.CKLEntityTypes;
import net.flybywire.createkineticlift.registries.CKLFluids;
import net.flybywire.createkineticlift.registries.CKLItems;
import net.flybywire.createkineticlift.registries.CKLPartialModels;
import net.flybywire.createkineticlift.registries.CKLSoundEvents;

import com.mojang.logging.LogUtils;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipModifier;

import net.createmod.catnip.lang.FontHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(CreateKineticLift.MOD_ID)
public class CreateKineticLift {

	public static final String MOD_ID = "createkineticlift";
	public static final Logger LOGGER = LogUtils.getLogger();

	public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(MOD_ID)
		.setTooltipModifierFactory(item ->
			new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE)
				.andThen(TooltipModifier.mapNull(KineticStats.create(item)))
		);

	public CreateKineticLift(IEventBus modEventBus, ModContainer modContainer) {
		REGISTRATE.defaultCreativeTab((ResourceKey<CreativeModeTab>) null);
		REGISTRATE.registerEventListeners(modEventBus);

		CKLSoundEvents.prepare();

		CKLItems.register();
		CKLBlocks.register();
		CKLFluids.register();
		CKLBlockEntityTypes.register();
		CKLEntityTypes.register();
		CKLPackets.register();

		CKLConfigs.register(modContainer);

		CKLCreativeTab.register(modEventBus);

		CKLPartialModels.init();

		modEventBus.addListener(CKLSoundEvents::register);
	}

	public static ResourceLocation asResource(String path) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
	}
}
