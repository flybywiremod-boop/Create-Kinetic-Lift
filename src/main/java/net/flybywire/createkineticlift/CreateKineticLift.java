package net.flybywire.createkineticlift;

import org.slf4j.Logger;

import net.flybywire.createkineticlift.registries.CKLBlockEntityTypes;
import net.flybywire.createkineticlift.registries.CKLBlocks;
import net.flybywire.createkineticlift.registries.CKLCreativeTab;
import net.flybywire.createkineticlift.registries.CKLFluids;
import net.flybywire.createkineticlift.registries.CKLItems;

import com.mojang.logging.LogUtils;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipModifier;

import net.createmod.catnip.lang.FontHelper;
import net.minecraft.resources.ResourceKey;
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

		CKLItems.register();
		CKLBlocks.register();
		CKLFluids.register();
		CKLBlockEntityTypes.register();
		CKLCreativeTab.register(modEventBus);
	}
}
