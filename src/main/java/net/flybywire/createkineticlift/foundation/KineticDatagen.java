package net.flybywire.createkineticlift.foundation;

import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import net.flybywire.createkineticlift.content.controlseat.ControlSeatBlock;
import net.minecraft.world.level.block.Block;

public class KineticDatagen {

    public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> horizontalBlockStateGen() {
        return (ctx, prov) -> {
            prov.horizontalBlock(
                    ctx.getEntry(),
                    prov.models().getExistingFile(prov.modLoc("block/" + ctx.getName()))
            );
        };
    }

    // Control seat
    public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> invertedHorizontalBlockStateGen() {
        return (ctx, prov) -> {
            prov.horizontalBlock(ctx.getEntry(), state -> {
                boolean inverted = state.getValue(ControlSeatBlock.INVERTED);
                String suffix = inverted ? "_inverted" : "";
                String modelPath = "block/" + ctx.getName() + suffix;
                return prov.models().getExistingFile(prov.modLoc(modelPath));
            });
        };
    }

    // Turbofan intake
    public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> customHorizontalBlockStateGen(String customModelName) {
        return (ctx, prov) -> {
            prov.horizontalBlock(
                    ctx.getEntry(),
                    prov.models().getExistingFile(prov.modLoc("block/" + customModelName))
            );
        };
    }
}