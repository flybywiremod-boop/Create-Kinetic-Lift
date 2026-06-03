package net.flybywire.createkineticlift.registries;

import static com.simibubi.create.foundation.data.TagGen.axeOrPickaxe;
import static com.simibubi.create.foundation.data.TagGen.pickaxeOnly;
import static net.flybywire.createkineticlift.CreateKineticLift.REGISTRATE;

import net.flybywire.createkineticlift.content.controlseat.ControlSeatBlock;
import net.flybywire.createkineticlift.content.turbofan.TurbofanExhaustBlock;
import net.flybywire.createkineticlift.content.turbofan.TurbofanExhaustBlockItem;
import net.flybywire.createkineticlift.content.turbofan.TurbofanIntakeBlock;
import net.flybywire.createkineticlift.content.turbofan.TurbofanIntakeBlockItem;
import net.flybywire.createkineticlift.content.turbofan.TurbofanStructuralBlock;
import net.flybywire.createkineticlift.foundation.data.CKLBlockStateGen;

import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntry;

import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.phys.Vec3;

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

	public static final BlockEntry<TurbofanExhaustBlock> TURBOFAN_EXHAUST = REGISTRATE
		.block("turbofan_exhaust", p -> {
			p
				.dynamicShape()
				.requiresCorrectToolForDrops()
				.strength(5.5f, 4.0f)
				.noOcclusion()
				.isSuffocating((state, level, pos) -> false);
			p
				.offsetFunction = (state, level, pos) -> {
				Direction facing = state.getValue(TurbofanExhaustBlock.FACING);
				return Vec3.atLowerCornerOf(facing.getNormal()).scale(-0.125);
			};
			// There GOTTA be a better way to offset a model
			return new TurbofanExhaustBlock(p);
		})
		.blockstate(CKLBlockStateGen.horizontalBlockProvider())
		.transform(pickaxeOnly())
		.tag(BlockTags.NEEDS_IRON_TOOL)
		.item(TurbofanExhaustBlockItem::new)
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
