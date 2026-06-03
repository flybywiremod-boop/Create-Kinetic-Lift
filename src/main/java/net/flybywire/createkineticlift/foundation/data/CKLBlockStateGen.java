package net.flybywire.createkineticlift.foundation.data;

import net.flybywire.createkineticlift.content.controlseat.ControlSeatBlock;

import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;

import net.minecraft.world.level.block.Block;

public class CKLBlockStateGen {

	public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> horizontalBlockProvider() {
		return (ctx, prov) -> {
			prov.horizontalBlock(
				ctx.get(),
				prov.models().getExistingFile(prov.modLoc("block/" + ctx.getName())));
		};
	}

	public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> invertedHorizontalBlockProvider() {
		return (ctx, prov) -> {
			prov.horizontalBlock(
				ctx.get(),
				state -> {
					return prov.models().getExistingFile(prov.modLoc("block/" + ctx.getName() + (state.getValue(ControlSeatBlock.INVERTED) ? "_inverted" : "")));
				});
		};
	}

	// Turbofan intake
	public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> customHorizontalBlockProvider(String customModelName) {
		return (ctx, prov) -> {
			prov.horizontalBlock(
				ctx.get(),
				prov.models().getExistingFile(prov.modLoc("block/" + customModelName))
			);
		};
	}
}
