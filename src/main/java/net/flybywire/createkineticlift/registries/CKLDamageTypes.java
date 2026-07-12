package net.flybywire.createkineticlift.registries;

import net.flybywire.createkineticlift.CreateKineticLift;

import com.simibubi.create.foundation.damageTypes.DamageTypeBuilder;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;

public final class CKLDamageTypes {

	public static final ResourceKey<DamageType> TURBOFAN = key("turbofan");

	private CKLDamageTypes() {
	}

	private static ResourceKey<DamageType> key(String name) {
		return ResourceKey.create(Registries.DAMAGE_TYPE, CreateKineticLift.asResource(name));
	}

	public static void bootstrap(BootstrapContext<DamageType> context) {
		new DamageTypeBuilder(TURBOFAN).register(context);
	}
}
