package net.flybywire.createkineticlift.Jet;

import net.flybywire.createkineticlift.registries.KineticBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
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
 * Front Turbofan Block Entity
 *
 * Handles:
 * - Fluid storage and consumption (turpentine, kerosene, diesel, naphtha)
 * - Thrust calculation based on fuel type and amount
 * - Assembly state with rear turbofan
 * - Blade rotation speed tracking
 */
public class TurbofanFrontBlockEntity extends BlockEntity implements MenuProvider {

    private static final int TANK_CAPACITY = 2_500_000;
    public static final float MAX_THRUST = 185.0f;
    private static final int FUEL_CONSUMPTION_RATE = 1;

    private float bladeAngle = 0;
    private float bladeSpeed = 0;
    private static final float MAX_BLADE_SPEED = 360.0f;

    private float currentThrust = 0;
    private float targetThrust = 0;

    private final FluidTank fluidTank = new FluidTank(TANK_CAPACITY) {
        @Override
        protected void onContentsChanged() {
            setChanged();
        }

        @Override
        public boolean isFluidValid(FluidStack stack) {
            return isValidFuel(stack);
        }
    };

    private LazyOptional<IFluidHandler> lazyFluidHandler = LazyOptional.empty();

    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> fluidTank.getFluidAmount();
                case 1 -> fluidTank.getCapacity();
                case 2 -> (int) (currentThrust * 100);
                case 3 -> (int) (bladeSpeed * 100);
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {}

        @Override
        public int getCount() {
            return 4;
        }
    };

    public TurbofanFrontBlockEntity(BlockPos pos, BlockState state) {
        super(KineticBlockEntities.TURBOFAN_FRONT.get(), pos, state);
    }

    public static boolean isValidFuel(FluidStack stack) {
        if (stack.isEmpty()) return false;

        String registryName = stack.getFluid().builtInRegistryHolder().key().location().toString().toLowerCase();
        return registryName.contains("naphtha") ||
                registryName.contains("turpentine") ||
                registryName.contains("kerosene") ||
                registryName.contains("diesel");
    }

    public float getFuelEfficiency(FluidStack stack) {
        if (stack.isEmpty()) return 0;

        String registryName = stack.getFluid().builtInRegistryHolder().key().location().toString().toLowerCase();

        if (registryName.contains("kerosene")) {
            return 1.2f;
        }
        if (registryName.contains("naphtha") || registryName.contains("turpentine")) {
            return 1.0f;
        }
        if (registryName.contains("diesel")) {
            return 0.8f;
        }

        return 0.5f;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return lazyFluidHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyFluidHandler = LazyOptional.of(() -> fluidTank);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyFluidHandler.invalidate();
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        nbt.put("FluidTank", fluidTank.writeToNBT(new CompoundTag()));
        nbt.putFloat("CurrentThrust", currentThrust);
        nbt.putFloat("TargetThrust", targetThrust);
        nbt.putFloat("BladeAngle", bladeAngle);
        nbt.putFloat("BladeSpeed", bladeSpeed);
        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        fluidTank.readFromNBT(nbt.getCompound("FluidTank"));
        currentThrust = nbt.getFloat("CurrentThrust");
        targetThrust = nbt.getFloat("TargetThrust");
        bladeAngle = nbt.getFloat("BladeAngle");
        bladeSpeed = nbt.getFloat("BladeSpeed");
    }

    public static void tick(Level level, BlockPos pos, BlockState state, TurbofanFrontBlockEntity entity) {
        if (level.isClientSide()) {
            entity.tickClient();
            return;
        }
        entity.tickServer(level, pos, state);
    }

    private void tickClient() {
        bladeAngle += bladeSpeed;
        if (bladeAngle >= 360) {
            bladeAngle -= 360;
        }
    }

    private void tickServer(Level level, BlockPos pos, BlockState state) {
        boolean isAssembled = state.getValue(TurbofanFrontBlock.ASSEMBLED);
        boolean wasRunning = state.getValue(TurbofanFrontBlock.RUNNING);

        if (!isAssembled) {
            TurbofanFrontBlock block = (TurbofanFrontBlock) state.getBlock();
            if (block.hasRearPart(level, pos, state)) {
                block.tryAssemble(level, pos, state);
                isAssembled = true;
            }
        }

        boolean shouldRun = false;

        if (isAssembled && targetThrust > 0) {
            int toConsume = (int) (FUEL_CONSUMPTION_RATE * (targetThrust / MAX_THRUST));
            FluidStack drained = fluidTank.drain(Math.max(1, toConsume), IFluidHandler.FluidAction.EXECUTE);

            if (!drained.isEmpty()) {
                float efficiency = getFuelEfficiency(drained);
                float thrustIncrement = (MAX_THRUST * efficiency) / 100.0f;
                currentThrust = Math.min(targetThrust, currentThrust + thrustIncrement);
                shouldRun = true;
            } else {
                currentThrust = Math.max(0, currentThrust - 5.0f);
            }
        } else {
            currentThrust = Math.max(0, currentThrust - 5.0f);
        }

        bladeSpeed = (currentThrust / MAX_THRUST) * MAX_BLADE_SPEED;

        if (shouldRun != wasRunning) {
            level.setBlock(pos, state.setValue(TurbofanFrontBlock.RUNNING, shouldRun), 3);
        }

        setChanged();
    }

    public void setTargetThrust(float thrust) {
        this.targetThrust = Math.max(0, Math.min(MAX_THRUST, thrust));
    }

    public float getCurrentThrust() {
        return currentThrust;
    }

    public float getBladeAngle() {
        return bladeAngle;
    }

    public float getBladeSpeed() {
        return bladeSpeed;
    }

    public FluidTank getFluidTank() {
        return fluidTank;
    }

    @Nullable
    public TurbofanRearBlockEntity getRearTurbofan() {
        if (level == null) return null;

        BlockState state = getBlockState();
        if (!state.getValue(TurbofanFrontBlock.ASSEMBLED)) return null;

        Direction facing = state.getValue(TurbofanFrontBlock.FACING);
        BlockPos rearPos = worldPosition.relative(facing.getOpposite());
        BlockEntity be = level.getBlockEntity(rearPos);

        if (be instanceof TurbofanRearBlockEntity rear) {
            return rear;
        }
        return null;
    }

    public void drops() {}

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.createkineticlift.turbofan_front");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return null;
    }
}
