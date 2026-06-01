package net.flybywire.createkineticlift.registries;

import static net.flybywire.createkineticlift.CreateKineticLift.REGISTRATE;

import net.flybywire.createkineticlift.content.turbofan.TurbofanStructuralBlockEntity;

import com.tterrag.registrate.util.entry.BlockEntityEntry;

public class CKLBlockEntityTypes {

	public static final BlockEntityEntry<TurbofanStructuralBlockEntity> TURBOFAN_STRUCTURAL = REGISTRATE
		.blockEntity("turbofan_structural", TurbofanStructuralBlockEntity::new)
		.validBlocks(CKLBlocks.TURBOFAN_STRUCTURAL)
		.register();

	public static void register() {
	}
}
