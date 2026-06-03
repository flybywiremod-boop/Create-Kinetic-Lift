package net.flybywire.createkineticlift.registries;

import net.flybywire.createkineticlift.CreateKineticLift;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CKLCreativeTab {
	private static final DeferredRegister<CreativeModeTab> TAB_REGISTER =
		DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CreateKineticLift.MOD_ID);
	public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CREATIVE_TAB = TAB_REGISTER.register("ckl_creative_tab",
		() -> CreativeModeTab.builder()
			.title(Component.translatable("itemGroup.createkineticlift.creative_tab"))
			.icon(CKLBlocks.CONTROL_SEAT::asStack)
			.displayItems((parameters, output) -> {
				output.accept(CKLBlocks.CONTROL_SEAT.get());
				output.accept(CKLBlocks.TURBOFAN_INTAKE.get());
				output.accept(CKLBlocks.TURBOFAN_EXHAUST.get());
				output.accept(CKLItems.IRON_BLADE.get());
				output.accept(CKLItems.RED_CABLE.get());
			})
			.build());

	public static void register(IEventBus modEventBus) {
		TAB_REGISTER.register(modEventBus);
	}
}
