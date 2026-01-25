package net.flybywire.createkineticlift.util;

import net.flybywire.createkineticlift.CreateKineticLift;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class CreateKineticLiftTags {
    public static class Blocks {
        public static final TagKey<Block> CONTROL_CHAIR = tag("control_chair");


        private  static TagKey<Block> tag(String name) {
            return BlockTags.create(new ResourceLocation(CreateKineticLift.MOD_ID, name));
        }
    }

    public static class items {

        private static TagKey<Item> tag(String name) {
            return ItemTags.create(new ResourceLocation(CreateKineticLift.MOD_ID, name));
        }
    }
}
