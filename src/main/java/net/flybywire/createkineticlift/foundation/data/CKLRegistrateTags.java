package net.flybywire.createkineticlift.foundation.data;

import net.flybywire.createkineticlift.CreateKineticLift;
import net.flybywire.createkineticlift.registries.CKLTags.CKLFluidTags;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.TagGen.CreateTagsProvider;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateTagsProvider;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;

public class CKLRegistrateTags {

	private static final CreateRegistrate REGISTRATE = CreateKineticLift.REGISTRATE;

	public static void addGenerators() {
		REGISTRATE.addDataGenerator(ProviderType.FLUID_TAGS, CKLRegistrateTags::genFluidTags);
	}

	private static void genFluidTags(RegistrateTagsProvider<Fluid> provider) {
		CreateTagsProvider<Fluid> tags = new CreateTagsProvider<>(provider, Fluid::builtInRegistryHolder);
		var turbofanFuels = tags.tag(CKLFluidTags.TURBOFAN_FUELS.tag);

		turbofanFuels.addOptional(ResourceLocation.parse("tfmg:kerosene"));
		turbofanFuels.addOptional(ResourceLocation.parse("tfmg:diesel"));
		turbofanFuels.addOptional(ResourceLocation.parse("createdieselgenerators:diesel"));
		turbofanFuels.addOptional(ResourceLocation.parse("createdieselgenerators:biodiesel"));
		turbofanFuels.addOptional(ResourceLocation.parse("createpropulsion:turpentine"));
	}
}
