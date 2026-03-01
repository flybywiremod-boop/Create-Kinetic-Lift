package net.flybywire.createkineticlift.registries;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.flybywire.createkineticlift.CreateKineticLift;
import net.flybywire.createkineticlift.content.controlseat.ControlSeatBlock;
import net.flybywire.createkineticlift.content.turbofan.TurbofanExhaustBlock;
import net.flybywire.createkineticlift.content.turbofan.TurbofanIntakeBlock;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import static net.flybywire.createkineticlift.foundation.KineticDatagen.*;

public class KineticBlocks {

    private static final CreateRegistrate REGISTRATE = CreateKineticLift.REGISTRATE;
    public static void register() {}



    public static final BlockEntry<ControlSeatBlock> CONTROL_SEAT =
            REGISTRATE.block("control_seat", ControlSeatBlock::new)
                    .properties(p -> p
                            .mapColor(MapColor.WOOD)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.WOOD)
                            .strength(1.0f)
                            .noOcclusion()
                    )
                    .blockstate(invertedHorizontalBlockStateGen())
                    .simpleItem()
                    .register();

    public static final BlockEntry<TurbofanIntakeBlock> TURBOFAN_INTAKE =
            REGISTRATE.block("turbofan_intake", TurbofanIntakeBlock::new)
                    .properties(p -> p
                            .mapColor(MapColor.METAL)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.METAL)
                            .strength(5.5f, 4.0f)
                            .noOcclusion()
                    )
                    .blockstate(customHorizontalBlockStateGen("turbofan_intake_body"))
                    .simpleItem()
                    .register();

//    public static final BlockEntry<TurbofanIntakeStructuralBlock> TURBOFAN_INTAKE_STRUCTURAL =
//            REGISTRATE.block("turbofan_intake_structure", TurbofanIntakeStructuralBlock::new)
//                    .properties(p -> p
//                            .mapColor(MapColor.METAL)
//                            .requiresCorrectToolForDrops()
//                            .sound(SoundType.METAL)
//                            .strength(5.5f, 4.0f)
//                            .noOcclusion()
//                    )
//                    .blockstate(horizontalBlockStateGen())
//                    .addLayer(() -> RenderType::cutout)
//                    .simpleItem()
//                    .register();

    public static final BlockEntry<TurbofanExhaustBlock> TURBOFAN_EXHAUST =
            REGISTRATE.block("turbofan_exhaust", TurbofanExhaustBlock::new)
                    .properties(p -> p
                            .mapColor(MapColor.METAL)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.METAL)
                            .strength(5.5f, 4.0f)
                            .noOcclusion()
                    )
                    .blockstate(horizontalBlockStateGen())
                    .simpleItem()
                    .register();
}




