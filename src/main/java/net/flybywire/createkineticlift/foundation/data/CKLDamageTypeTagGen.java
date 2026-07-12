package net.flybywire.createkineticlift.foundation.data;

import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.Nullable;

import net.flybywire.createkineticlift.CreateKineticLift;
import net.flybywire.createkineticlift.registries.CKLDamageTypes;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageType;

import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class CKLDamageTypeTagGen extends TagsProvider<DamageType> {

	public CKLDamageTypeTagGen(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
							   @Nullable ExistingFileHelper existingFileHelper) {
		super(output, Registries.DAMAGE_TYPE, lookupProvider, CreateKineticLift.MOD_ID, existingFileHelper);
	}

	@Override
	protected void addTags(HolderLookup.Provider provider) {
		tag(DamageTypeTags.BYPASSES_ARMOR).add(CKLDamageTypes.TURBOFAN);
	}

	@Override
	public String getName() {
		return "Create: Kinetic Lift's Damage Type Tags";
	}
}
