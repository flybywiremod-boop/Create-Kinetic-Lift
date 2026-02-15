package net.flybywire.createkineticlift.registries;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.flybywire.createkineticlift.CreateKineticLift;
import net.flybywire.createkineticlift.content.controlseat.ControlSeatBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

public class KineticBlocks {

    private static final CreateRegistrate REGISTRATE = CreateKineticLift.REGISTRATE;

    public static final BlockEntry<ControlSeatBlock> CONTROL_SEAT = REGISTRATE.block("control_seat",
                    ControlSeatBlock::new)
            .properties(p -> p
                    .mapColor(MapColor.WOOD)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.WOOD)
                    .strength(1.0f)
                    .noOcclusion()
            )
            .simpleItem()
            .register();

    public static void register() {
    }
}

    // Move these in the class in next update ig

//     public static final BlockEntry<Turbofan_Front> TURBOFAN_FRONT = REGISTRATE.block("turbofan_front",
//                     TurbofanFrontBlock::new)
//             .properties(p -> p.mapColor(MapColor.METAL))
//             .properties(p -> p.requiresCorrectToolForDrops())
//             .properties(p -> p.sound(SoundType.METAL))
//             .properties(p -> p.strength(5.5f, 4.0f))
//             .properties(p -> p.noOcclusion())
//             .simpleItem()
//             .register();
//
//
//
//     public static final BlockEntry<TurbofanRear> TURBOFAN_REAR = REGISTRATE.block("turbofan_rear",
//                    TurbofanRearBlock::new)
//            .properties(p -> p.mapColor(MapColor.METAL))
//            .properties(p -> p.requiresCorrectToolForDrops())
//            .properties(p -> p.sound(SoundType.METAL))
//            .properties(p -> p.strength(5.5f, 4.0f))
//            .properties(p -> p.noOcclusion())
//            .simpleItem()
//            .register();
