package net.flybywire.createkineticlift.registries;

import static com.simibubi.create.foundation.data.TagGen.axeOrPickaxe;
import static com.simibubi.create.foundation.data.TagGen.pickaxeOnly;
import static net.flybywire.createkineticlift.CreateKineticLift.REGISTRATE;

import net.flybywire.createkineticlift.content.controlseat.ControlSeatBlock;
import net.flybywire.createkineticlift.content.turbofan.TurbofanIntakeBlock;
import net.flybywire.createkineticlift.content.turbofan.TurbofanIntakeBlockItem;
import net.flybywire.createkineticlift.content.turbofan.TurbofanStructuralBlock;
import net.flybywire.createkineticlift.foundation.datagen.CKLBlockStateGen;

import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntry;

import net.minecraft.tags.BlockTags;

public class CKLBlocks {

	public static final BlockEntry<ControlSeatBlock> CONTROL_SEAT = REGISTRATE
		.block("control_seat", ControlSeatBlock::new)
		.initialProperties(SharedProperties::wooden)
		.properties(p -> p
			.requiresCorrectToolForDrops()
			.strength(1.0f))
		.blockstate(CKLBlockStateGen.invertedHorizontalBlockProvider())
		.transform(axeOrPickaxe())
		.tag(BlockTags.NEEDS_STONE_TOOL)
		.item()
		.build()
		.register();

	public static final BlockEntry<TurbofanIntakeBlock> TURBOFAN_INTAKE = REGISTRATE
		.block("turbofan_intake", TurbofanIntakeBlock::new)

		.initialProperties(SharedProperties::softMetal)
		.properties(p -> p
			.requiresCorrectToolForDrops()
			.strength(5.5f, 4.0f)
			.noOcclusion()
			.isSuffocating((state, level, pos) -> false))
		.blockstate(CKLBlockStateGen.horizontalBlockProvider())
		.transform(pickaxeOnly())
		.tag(BlockTags.NEEDS_IRON_TOOL)
		.item(TurbofanIntakeBlockItem::new)
		.build()
		.register();

	public static final BlockEntry<TurbofanStructuralBlock> TURBOFAN_STRUCTURAL = REGISTRATE
		.block("turbofan_structure", TurbofanStructuralBlock::new)
		.initialProperties(SharedProperties::softMetal)
		.clientExtension(() -> () -> new TurbofanStructuralBlock.RenderProperties())
		.properties(p -> p
			.requiresCorrectToolForDrops()
			.strength(5.5f, 4.0f)
			.noOcclusion()
			.isSuffocating((state, level, pos) -> false))
		.blockstate((ctx, prov) -> {
		})
		.transform(pickaxeOnly())
		.tag(BlockTags.NEEDS_IRON_TOOL)
		.register();

	public static void register() {
	}
}
