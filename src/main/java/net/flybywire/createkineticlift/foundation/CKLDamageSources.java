package net.flybywire.createkineticlift.foundation;

import net.flybywire.createkineticlift.registries.CKLDamageTypes;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;

public final class CKLDamageSources {

	private CKLDamageSources() {
	}

	public static DamageSource turbofan(Level level) {
		return source(CKLDamageTypes.TURBOFAN, level);
	}

	private static DamageSource source(ResourceKey<DamageType> key, LevelReader level) {
		Registry<DamageType> registry = level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE);
		return new DamageSource(registry.getHolderOrThrow(key));
	}
}
