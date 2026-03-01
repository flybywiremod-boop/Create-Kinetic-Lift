package net.flybywire.createkineticlift.registries;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.flybywire.createkineticlift.CreateKineticLift;
import net.flybywire.createkineticlift.content.turbofan.blades.TurbofanBladeItem;
import net.minecraft.world.item.Item;

public class KineticItems {
    private static final CreateRegistrate REGISTRATE = CreateKineticLift.REGISTRATE;

    public static final ItemEntry<Item> RED_CABLE = REGISTRATE.item("red_cable", Item::new)
            .properties(p -> p
                    .stacksTo(1)
            )
            .register();

    public static final ItemEntry<TurbofanBladeItem> IRON_BLADE = REGISTRATE.item("iron_blade", TurbofanBladeItem::new)
            .properties(p -> p
                    .stacksTo(1)
            )
            .register();

    public static void register() {
    }
}
