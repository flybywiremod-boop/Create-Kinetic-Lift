package net.flybywire.createkineticlift.foundation.data;

import com.simibubi.create.api.data.recipe.DeployingRecipeGen;

import com.simibubi.create.foundation.utility.DyeHelper;

import dev.simulated_team.simulated.index.SimBlocks;
import io.netty.util.concurrent.CompleteFuture;

import net.flybywire.createkineticlift.CreateKineticLift;

import net.flybywire.createkineticlift.registries.CKLBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.NotNull;

public class CKLDeployingRecipes extends DeployingRecipeGen {
	public CKLDeployingRecipes(final PackOutput output, final CompleteFuture<HolderLookup.Provider> registries) {
		super(output, registries, CreateKineticLift.MOD_ID);

		for ( (final DyeColor color : DyeColor.values()) {
			this.create("deploying_sidestick_" + color.getName(), b -> b
				.require(DyeHelper.getWoolOfDye(color))
				.require(SimBlocks.THROTTLE_LEVER)
				.output(CKLBlocks.DYED_SIDESTICK_BLOCKS.get(color), 1)
			);
		}
	}

	@Override
	public @NotNull String getName() {
		return "Aero's Devious Deploying Recipes";
	}
}
