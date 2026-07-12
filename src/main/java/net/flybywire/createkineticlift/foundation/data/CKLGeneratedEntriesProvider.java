package net.flybywire.createkineticlift.foundation.data;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

import net.flybywire.createkineticlift.CreateKineticLift;
import net.flybywire.createkineticlift.registries.CKLDamageTypes;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;

import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;

public class CKLGeneratedEntriesProvider extends DatapackBuiltinEntriesProvider {

	private static final RegistrySetBuilder BUILDER =
		new RegistrySetBuilder().add(Registries.DAMAGE_TYPE, CKLDamageTypes::bootstrap);

	public CKLGeneratedEntriesProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, registries, BUILDER, Set.of(CreateKineticLift.MOD_ID));
	}

	@Override
	public String getName() {
		return "Create: Kinetic Lift's Generated Registry Entries";
	}
}
