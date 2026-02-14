package net.flybywire.createkineticlift.registries;

import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.flybywire.createkineticlift.CreateKineticLift;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;

public class KineticItems {
    private static final CreateRegistrate REGISTRATE = CreateKineticLift.REGISTRATE;

    public static final ItemEntry<Item> RED_CABLE =
            REGISTRATE.item("red_cable", Item::new)
                    .properties(p ->p.stacksTo(1))
                    .model(AssetLookup.itemModelWithPartials())
                    .register();

    public static final ItemEntry<Item> IRON_BLADE =
            REGISTRATE.item("iron_blade", Item::new)
                    .properties(p -> p.stacksTo(1))
                    .model(AssetLookup.itemModelWithPartials())
                    .register();

    public static void register(IEventBus modEventBus) {
    }
}
