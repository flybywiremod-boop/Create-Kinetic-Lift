package net.flybywire.createkineticlift.content.turbofan;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.api.equipment.goggles.IHaveHoveringInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import java.util.List;

public class TurbofanBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation, IHaveHoveringInformation {

    // to be changed
    public static final int MAX_BLADES = 25;

    protected TurbofanData turbofanData;
    protected final ItemStackHandler bladeInventory;
    private LazyOptional<IItemHandler> itemHandler;

    public TurbofanBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);

        this.bladeInventory = new ItemStackHandler(MAX_BLADES) {

            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
                sendData();
            }
        };

    }

    public int getBladeCount() {
        int count = 0;
        for (int i = 0; i < bladeInventory.getSlots(); i++) {
            if (!bladeInventory.getStackInSlot(i).isEmpty()) {
                count++;
            }
        }
        return count;
    }

    public boolean addBlade(net.minecraft.world.item.ItemStack heldItem) {
        for (int i = 0; i < bladeInventory.getSlots(); i++) {
            if (bladeInventory.getStackInSlot(i).isEmpty()) {
                net.minecraft.world.item.ItemStack newBlade = heldItem.copy();
                newBlade.setCount(1);
                bladeInventory.setStackInSlot(i, newBlade);
                return true;
            }
        }
        return false;
    }

    public net.minecraft.world.item.ItemStack removeBlade() {
        for (int i = bladeInventory.getSlots() - 1; i >= 0; i--) {
            net.minecraft.world.item.ItemStack stack = bladeInventory.getStackInSlot(i);
            if (!stack.isEmpty()) {
                bladeInventory.setStackInSlot(i, net.minecraft.world.item.ItemStack.EMPTY);
                return stack;
            }
        }
        return net.minecraft.world.item.ItemStack.EMPTY;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
    }

    @Override
    protected void write(CompoundTag compound, boolean clientPacket) {
        super.write(compound, clientPacket);
        compound.put("Blades", bladeInventory.serializeNBT());
    }

    @Override
    protected void read(CompoundTag compound, boolean clientPacket) {
        super.read(compound, clientPacket);
        bladeInventory.deserializeNBT(compound.getCompound("Blades"));
    }

    // Blades animation
    public float[] prevBladeAngles = new float[MAX_BLADES];
    public float[] visualBladeAngles = new float[MAX_BLADES];

    @Override
    public void tick() {
        super.tick();

        if (level != null && level.isClientSide) {
            int count = getBladeCount();
            float angleStep = count > 0 ? 360f / count : 0;

            for (int i = 0; i < MAX_BLADES; i++) {
                prevBladeAngles[i] = visualBladeAngles[i];

                if (i < count) {
                    float targetAngle = i * angleStep;

                    // 0.2f is the speed of the animation, with 1.0f being instantaneous
                    visualBladeAngles[i] = net.createmod.catnip.math.AngleHelper.angleLerp(0.2f, visualBladeAngles[i], targetAngle);
                }
            }
        }
    }
}