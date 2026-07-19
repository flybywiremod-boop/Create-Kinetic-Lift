package net.flybywire.createkineticlift.registries;

import com.tterrag.registrate.providers.DataGenContext;

import net.flybywire.createkineticlift.CreateKineticLift;
import net.flybywire.createkineticlift.content.cable.ComputerCableItem;
import net.flybywire.createkineticlift.content.turbofan.blades.TurbofanBladeItem;

import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import com.tterrag.registrate.util.entry.ItemEntry;

import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;

public class CKLItems {

	private static final CreateRegistrate REGISTRATE = CreateKineticLift.REGISTRATE;

	public static final ItemEntry<TurbofanBladeItem> IRON_BLADE = REGISTRATE
		.item("iron_blade", TurbofanBladeItem::new)
		.lang("Fan Blade")
		.model(AssetLookup.existingItemModel())
		.recipe((ctx, prov) ->
			ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ctx.get(), 3)
				.pattern("S")
				.pattern("A")
				.define('S', AllItems.IRON_SHEET)
				.define('A', AllItems.ANDESITE_ALLOY)
				.unlockedBy("has_ingredient", RegistrateRecipeProvider.has(AllItems.IRON_SHEET))
				.save(prov)
		)
		.register();

	public static final ItemEntry<ComputerCableItem> COMPUTER_CABLE = REGISTRATE
		.item("computer_cable", ComputerCableItem::new)
		.lang("Computer Cable")
		.model(AssetLookup.existingItemModel())
		.recipe((ctx, prov) ->
			ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ctx.get(), 1)
				.pattern("STS")
				.define('S', AllItems.IRON_SHEET)
				.define('T', AllItems.TRANSMITTER)
				.unlockedBy("has_ingredient", RegistrateRecipeProvider.has(AllItems.IRON_SHEET))
				.save(prov)
		)
		.register();

	public static void register() {
	}
}
