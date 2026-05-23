package net.flybywire.createkineticlift.foundation.datagen;

import com.tterrag.registrate.providers.DataGenContext;

import net.flybywire.createkineticlift.content.controlseat.ControlSeatBlock;

import net.minecraft.world.level.block.Block;

import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;

import net.neoforged.neoforge.client.model.generators.ModelFile;

public class CKLBlockStateGen {

	public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> horizontalBlockProvider(boolean customItem) {
		return (ctx, prov) -> {
			prov.horizontalBlock(
				ctx.get(),
				prov.models().getExistingFile(prov.modLoc("block/" + ctx.getName())));
		};
	}

	public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> invertedHorizontalBlockProvider(boolean customItem) {
		return (ctx, prov) -> {
			prov.horizontalBlock(
				ctx.get(),
				state -> {
					return prov.models().getExistingFile(prov.modLoc("block/" + ctx.getName() + (state.getValue(ControlSeatBlock.INVERTED) ? "_inverted" : "")));
				});
		};
	}
}
