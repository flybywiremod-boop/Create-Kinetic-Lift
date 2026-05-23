package net.flybywire.createkineticlift;

import org.slf4j.Logger;

import net.flybywire.createkineticlift.registries.KineticBlockEntityTypes;
import net.flybywire.createkineticlift.registries.KineticBlocks;
import net.flybywire.createkineticlift.registries.KineticCreativeTabs;
import net.flybywire.createkineticlift.registries.KineticFluids;
import net.flybywire.createkineticlift.registries.KineticItems;

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

		KineticItems.register();
		KineticBlocks.register();
		KineticFluids.register();
		KineticBlockEntityTypes.register();
		KineticCreativeTabs.register(modEventBus);
	}
}
