package net.flybywire.createkineticlift.registries;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.flybywire.createkineticlift.CreateKineticLift;
import net.minecraft.resources.ResourceLocation;

public class KineticPartialModels {

    // Turbofan
    public static final PartialModel TURBOFAN_CONE = partial("turbofan_cone");
    public static final PartialModel IRON_BLADE = partial("iron_blade");

    private static PartialModel partial(String path) {
        return PartialModel.of(ResourceLocation.fromNamespaceAndPath(CreateKineticLift.MOD_ID, "partial/" + path));
    }

    public static void register() {}
}
