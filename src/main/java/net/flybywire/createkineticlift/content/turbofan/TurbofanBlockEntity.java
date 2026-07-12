package net.flybywire.createkineticlift.content.turbofan;

import static net.flybywire.createkineticlift.content.turbofan.AbstractTurbofanCoreBlock.ASSEMBLED;

import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import net.flybywire.createkineticlift.avionics.ControlInput;
import net.flybywire.createkineticlift.avionics.IAvionicsPeripheral;
import net.flybywire.createkineticlift.avionics.PeripheralControl;
import net.flybywire.createkineticlift.content.turbofan.blades.TurbofanBladeItem;
import net.flybywire.createkineticlift.foundation.CKLLang;
import net.flybywire.createkineticlift.foundation.config.CKLConfigs;
import net.flybywire.createkineticlift.registries.CKLTags.CKLFluidTags;

import com.simibubi.create.AllItems;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.fan.AirCurrent;
import com.simibubi.create.content.kinetics.fan.IAirCurrentSource;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.item.SmartInventory;

import dev.ryanhcode.sable.api.block.BlockEntitySubLevelActor;
import dev.ryanhcode.sable.api.physics.force.ForceGroups;
import dev.ryanhcode.sable.api.physics.force.QueuedForceGroup;
import dev.ryanhcode.sable.api.physics.handle.RigidBodyHandle;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import net.createmod.catnip.math.AngleHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public class TurbofanBlockEntity extends SmartBlockEntity
	implements IHaveGoggleInformation, IAvionicsPeripheral, BlockEntitySubLevelActor {

	// Assembly
	public static final int REQUIRED_ASSEMBLY_STAGE = 2;
	public static final int MAX_BLADES = 15;

	// Thrust
	public static final float MAX_THRUST_PN = 14500.0f;
	private static final float REVERSE_THRUST_MULTIPLIER = 0.5f;
	private static final float SPOOL_SMOOTHING = 0.025f;
	private static final float THRUST_SNAP_THRESHOLD_PN = 0.5f;
	private static final float BLADE_EFFICIENCY_EXPONENT = 0.65f;

	// Fuel
	private static final float IDLE_FUEL_CONSUMPTION_MB_PER_SECOND = 1.0f;
	private static final float MAX_FUEL_CONSUMPTION_MB_PER_SECOND = 20.0f;

	// Air currents
	public static final float MAX_INTAKE_AIR_CURRENT_DISTANCE = 24.0f;
	public static final float MAX_EXHAUST_AIR_CURRENT_DISTANCE = 24.0f;

	private static final float MAX_INTAKE_AIR_CURRENT_SPEED = 1024.0f;
	private static final float MAX_EXHAUST_AIR_CURRENT_SPEED = 1536.0f;

	private static final int AIR_CURRENT_REBUILD_INTERVAL = 5;
	private static final int ENTITY_SEARCH_INTERVAL = 2;

	// Throttle input
	private static final float MAX_DISPLAYED_THROTTLE_PERCENT = 105.0f;
	private static final float THROTTLE_TAP_RESPONSE = 0.006f;
	private static final float THROTTLE_LINEAR_RATE = 0.003f;
	private static final float THROTTLE_ACCELERATION = 0.000035f;

	// Client animation
	private static final float IDLE_VISUAL_RPM = 2.0f;
	private static final float MAX_VISUAL_RPM = 256.0f;
	private static final float ROTOR_RPM_SNAP_THRESHOLD = 0.01f;

	private static final Set<PeripheralControl> SUPPORTED_CONTROLS =
		Set.of(
			PeripheralControl.THROTTLE_UP,
			PeripheralControl.THROTTLE_DOWN,
			PeripheralControl.REVERSE_THRUST_TOGGLE,
			PeripheralControl.ENGINE_TOGGLE
		);

	private int assemblyStage;

	private float throttle;
	private float currentThrustPN;

	private double fuelConsumptionAccumulator;

	private boolean reverseThrust;
	private boolean engineRunning;

	private float throttleUpInput;
	private float throttleDownInput;
	private int throttleHoldTicks;
	private float throttleHoldDirection;
	private float throttleHoldStart;

	private int airCurrentRebuildCooldown;
	private int entitySearchCooldown;

	public final TurbofanIntakeAirCurrent intakeAirCurrent;
	public final TurbofanExhaustAirCurrent exhaustAirCurrent;

	final float[] prevBladeAngles = new float[MAX_BLADES];
	final float[] bladeAngles = new float[MAX_BLADES];

	float prevRotorAngle;
	float rotorAngle;

	private float rotorRpm;

	SmartFluidTankBehaviour tank;

	SmartInventory bladeInventory =
		new SmartInventory
			(1, this, (slot, stack) -> stack.getItem() instanceof TurbofanBladeItem)
			.withMaxStackSize(MAX_BLADES);

	public TurbofanBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);

		IAirCurrentSource intakeAirCurrentSource = new IntakeAirCurrentSource();
		IAirCurrentSource exhaustAirCurrentSource = new ExhaustAirCurrentSource();
		intakeAirCurrent = new TurbofanIntakeAirCurrent(this, intakeAirCurrentSource);
		exhaustAirCurrent = new TurbofanExhaustAirCurrent(exhaustAirCurrentSource);
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		tank = SmartFluidTankBehaviour.single(this, 800).forbidExtraction();
		tank.getPrimaryHandler().setValidator(stack -> CKLFluidTags.TURBOFAN_FUELS.matches(stack.getFluid()));
		behaviours.add(tank);
	}

	IFluidHandler getFuelHandler() {
		return tank.getCapability();
	}

	public boolean hasFuel() {
		return !tank.isEmpty();
	}

	public int getBladeCount() {
		return bladeInventory.getStackInSlot(0).getCount();
	}

	public float getThrottle() {
		return throttle;
	}

	public float getThrustPN() {
		return currentThrustPN;
	}

	public float getIntakeAirflowStrength() {
		float rotorStrength = Mth.clamp((rotorRpm - IDLE_VISUAL_RPM) / (MAX_VISUAL_RPM - IDLE_VISUAL_RPM), 0.0f, 1.0f);

		return rotorStrength * getBladeEfficiency();
	}

	public float getExhaustAirflowStrength() {
		float strength = getIntakeAirflowStrength();

		if (reverseThrust)
			strength *= REVERSE_THRUST_MULTIPLIER;

		return strength;
	}

	public boolean isReverseThrust() {
		return reverseThrust;
	}

	public boolean isEngineRunning() {
		return engineRunning;
	}

	public boolean isThrottled() {
		return engineRunning && throttle > 0.0f;
	}

	public float getBladeEfficiency() {
		int bladeCount = getBladeCount();

		if (bladeCount <= 0)
			return 0.0f;

		float bladeRatio = bladeCount / (float) MAX_BLADES;

		return Mth.clamp((float) Math.pow(bladeRatio, BLADE_EFFICIENCY_EXPONENT), 0.0f, 1.0f);
	}

	private float getTargetThrustPN() {
		if (!engineRunning || throttle <= 0.0f)
			return 0.0f;

		float directionMultiplier = reverseThrust ? -REVERSE_THRUST_MULTIPLIER : 1.0f;

		return throttle
			* getBladeEfficiency()
			* CKLConfigs.server().turbofan.maxThrust.getF()
			* directionMultiplier;
	}

	private String getDisplayedThrottle() {
		float displayedThrottle = throttle * MAX_DISPLAYED_THROTTLE_PERCENT;

		int roundedThrottle = Math.round(displayedThrottle);

		if (throttle > 0.0f && roundedThrottle == 0)
			return "<1";

		return Integer.toString(roundedThrottle);
	}

	private float getTargetVisualRotorRpm() {
		if (!engineRunning)
			return 0.0f;

		return Mth.lerp(throttle, IDLE_VISUAL_RPM, MAX_VISUAL_RPM);
	}

	protected boolean addBlade(ItemStack stack) {
		if (!canInteractWithBlades())
			return false;

		stack = bladeInventory.insertItem(0, stack.copyWithCount(1), false);

		return stack.isEmpty();
	}

	protected ItemStack removeBlade() {
		if (!canInteractWithBlades())
			return ItemStack.EMPTY;

		return bladeInventory.extractItem(0, 1, false);
	}

	public boolean canInteractWithBlades() {
		return !engineRunning && currentThrustPN == 0.0f && rotorRpm == 0.0f;
	}

	public int getAssemblyStage() {
		return assemblyStage;
	}

	protected void incrementAssemblyStage() {
		Level level = getLevel();
		BlockPos pos = getBlockPos();
		BlockState state = getBlockState();

		if (level == null || level.isClientSide)
			return;

		if (assemblyStage >= REQUIRED_ASSEMBLY_STAGE)
			return;

		assemblyStage++;
		setChanged();
		sendData();

		if (assemblyStage >= REQUIRED_ASSEMBLY_STAGE)
			assemble(state, level, pos);
	}

	private void assemble(BlockState state, Level level, BlockPos pos) {
		Direction direction = state.getValue(TurbofanIntakeBlock.FACING);

		Direction opposite = direction.getOpposite();

		BlockPos targetPos = pos.relative(opposite, 3);

		BlockState targetState = level.getBlockState(targetPos);

		if (!(targetState.getBlock() instanceof AbstractTurbofanCoreBlock))
			return;

		BlockState newState = state.setValue(ASSEMBLED, true);
		BlockState newTargetState = targetState.setValue(ASSEMBLED, true);

		level.setBlock(pos, newState, 3);
		level.setBlock(targetPos, newTargetState, 3);
	}

	@Override
	public void tick() {
		super.tick();

		if (level == null)
			return;

		if (level.isClientSide) {
			tickRotor();
			tickAirCurrents();
			return;
		}

		boolean changed = tickThrottle();
		changed |= tickFuelConsumption();
		changed |= tickThrust();

		tickRotorRpm();

		if (changed) {
			setChanged();
			sendData();
		}

		tickAirCurrents();
	}

	private boolean tickFuelConsumption() {
		if (!CKLConfigs.server().turbofan.needsFuel.get()) {
			fuelConsumptionAccumulator = 0.0;
			return false;
		}

		if (!engineRunning)
			return false;

		if (!hasFuel()) {
			stopEngine();
			return true;
		}

		fuelConsumptionAccumulator +=
			Mth.lerp(throttle, IDLE_FUEL_CONSUMPTION_MB_PER_SECOND, MAX_FUEL_CONSUMPTION_MB_PER_SECOND) / 20.0;

		int fuelToConsume = (int) fuelConsumptionAccumulator;

		if (fuelToConsume <= 0)
			return false;

		FluidStack drainedFuel =
			tank.getPrimaryHandler().drain(fuelToConsume, IFluidHandler.FluidAction.EXECUTE);

		fuelConsumptionAccumulator -= drainedFuel.getAmount();

		if (drainedFuel.getAmount() < fuelToConsume) {
			stopEngine();
			return true;
		}

		return false;
	}

	private boolean tickThrottle() {
		if (!engineRunning)
			return false;

		float throttleInput = Math.signum(Mth.clamp(getThrottleChangeInput(), -1.0f, 1.0f));

		if (throttleInput == 0.0f) {
			throttleHoldTicks = 0;
			throttleHoldDirection = 0.0f;
			throttleHoldStart = throttle;
			return false;
		}

		if (Float.compare(throttleInput, throttleHoldDirection) != 0) {
			throttleHoldTicks = 0;
			throttleHoldDirection = throttleInput;
			throttleHoldStart = throttle;
		}

		throttleHoldTicks++;

		float change = getThrottleHoldChange(throttleHoldTicks) * throttleInput;

		float previousThrottle = throttle;

		throttle = Mth.clamp(throttleHoldStart + change, 0.0f, 1.0f);

		return Float.compare(previousThrottle, throttle) != 0;
	}

	private float getThrottleHoldChange(int holdTicks) {
		return THROTTLE_TAP_RESPONSE + THROTTLE_LINEAR_RATE * holdTicks + THROTTLE_ACCELERATION * holdTicks * holdTicks;
	}

	private boolean tickThrust() {
		float targetThrustPN = getTargetThrustPN();

		if (Float.compare(currentThrustPN, targetThrustPN) == 0)
			return false;

		float previousThrustPN = currentThrustPN;

		currentThrustPN = Mth.lerp(SPOOL_SMOOTHING, currentThrustPN, targetThrustPN);

		if (Math.abs(currentThrustPN - targetThrustPN) < THRUST_SNAP_THRESHOLD_PN) {
			currentThrustPN = targetThrustPN;
		}

		return Float.compare(previousThrustPN, currentThrustPN) != 0;
	}

	private void tickAirCurrents() {
		if (airCurrentRebuildCooldown-- <= 0) {
			airCurrentRebuildCooldown = AIR_CURRENT_REBUILD_INTERVAL;

			intakeAirCurrent.rebuild();
			exhaustAirCurrent.rebuild();
		}

		if (entitySearchCooldown-- <= 0) {
			entitySearchCooldown = ENTITY_SEARCH_INTERVAL;

			if (intakeAirCurrent.maxDistance > 0.0f)
				intakeAirCurrent.findEntities();

			if (exhaustAirCurrent.maxDistance > 0.0f)
				exhaustAirCurrent.findEntities();
		}

		if (intakeAirCurrent.maxDistance > 0.0f)
			intakeAirCurrent.tick();

		if (exhaustAirCurrent.maxDistance > 0.0f)
			exhaustAirCurrent.tick();
	}

	private void tickRotor() {
		TurbofanSoundInstance.tickSound(this);

		tickBladePositions();
		tickRotorRpm();

		prevRotorAngle = rotorAngle;

		float degreesPerTick = rotorRpm * 360.0f / 60.0f / 20.0f;

		rotorAngle += degreesPerTick;

		if (rotorAngle >= 360.0f) {
			rotorAngle -= 360.0f;
			prevRotorAngle -= 360.0f;
		}
	}

	private void tickRotorRpm() {
		float targetRpm = getTargetVisualRotorRpm();

		rotorRpm = Mth.lerp(SPOOL_SMOOTHING, rotorRpm, targetRpm);

		if (Math.abs(rotorRpm - targetRpm) < ROTOR_RPM_SNAP_THRESHOLD)
			rotorRpm = targetRpm;
	}

	private void tickBladePositions() {
		int bladeCount = getBladeCount();

		if (bladeCount <= 0)
			return;

		float step = 360.0f / bladeCount;

		for (int i = 0; i < MAX_BLADES; i++) {
			prevBladeAngles[i] = bladeAngles[i];

			if (i < bladeCount) {
				float targetAngle = i * step;

				bladeAngles[i] = AngleHelper.angleLerp(0.2f, bladeAngles[i], targetAngle);
			}
		}
	}

	@Override
	public void sable$physicsTick(ServerSubLevel subLevel, RigidBodyHandle handle, double timeStep) {
		double thrustPN = currentThrustPN;

		if (thrustPN == 0.0 || !Double.isFinite(thrustPN) || !Double.isFinite(timeStep) || timeStep <= 0.0)
			return;

		Direction facing = getBlockState().getValue(AbstractTurbofanCoreBlock.FACING);

		Vector3d directionLocal = new Vector3d(facing.getStepX(), facing.getStepY(), facing.getStepZ()).normalize();

		Vector3d applicationPointLocal =
			new Vector3d(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5);

		Vector3d impulseLocal = new Vector3d(directionLocal).mul(thrustPN * timeStep);

		QueuedForceGroup propulsion = subLevel.getOrCreateQueuedForceGroup(ForceGroups.PROPULSION.get());

		propulsion.applyAndRecordPointForce(applicationPointLocal, impulseLocal);
	}

	@Override
	public AABB getRenderBoundingBox() {
		return getActorBounds();
	}

	@Override
	public AABB getActorBounds() {
		BlockState state = getBlockState();

		if (!(state.getBlock() instanceof AbstractTurbofanCoreBlock coreBlock))
			return new AABB(worldPosition);

		if (!state.getValue(ASSEMBLED) || level == null || !coreBlock.hasValidConnection(state, level, worldPosition))
			return new AABB(worldPosition);

		Direction facing = state.getValue(AbstractTurbofanCoreBlock.FACING);

		Direction lengthDirection = facing.getOpposite();

		Vec3 center = Vec3.atCenterOf(worldPosition)
			.add(lengthDirection.getStepX() * 1.5, 0, lengthDirection.getStepZ() * 1.5);

		double halfX = lengthDirection.getAxis() == Direction.Axis.X ? 3.0 : 1.5;
		double halfY = 1.5;
		double halfZ = lengthDirection.getAxis() == Direction.Axis.Z ? 3.0 : 1.5;

		return new AABB(center.x - halfX, center.y - halfY, center.z - halfZ,
			center.x + halfX, center.y + halfY, center.z + halfZ);
	}

	@Override
	public Set<PeripheralControl> getSupportedControls() {
		return SUPPORTED_CONTROLS;
	}

	@Override
	public boolean supportsControl(PeripheralControl control) {
		return SUPPORTED_CONTROLS.contains(control);
	}

	@Override
	public void receiveControl(ControlInput input) {
		if (level == null || level.isClientSide)
			return;

		float value = Math.max(0.0f, input.value());

		switch (input.control()) {
			case THROTTLE_UP -> throttleUpInput = value;
			case THROTTLE_DOWN -> throttleDownInput = value;
			case REVERSE_THRUST_TOGGLE -> {
				if (value > 0.0f)
					toggleReverseThrust();
			}
			case ENGINE_TOGGLE -> {
				if (value > 0.0f)
					toggleEngine();
			}
			default -> {
			}
		}
	}

	private void toggleEngine() {
		if (engineRunning) {
			stopEngine();
		} else {
			if (CKLConfigs.server().turbofan.needsFuel.get() && !hasFuel())
				return;

			engineRunning = true;
		}

		setChanged();
		sendData();
	}

	private void stopEngine() {
		engineRunning = false;
		throttle = 0.0f;
		fuelConsumptionAccumulator = 0.0;
		throttleUpInput = 0.0f;
		throttleDownInput = 0.0f;
		throttleHoldTicks = 0;
		throttleHoldDirection = 0.0f;
		throttleHoldStart = 0.0f;
	}

	private void toggleReverseThrust() {
		reverseThrust = !reverseThrust;

		setChanged();
		sendData();
	}

	public float getThrottleChangeInput() {
		return throttleUpInput - throttleDownInput;
	}

	private final class IntakeAirCurrentSource
		implements IAirCurrentSource {

		@Nullable
		@Override
		public AirCurrent getAirCurrent() {
			return intakeAirCurrent;
		}

		@Nullable
		@Override
		public Level getAirCurrentWorld() {
			return level;
		}

		@Override
		public BlockPos getAirCurrentPos() {
			return worldPosition.relative(getAirflowOriginSide());
		}

		@Override
		public float getSpeed() {
			return getIntakeAirflowStrength() * MAX_INTAKE_AIR_CURRENT_SPEED;
		}

		@Override
		public Direction getAirflowOriginSide() {
			return getBlockState().getValue(AbstractTurbofanCoreBlock.FACING);
		}

		@Override
		public Direction getAirFlowDirection() {
			if (getIntakeAirflowStrength() <= 0.0f)
				return null;

			return getAirflowOriginSide().getOpposite();
		}

		@Override
		public float getMaxDistance() {
			return MAX_INTAKE_AIR_CURRENT_DISTANCE * getIntakeAirflowStrength();
		}

		@Override
		public boolean isSourceRemoved() {
			return TurbofanBlockEntity.this.isRemoved();
		}
	}

	private final class ExhaustAirCurrentSource
		implements IAirCurrentSource {

		@Nullable
		@Override
		public AirCurrent getAirCurrent() {
			return exhaustAirCurrent;
		}

		@Nullable
		@Override
		public Level getAirCurrentWorld() {
			return level;
		}

		@Override
		public BlockPos getAirCurrentPos() {
			return worldPosition.relative(getAirflowOriginSide(), 4);
		}

		@Override
		public float getSpeed() {
			return getExhaustAirflowStrength() * MAX_EXHAUST_AIR_CURRENT_SPEED;
		}

		@Override
		public Direction getAirflowOriginSide() {
			return getBlockState().getValue(AbstractTurbofanCoreBlock.FACING).getOpposite();
		}

		@Override
		public Direction getAirFlowDirection() {
			if (getExhaustAirflowStrength() <= 0.0f)
				return null;

			return getAirflowOriginSide();
		}

		@Override
		public float getMaxDistance() {
			return MAX_EXHAUST_AIR_CURRENT_DISTANCE * getExhaustAirflowStrength();
		}

		@Override
		public boolean isSourceRemoved() {
			return TurbofanBlockEntity.this.isRemoved();
		}
	}

	@Override
	protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		super.write(tag, registries, clientPacket);

		tag.putInt("AssemblyStage", assemblyStage);
		tag.putFloat("Throttle", throttle);
		tag.putFloat("CurrentThrustPN", currentThrustPN);
		tag.putBoolean("ReverseThrust", reverseThrust);
		tag.putBoolean("EngineRunning", engineRunning);
		tag.put("BladeInventory", bladeInventory.serializeNBT(registries));
	}

	@Override
	protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		super.read(tag, registries, clientPacket);

		assemblyStage = tag.getInt("AssemblyStage");
		throttle = tag.getFloat("Throttle");
		currentThrustPN = tag.getFloat("CurrentThrustPN");
		reverseThrust = tag.getBoolean("ReverseThrust");
		engineRunning = tag.getBoolean("EngineRunning");

		if (tag.contains("BladeInventory")) {
			bladeInventory.deserializeNBT(registries, tag.getCompound("BladeInventory"));
		}

		if (clientPacket) {
			intakeAirCurrent.rebuild();
			exhaustAirCurrent.rebuild();
		}
	}

	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		Level level = getLevel();
		BlockPos pos = getBlockPos();
		BlockState state = getBlockState();

		if (level != null
			&& state.getBlock() instanceof TurbofanIntakeBlock turbofanIntakeBlock
			&& !turbofanIntakeBlock.hasValidConnection(state, level, pos))
			return false;

		CKLLang.translate("gui.goggles.turbofan.header")
			.style(ChatFormatting.AQUA)
			.forGoggles(tooltip);

		if (assemblyStage < REQUIRED_ASSEMBLY_STAGE) {
			CKLLang.translate("gui.goggles.turbofan.unfinished")
				.color(0xde5050)
				.forGoggles(tooltip);

			CKLLang.translate("gui.goggles.turbofan.next_component",
					new ItemStack(AllItems.PRECISION_MECHANISM.get()).getHoverName())
				.color(0xfff240)
				.forGoggles(tooltip);

			return true;
		}

		if (CKLConfigs.server().turbofan.needsFuel.get())
			containedFluidTooltip(tooltip, isPlayerSneaking, tank.getPrimaryHandler());

		CKLLang.translate("gui.goggles.turbofan.blades", getBladeCount(), MAX_BLADES)
			.style(ChatFormatting.GRAY)
			.forGoggles(tooltip);

		CKLLang.translate("gui.goggles.turbofan.efficiency", Math.round(getBladeEfficiency() * 100.0f))
			.style(ChatFormatting.GRAY)
			.forGoggles(tooltip);

		CKLLang.translate("gui.goggles.turbofan.engine_state", engineRunning ? "On" : "Off")
			.style(engineRunning ? ChatFormatting.GREEN : ChatFormatting.GRAY)
			.forGoggles(tooltip);

		CKLLang.translate("gui.goggles.turbofan.throttle", getDisplayedThrottle())
			.style(ChatFormatting.AQUA)
			.forGoggles(tooltip);

		CKLLang.translate("gui.goggles.turbofan.thrust", Math.round(currentThrustPN))
			.style(reverseThrust ? ChatFormatting.RED : ChatFormatting.AQUA)
			.forGoggles(tooltip);

		return true;
	}
}
