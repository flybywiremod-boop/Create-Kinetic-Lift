package net.flybywire.createkineticlift.registries;


import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.flybywire.createkineticlift.CreateKineticLift;
import net.flybywire.createkineticlift.jet.FuelTankBlock;
import net.flybywire.createkineticlift.jet.TurbofanFrontBlock;
import net.flybywire.createkineticlift.jet.TurbofanRearBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;

public class KineticBlocks {
    private static final CreateRegistrate REGISTRATE = CreateKineticLift.REGISTRATE;

    public static final BlockEntry<ControlBlock> CONTROL_CHAIR = REGISTRATE.block("control_chair",
                    ControlBlock::new)
            .properties(p -> p.mapColor(MapColor.WOOD))
            .properties(p -> p.requiresCorrectToolForDrops())
            .properties(p -> p.sound(SoundType.WOOD))
            .properties(p -> p.strength(1.0f))
            .properties(p -> p.noOcclusion())
            .simpleItem()
            .register();

    public static final BlockEntry<TurbofanFrontBlock> TURBOFAN_FRONT = REGISTRATE.block("turbofan_front",
                    TurbofanFrontBlock::new)
            .properties(p -> p.mapColor(MapColor.METAL))
            .properties(p -> p.requiresCorrectToolForDrops())
            .properties(p -> p.sound(SoundType.METAL))
            .properties(p -> p.strength(5.5f,4.0f))
            .properties(p -> p.noOcclusion())
            .simpleItem()
            .register();


    public static final BlockEntry<TurbofanRearBlock> TURBOFAN_REAR = REGISTRATE.block("turbofan_rear",
                    TurbofanRearBlock::new)
            .properties(p -> p.mapColor(MapColor.METAL))
            .properties(p -> p.requiresCorrectToolForDrops())
            .properties(p -> p.sound(SoundType.METAL))
            .properties(p -> p.strength(5.5f, 4.0f))
            .properties(p -> p.noOcclusion())
            .simpleItem()
            .register();

    public static final BlockEntry<FuelTankBlock> FUEL_TANK = REGISTRATE.block("fuel_tank",
                    FuelTankBlock::new)
            .properties(p -> p.mapColor(MapColor.METAL))
            .properties(p -> p.requiresCorrectToolForDrops())
            .properties(p -> p.sound(SoundType.METAL))
            .properties(p -> p.strength(3.0f))
            .properties(p -> p.noOcclusion())
            .simpleItem()
            .register();

    public static void register(IEventBus eventBus) {

    }
}
