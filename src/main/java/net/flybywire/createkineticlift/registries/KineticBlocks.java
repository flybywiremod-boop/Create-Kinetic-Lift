package net.flybywire.createkineticlift.registries;

import com.simibubi.create.foundation.data.SharedProperties;

import com.tterrag.registrate.util.entry.BlockEntry;

import net.flybywire.createkineticlift.content.controlseat.ControlSeatBlock;
import net.flybywire.createkineticlift.foundation.datagen.CKLBlockStateGen;

import net.minecraft.tags.BlockTags;

import static com.simibubi.create.foundation.data.TagGen.axeOrPickaxe;
import static net.flybywire.createkineticlift.CreateKineticLift.REGISTRATE;

public class KineticBlocks {

	public static final BlockEntry<ControlSeatBlock> CONTROL_SEAT = REGISTRATE
		.block("control_seat", ControlSeatBlock::new)
		.initialProperties(SharedProperties::wooden)
		.properties(p -> p
			.requiresCorrectToolForDrops()
			.strength(1.0f))
		.blockstate(CKLBlockStateGen.invertedHorizontalBlockProvider(false))
		.transform(axeOrPickaxe())
		.tag(BlockTags.NEEDS_STONE_TOOL)
		.item()
		.build()
		.register();

	public static void register() {
	}
}
