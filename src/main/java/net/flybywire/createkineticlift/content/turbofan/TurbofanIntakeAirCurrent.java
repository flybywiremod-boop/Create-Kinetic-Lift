package net.flybywire.createkineticlift.content.turbofan;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.flybywire.createkineticlift.foundation.CKLDamageSources;

import com.simibubi.create.content.kinetics.fan.AirCurrent;
import com.simibubi.create.content.kinetics.fan.IAirCurrentSource;
import com.simibubi.create.content.kinetics.fan.processing.FanProcessingType;

import dev.ryanhcode.sable.companion.SableCompanion;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class TurbofanIntakeAirCurrent extends AirCurrent {

	private static final double AIRFLOW_HALF_WIDTH = 1.5;

	private static final float CENTERING_FORCE = 0.24f;
	private static final float LATERAL_VELOCITY_RETENTION = 0.88f;

	private static final float MAX_FAR_LATERAL_SPEED = 0.45f;
	private static final float MAX_NEAR_LATERAL_SPEED = 0.12f;

	private static final float COLLISION_SLOWDOWN_DISTANCE = 0.75f;
	private static final float MAX_INTAKE_APPROACH_SPEED = 0.12f;

	private static final float DAMAGE_RANGE = 1.5f;
	private static final float INTAKE_DAMAGE = 8.0f;

	private final TurbofanBlockEntity turbofan;
	private final FanProcessingType intakeEffect = new TurbofanIntakeEffect();

	public TurbofanIntakeAirCurrent(TurbofanBlockEntity turbofan, IAirCurrentSource source) {
		super(source);
		this.turbofan = turbofan;
	}

	@Override
	public void rebuild() {
		Level level = source.getAirCurrentWorld();
		float requestedDistance = source.getMaxDistance();

		if (level == null || source.getSpeed() == 0.0f || requestedDistance <= 0.0f) {
			reset();
			return;
		}

		direction = source.getAirflowOriginSide();
		pushing = source.getAirFlowDirection() == direction;

		if (direction == null) {
			reset();
			return;
		}

		maxDistance = getFlowLimit(level, source.getAirCurrentPos(), requestedDistance, direction);

		segments.clear();
		affectedItemHandlers.clear();

		if (maxDistance <= 0.0f) {
			bounds = new AABB(0, 0, 0, 0, 0, 0);
			caughtEntities.clear();
			return;
		}

		bounds = createBounds(source.getAirCurrentPos(), direction, maxDistance);
	}

	@Override
	public void tickAffectedHandlers() {
	}

	@Override
	public void findAffectedHandlers() {
	}

	@Nullable
	@Override
	public FanProcessingType getTypeAt(float offset) {
		if (offset < 0.0f || offset > maxDistance)
			return null;

		return intakeEffect;
	}

	private void applyIntakeEffects(Entity entity, Level level) {
		if (direction == null || maxDistance <= 0.0f)
			return;

		float airflowStrength = turbofan.getIntakeAirflowStrength();

		if (airflowStrength <= 0.0f)
			return;

		Vec3 localAxis = Vec3.atLowerCornerOf(direction.getNormal());
		Vec3 localOrigin = Vec3.atCenterOf(source.getAirCurrentPos()).add(localAxis.scale(0.5));

		Vec3 globalOrigin = localOrigin;
		Vec3 globalAxis = localAxis;

		var subLevel = SableCompanion.INSTANCE.getContaining(level, source.getAirCurrentPos());

		if (subLevel != null) {
			var pose = subLevel.logicalPose();

			globalOrigin = pose.transformPosition(localOrigin);

			Vec3 globalAxisEnd = pose.transformPosition(localOrigin.add(localAxis));
			globalAxis = globalAxisEnd.subtract(globalOrigin).normalize();
		}

		Vec3 entityCenter = entity.getBoundingBox().getCenter();

		Vec3 fromOrigin = entityCenter.subtract(globalOrigin);
		double axialDistance = fromOrigin.dot(globalAxis);

		Vec3 centerlinePoint = globalOrigin.add(globalAxis.scale(axialDistance));
		Vec3 centerOffset = centerlinePoint.subtract(entityCenter);

		Vec3 movement = entity.getDeltaMovement();

		double axialSpeed = movement.dot(globalAxis);

		Vec3 axialMovement = globalAxis.scale(axialSpeed);
		Vec3 perpendicularMovement = movement.subtract(axialMovement);

		Vec3 centeredMovement = perpendicularMovement
			.scale(LATERAL_VELOCITY_RETENTION)
			.add(centerOffset.scale(CENTERING_FORCE * airflowStrength));

		float distanceFromIntake = Mth.clamp((float) (axialDistance / COLLISION_SLOWDOWN_DISTANCE), 0.0f, 1.0f);

		float maxCenteringSpeed =
			Mth.lerp(distanceFromIntake, MAX_NEAR_LATERAL_SPEED, MAX_FAR_LATERAL_SPEED) * airflowStrength;

		double centeringSpeed = centeredMovement.length();

		if (centeringSpeed > maxCenteringSpeed)
			centeredMovement = centeredMovement.scale(maxCenteringSpeed / centeringSpeed);

		if (axialDistance <= COLLISION_SLOWDOWN_DISTANCE && axialDistance >= 0.0 && axialSpeed < -MAX_INTAKE_APPROACH_SPEED)
			axialSpeed = -MAX_INTAKE_APPROACH_SPEED;

		entity.setDeltaMovement(globalAxis.scale(axialSpeed).add(centeredMovement));

		entity.fallDistance = 0.0f;

		if (!level.isClientSide && entity instanceof LivingEntity && axialDistance >= 0.0 && axialDistance <= DAMAGE_RANGE)
			entity.hurt(CKLDamageSources.turbofan(level), INTAKE_DAMAGE);
	}

	private void reset() {
		direction = null;
		pushing = false;
		maxDistance = 0.0f;

		bounds = new AABB(0, 0, 0, 0, 0, 0);

		segments.clear();
		affectedItemHandlers.clear();
		caughtEntities.clear();
	}

	private static AABB createBounds(BlockPos pos, Direction direction, float distance) {
		Vec3 directionVec = Vec3.atLowerCornerOf(direction.getNormal());

		Vec3 start = Vec3.atCenterOf(pos).add(directionVec.scale(0.5));
		Vec3 end = start.add(directionVec.scale(distance));

		double minX = Math.min(start.x, end.x);
		double minY = Math.min(start.y, end.y);
		double minZ = Math.min(start.z, end.z);

		double maxX = Math.max(start.x, end.x);
		double maxY = Math.max(start.y, end.y);
		double maxZ = Math.max(start.z, end.z);

		if (direction.getAxis() != Direction.Axis.X) {
			minX -= AIRFLOW_HALF_WIDTH;
			maxX += AIRFLOW_HALF_WIDTH;
		}

		if (direction.getAxis() != Direction.Axis.Y) {
			minY -= AIRFLOW_HALF_WIDTH;
			maxY += AIRFLOW_HALF_WIDTH;
		}

		if (direction.getAxis() != Direction.Axis.Z) {
			minZ -= AIRFLOW_HALF_WIDTH;
			maxZ += AIRFLOW_HALF_WIDTH;
		}

		return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
	}

	private class TurbofanIntakeEffect implements FanProcessingType {

		@Override
		public boolean isValidAt(Level level, BlockPos pos) {
			return false;
		}

		@Override
		public int getPriority() {
			return 0;
		}

		@Override
		public boolean canProcess(ItemStack stack, Level level) {
			return false;
		}

		@Nullable
		@Override
		public List<ItemStack> process(ItemStack stack, Level level) {
			return null;
		}

		@Override
		public void spawnProcessingParticles(Level level, Vec3 pos) {
		}

		@Override
		public void morphAirFlow(AirFlowParticleAccess particleAccess, RandomSource random) {
		}

		@Override
		public void affectEntity(Entity entity, Level level) {
			applyIntakeEffects(entity, level);
		}
	}
}
