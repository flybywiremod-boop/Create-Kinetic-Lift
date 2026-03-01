package net.flybywire.createkineticlift.registries;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import net.flybywire.createkineticlift.CreateKineticLift;
import net.flybywire.createkineticlift.content.turbofan.TurbofanBlockEntity;
import net.flybywire.createkineticlift.content.turbofan.rendering.TurbofanRenderer;

public class KineticBlockEntities {

    public static final CreateRegistrate REGISTRATE = CreateKineticLift.registrate();
    public static void register() {};

    public static final BlockEntityEntry<TurbofanBlockEntity> TURBOFAN =
            REGISTRATE.blockEntity("turbofan", TurbofanBlockEntity::new)
                    .validBlocks(KineticBlocks.TURBOFAN_INTAKE)
                    .renderer(() -> TurbofanRenderer::new)
                    .register();
}
