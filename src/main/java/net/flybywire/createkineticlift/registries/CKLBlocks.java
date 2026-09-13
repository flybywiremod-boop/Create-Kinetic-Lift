package net.flybywire.createkineticlift.registries;

import com.simibubi.create.foundation.block.DyedBlockList;

import com.simibubi.create.foundation.utility.DyeHelper;

import com.tterrag.registrate.providers.RegistrateLangProvider;
import com.tterrag.registrate.providers.RegistrateRecipeProvider;

import dev.simulated_team.simulated.index.SimBlocks;

import dev.simulated_team.simulated.index.SimTags;

import net.flybywire.createkineticlift.CreateKineticLift;
import net.flybywire.createkineticlift.content.controlseat.SidestickBlock;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntry;

import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.DyeColor;

public class CKLBlocks {

	private static final CreateRegistrate REGISTRATE = CreateKineticLift.REGISTRATE;

	public static final BlockEntry<SidestickBlock> BLUE_SIDESTICK = REGISTRATE
		.block("sidestick", p -> new SidestickBlock(p, DyeColor.BLUE) )
		.lang("Sidestick")
		.initialProperties(SharedProperties::wooden)
		.properties(p -> p.mapColor(DyeColor.BLUE))

		.recipe((c, p) -> {
			ShapedRecipeBuilder.shaped(RecipeCategory.MISC, c.get(), 1)
				.pattern("L")
				.pattern("W")
				.define("L", SimBlocks.THROTTLE_LEVER)
				.define("W", DyeHelper.getWoolOfDye(DyeColor.BLUE))
				.group("createkineticlift:sidestick")
				.unlockedBy("has_ingredient", RegistrateRecipeProvider.has(ItemTags.WOOL))
				.save(p);
			ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, c.get())
				.group("createkineticlift:sidestick")
				.requires(SimTags.Items.dyesTag(color))
		}


		.item()
		.build()
		.register();

	public static final DyedBlockList<SidestickBlock> DYED_SIDESTICK_BLOCKS = new DyedBlockList<>(color -> {
		String colorName = color.getSerializedName();
		if (color == DyeColor.BLUE) {
			return BLUE_SIDESTICK_BLOCK;
		} else {
			return REGISTRATE.block(colorName + "_sidestick", p -> new SidestickBock(p,color))
				.lang(RegistrateLangProvider.toEnglishName(color.getName()) + "Sidestick")
		}
	)

	public static void register() {
	}
}
