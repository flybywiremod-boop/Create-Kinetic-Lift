package net.flybywire.createkineticlift.registries;

import java.util.function.Consumer;

import net.flybywire.createkineticlift.CreateKineticLift;
import net.flybywire.createkineticlift.registries.CKLTags.CKLFluidTags;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.builders.FluidBuilder.FluidTypeFactory;
import com.tterrag.registrate.util.entry.FluidEntry;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.DispensibleContainerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.MapColor;

import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;

public class CKLFluids {

	private static final CreateRegistrate REGISTRATE = CreateKineticLift.REGISTRATE;

	public static final FluidEntry<BaseFlowingFluid.Flowing> KEROSENE = REGISTRATE
		.fluid("kerosene",
			ResourceLocation.withDefaultNamespace("block/water_still"),
			ResourceLocation.withDefaultNamespace("block/water_flow"),
			TranslucentRenderedPlaceableFluidType.create(0xB8D8C36A)
		)
		.lang("Kerosene")
		.properties(properties -> properties
			.density(800)
			.viscosity(1000))
		.fluidProperties(properties -> properties
			.levelDecreasePerBlock(1)
			.tickRate(5)
			.slopeFindDistance(4)
			.explosionResistance(100.0f))
		.renderType(() -> RenderType::translucent)
		.source(BaseFlowingFluid.Source::new)
		.tag(CKLFluidTags.TURBOFAN_FUELS.tag)
		.block()
		.properties(properties -> properties.mapColor(MapColor.TERRACOTTA_YELLOW))
		.build()
		.bucket()
		.onRegister(CKLFluids::registerFluidDispenseBehavior)
		.tag(Tags.Items.BUCKETS)
		.build()
		.register();

	private static final DispenseItemBehavior DEFAULT = new DefaultDispenseItemBehavior();

	private static final DispenseItemBehavior DISPENSE_FLUID = new DefaultDispenseItemBehavior() {
		@Override
		protected ItemStack execute(BlockSource source, ItemStack stack) {
			DispensibleContainerItem container = (DispensibleContainerItem) stack.getItem();
			BlockPos pos = source.pos().relative(source.state().getValue(DispenserBlock.FACING));
			Level level = source.level();

			if (container.emptyContents(null, level, pos, null, stack))
				return new ItemStack(Items.BUCKET);

			return DEFAULT.dispense(source, stack);
		}
	};

	private static void registerFluidDispenseBehavior(BucketItem bucket) {
		DispenserBlock.registerBehavior(bucket, DISPENSE_FLUID);
	}

	public static void register() {
	}

	private static class TranslucentRenderedPlaceableFluidType extends FluidType {

		private final ResourceLocation stillTexture;
		private final ResourceLocation flowingTexture;
		private final int tintColor;

		public static FluidTypeFactory create(int tintColor) {
			return (properties, stillTexture, flowingTexture) ->
				new TranslucentRenderedPlaceableFluidType(properties, stillTexture, flowingTexture, tintColor);
		}

		private TranslucentRenderedPlaceableFluidType(Properties properties, ResourceLocation stillTexture,
													  ResourceLocation flowingTexture, int tintColor) {
			super(properties);
			this.stillTexture = stillTexture;
			this.flowingTexture = flowingTexture;
			this.tintColor = tintColor;
		}

		@SuppressWarnings("removal")
		@Override
		public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
			consumer.accept(new IClientFluidTypeExtensions() {
				@Override
				public ResourceLocation getStillTexture() {
					return stillTexture;
				}

				@Override
				public ResourceLocation getFlowingTexture() {
					return flowingTexture;
				}

				@Override
				public int getTintColor(FluidStack stack) {
					return tintColor;
				}

				@Override
				public int getTintColor(FluidState state, BlockAndTintGetter getter, BlockPos pos) {
					return tintColor;
				}
			});
		}
	}
}
