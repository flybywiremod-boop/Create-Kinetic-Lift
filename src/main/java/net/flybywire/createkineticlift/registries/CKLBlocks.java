package net.flybywire.createkineticlift.registries;

import static com.simibubi.create.foundation.data.TagGen.axeOrPickaxe;
import static com.simibubi.create.foundation.data.TagGen.pickaxeOnly;

import net.flybywire.createkineticlift.CreateKineticLift;
import net.flybywire.createkineticlift.content.controlseat.ControlSeatBlock;
import net.flybywire.createkineticlift.content.turbofan.AbstractTurbofanCoreBlock;
import net.flybywire.createkineticlift.content.turbofan.TurbofanExhaustBlock;
import net.flybywire.createkineticlift.content.turbofan.TurbofanExhaustBlockItem;
import net.flybywire.createkineticlift.content.turbofan.TurbofanIntakeBlock;
import net.flybywire.createkineticlift.content.turbofan.TurbofanIntakeBlockItem;
import net.flybywire.createkineticlift.content.turbofan.TurbofanStructuralBlock;
import net.flybywire.createkineticlift.foundation.data.CKLBlockStateGen;

import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import com.tterrag.registrate.util.entry.BlockEntry;

import net.minecraft.core.Direction;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public class CKLBlocks {

	private static final CreateRegistrate REGISTRATE = CreateKineticLift.REGISTRATE;

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
		.recipe((ctx, prov) ->
			ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ctx.get(), 1)
				.pattern("LM")
				.pattern("SW")
				.pattern("SS")
				.define('L', Items.LEVER)
				.define('M', AllItems.PRECISION_MECHANISM)
				.define('S', ItemTags.WOODEN_SLABS)
				.define('W', Items.BLUE_WOOL)
				.unlockedBy("has_ingredient", RegistrateRecipeProvider.has(AllItems.PRECISION_MECHANISM))
				.save(prov)
		)
		.register();

	public static final BlockEntry<TurbofanIntakeBlock> REGULAR_TURBOFAN_INTAKE = REGISTRATE
		.block("regular_turbofan_intake", TurbofanIntakeBlock::new)

		.initialProperties(SharedProperties::softMetal)
		.clientExtension(() -> () -> new AbstractTurbofanCoreBlock.RenderProperties())
		.properties(p -> p
			.requiresCorrectToolForDrops()
			.strength(5.5f, 4.0f)
			.noOcclusion()
			.isSuffocating((state, level, pos) -> false))
		.blockstate(CKLBlockStateGen.customHorizontalBlockProvider("regular_turbofan_intake_body"))
		.transform(pickaxeOnly())
		.tag(BlockTags.NEEDS_IRON_TOOL)
		.item(TurbofanIntakeBlockItem::new)
		.build()
		.register();

	public static final BlockEntry<TurbofanExhaustBlock> REGULAR_TURBOFAN_EXHAUST = REGISTRATE
		.block("regular_turbofan_exhaust", p -> {
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
		.clientExtension(() -> () -> new AbstractTurbofanCoreBlock.RenderProperties())
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
