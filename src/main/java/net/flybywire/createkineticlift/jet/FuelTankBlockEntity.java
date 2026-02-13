package net.flybywire.createkineticlift.jet;

import net.flybywire.createkineticlift.registries.KineticBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Fuel Tank Block Entity - Stores fuel for turbofan engines
 *
 * Capacity: 2,500,000 mB per tank
 * Stackable: Yes, tanks connect vertically
 */
public class FuelTankBlockEntity extends BlockEntity {

    public static final int TANK_CAPACITY = 2_500_000;

    private final FluidTank fluidTank = new FluidTank(TANK_CAPACITY) {
        @Override
        protected void onContentsChanged() {
            setChanged();
        }

        @Override
        public boolean isFluidValid(FluidStack stack) {
            // Accept any fluid - turbofan will check if it's valid fuel
            return true;
        }
    };

    private final LazyOptional<IFluidHandler> fluidHandler = LazyOptional.of(() -> fluidTank);

    public FuelTankBlockEntity(BlockPos pos, BlockState state) {
        super(KineticBlockEntities.FUEL_TANK.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, FuelTankBlockEntity entity) {
        // Currently no tick behavior needed
        // Could add fluid equalization between stacked tanks here
    }

    public void onRemoved() {
        // Called when block is broken - could drop contents or handle cleanup
    }

    public FluidTank getFluidTank() {
        return fluidTank;
    }

    public int getFluidAmount() {
        return fluidTank.getFluidAmount();
    }

    public FluidStack getFluid() {
        return fluidTank.getFluid();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return fluidHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        fluidHandler.invalidate();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("FluidTank", fluidTank.writeToNBT(new CompoundTag()));
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("FluidTank")) {
            fluidTank.readFromNBT(tag.getCompound("FluidTank"));
        }
    }
}
