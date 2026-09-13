package net.flybywire.createkineticlift.registries;

import com.jcraft.jorbis.Block;
import com.simibubi.create.foundation.data.TagGen;
import com.tterrag.registrate.providers.ProviderType;

import com.tterrag.registrate.providers.RegistrateItemTagsProvider;
import com.tterrag.registrate.providers.RegistrateTagsProvider;

import net.flybywire.createkineticlift.CreateKineticLift;

import net.createmod.catnip.lang.Lang;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

public class CKLTags {
	public static void addGenerators() {
		CreateKineticLift.getRegistrate().addDataGenerator(ProviderType.BLOCK_TAGS, BlockTags::genBlockTags);
		CreateKineticLift.getRegistrate().addDataGenerator(ProviderType.ItemTags, ItemTags::genItemTags);
	}

	public static class BlockTags {
		public static final TagKey<Block> SIDESTICK = create("sidestick");
		private static TagKey<Block> create(final String path) {
			return TagKey.create(Registries.BLOCK, CreateKineticLift.path));
		}

		private static void genBlockTags(final RegistrateTagsProvider<Block> provIn) {
			final TagGen.CreateTagsProvider<Block> prov = new TagGen.CreateTagsProvider<>(provIn, Block::builtInRegistryHolder);
	}

		public static class ItemTags {
			public static final TagKey<Item> SIDESTICK = create("sidestick");
			private static TagKey<Item> create(final String path) {
				return TagKey.create(Registries.ITEM, CreateKineticLift.path(path));
			}

			public static void genItemTags(final RegistrateItemTagsProvider provIn) {
				final TagGen.CreateTagsProvider<Item> prov = new TagGen.CreateTagsProvider<>(provIn, Item::builtInRegistryHolder);

			}

	public enum CKLFluidTags {

		TURBOFAN_FUELS;

		public final TagKey<Fluid> tag;

		CKLFluidTags() {
			tag = TagKey.create(Registries.FLUID, CreateKineticLift.asResource(Lang.asId(name())));
		}

		@SuppressWarnings("deprecation")
		public boolean matches(Fluid fluid) {
			return fluid.is(tag);
		}

		public boolean matches(FluidState state) {
			return state.is(tag);
		}
	}
}
