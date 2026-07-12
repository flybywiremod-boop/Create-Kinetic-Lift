package net.flybywire.createkineticlift.registries;

import net.flybywire.createkineticlift.CreateKineticLift;

import net.createmod.catnip.lang.Lang;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

public class CKLTags {

	public enum CKLFluidTags {

		TURBOFAN_FUELS;

		public final TagKey<Fluid> tag;

		CKLFluidTags() {
			tag = TagKey.create(Registries.FLUID, CreateKineticLift.asResource(Lang.asId(name())));
		}

		@SuppressWarnings("deprecation")
		public boolean matches(Fluid fluid) {
			return fluid.is(tag);
		}

		public boolean matches(FluidState state) {
			return state.is(tag);
		}
	}
}
