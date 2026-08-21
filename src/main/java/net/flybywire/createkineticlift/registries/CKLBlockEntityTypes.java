package net.flybywire.createkineticlift.registries;

import net.flybywire.createkineticlift.CreateKineticLift;
import net.flybywire.createkineticlift.content.controlseat.SidestickBlockEntity;
import net.flybywire.createkineticlift.content.turbofan.TurbofanBlockEntity;
import net.flybywire.createkineticlift.content.turbofan.TurbofanRenderer;
import net.flybywire.createkineticlift.content.turbofan.TurbofanStructuralBlockEntity;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.BlockEntityEntry;

public class CKLBlockEntityTypes {

	private static final CreateRegistrate REGISTRATE = CreateKineticLift.REGISTRATE;

	public static final BlockEntityEntry<SidestickBlockEntity> CONTROL_SEAT = REGISTRATE
		.blockEntity("control_seat", SidestickBlockEntity::new)
		.validBlocks(CKLBlocks.CONTROL_SEAT)
		.register();

	public static final BlockEntityEntry<TurbofanBlockEntity> TURBOFAN_INTAKE = REGISTRATE
		.blockEntity("turbofan_intake", TurbofanBlockEntity::new)
		.validBlocks(CKLBlocks.REGULAR_TURBOFAN_INTAKE)
		.renderer(() -> TurbofanRenderer::new)
		.register();

	public static final BlockEntityEntry<TurbofanStructuralBlockEntity> TURBOFAN_STRUCTURAL = REGISTRATE
		.blockEntity("turbofan_structural", TurbofanStructuralBlockEntity::new)
		.validBlocks(CKLBlocks.TURBOFAN_STRUCTURAL)
		.register();

	public static void register() {
	}
}
