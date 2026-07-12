package net.flybywire.createkineticlift.content.turbofan;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.content.kinetics.fan.AirCurrent;
import com.simibubi.create.content.kinetics.fan.IAirCurrentSource;
import com.simibubi.create.content.kinetics.fan.processing.FanProcessingType;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class TurbofanExhaustAirCurrent extends AirCurrent {

	private static final float FIRE_DURATION_SECONDS = 4.0f;

	private final FanProcessingType exhaustEffect = new TurbofanExhaustEffect();

	public TurbofanExhaustAirCurrent(IAirCurrentSource source) {
		super(source);
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

		return exhaustEffect;
	}

	private static class TurbofanExhaustEffect
		implements FanProcessingType {

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
			if (!level.isClientSide)
				entity.igniteForSeconds(FIRE_DURATION_SECONDS);
		}
	}
}
