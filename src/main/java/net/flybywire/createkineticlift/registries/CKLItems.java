package net.flybywire.createkineticlift.registries;

import static net.flybywire.createkineticlift.CreateKineticLift.REGISTRATE;

import com.simibubi.create.foundation.data.AssetLookup;
import com.tterrag.registrate.util.entry.ItemEntry;

import net.minecraft.world.item.Item;

public class CKLItems {
	public static void register() {
	}

	public static final ItemEntry<Item> IRON_BLADE = REGISTRATE
		.item("iron_blade", Item::new)
		.lang("Iron Blade")
		.model(AssetLookup.existingItemModel())
		.register();

	public static final ItemEntry<Item> RED_CABLE = REGISTRATE
		.item("red_cable", Item::new)
		.lang("Red Cable")
		.model(AssetLookup.existingItemModel())
		.register();
}
