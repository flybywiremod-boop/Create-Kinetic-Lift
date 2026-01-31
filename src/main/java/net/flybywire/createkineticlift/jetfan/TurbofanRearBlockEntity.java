package net.flybywire.createkineticlift.jetfan;

import net.flybywire.createkineticlift.block.entity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Rear Turbofan Block Entity
 *
 * Handles:
 * - Reverse thrust (spoiler) mode
 * - Exhaust particle effects
 * - Thrust reverser animation state
 * - Communication with front turbofan
 */
public class TurbofanRearBlockEntity extends BlockEntity {

    private boolean reverseThrust = false;
    private float reverserAngle = 0;
    private static final float REVERSER_SPEED = 5.0f;

    private int particleTimer = 0;
    private static final int PARTICLE_INTERVAL = 2;

    public TurbofanRearBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TURBOFAN_REAR.get(), pos, state);
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        nbt.putBoolean("ReverseThrust", reverseThrust);
        nbt.putFloat("ReverserAngle", reverserAngle);
        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        reverseThrust = nbt.getBoolean("ReverseThrust");
        reverserAngle = nbt.getFloat("ReverserAngle");
    }

    public static void tick(Level level, BlockPos pos, BlockState state, TurbofanRearBlockEntity entity) {
        if (level.isClientSide()) {
            entity.tickClient(level, pos, state);
            return;
        }
        entity.tickServer(level, pos, state);
    }

    private void tickClient(Level level, BlockPos pos, BlockState state) {
        float targetAngle = reverseThrust ? 45.0f : 0.0f;
        if (reverserAngle < targetAngle) {
            reverserAngle = Math.min(targetAngle, reverserAngle + REVERSER_SPEED);
        } else if (reverserAngle > targetAngle) {
            reverserAngle = Math.max(targetAngle, reverserAngle - REVERSER_SPEED);
        }
    }

    private void tickServer(Level level, BlockPos pos, BlockState state) {
        boolean isAssembled = state.getValue(TurbofanRearBlock.ASSEMBLED);
        reverseThrust = state.getValue(TurbofanRearBlock.REVERSE_THRUST);

        if (!isAssembled) {
            TurbofanRearBlock block = (TurbofanRearBlock) state.getBlock();
            if (block.hasFrontPart(level, pos, state)) {
                TurbofanFrontBlockEntity front = getFrontTurbofan();
                if (front != null) {
                    Direction facing = state.getValue(TurbofanRearBlock.FACING);
                    BlockPos frontPos = pos.relative(facing);
                    BlockState frontState = level.getBlockState(frontPos);
                    if (frontState.getBlock() instanceof TurbofanFrontBlock frontBlock) {
                        frontBlock.tryAssemble(level, frontPos, frontState);
                    }
                }
            }
        }

        TurbofanFrontBlockEntity front = getFrontTurbofan();
        if (front != null && front.getCurrentThrust() > 0 && level instanceof ServerLevel serverLevel) {
            particleTimer++;
            if (particleTimer >= PARTICLE_INTERVAL) {
                particleTimer = 0;
                spawnExhaustParticles(serverLevel, pos, state, front.getCurrentThrust());
            }

            // Apply thrust force to VS2 ship if on one
            TurbofanForceApplier.applyThrustForce(level, pos, state, this);
        }

        setChanged();
    }

    private void spawnExhaustParticles(ServerLevel level, BlockPos pos, BlockState state, float thrust) {
        Direction facing = state.getValue(TurbofanRearBlock.FACING);
        Vec3 center = Vec3.atCenterOf(pos);

        Vec3 exhaustOffset = Vec3.atLowerCornerOf(facing.getOpposite().getNormal()).scale(0.8);
        Vec3 exhaustPos = center.add(exhaustOffset);

        float thrustRatio = thrust / TurbofanFrontBlockEntity.MAX_THRUST;
        double speed = 0.5 + thrustRatio * 1.5;

        Vec3 velocity;
        if (reverseThrust) {
            velocity = Vec3.atLowerCornerOf(facing.getNormal()).scale(speed * 0.5);
        } else {
            velocity = Vec3.atLowerCornerOf(facing.getOpposite().getNormal()).scale(speed);
        }

        int particleCount = (int) (3 + thrustRatio * 7);
        for (int i = 0; i < particleCount; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * 0.5;
            double offsetY = (level.random.nextDouble() - 0.5) * 0.5;
            double offsetZ = (level.random.nextDouble() - 0.5) * 0.5;

            level.sendParticles(
                    thrustRatio > 0.7 ? ParticleTypes.FLAME : ParticleTypes.SMOKE,
                    exhaustPos.x + offsetX,
                    exhaustPos.y + offsetY,
                    exhaustPos.z + offsetZ,
                    1,
                    velocity.x * 0.1,
                    velocity.y * 0.1,
                    velocity.z * 0.1,
                    speed * 0.1
            );
        }
    }

    @Nullable
    public TurbofanFrontBlockEntity getFrontTurbofan() {
        if (level == null) return null;

        BlockState state = getBlockState();
        Direction facing = state.getValue(TurbofanRearBlock.FACING);
        BlockPos frontPos = worldPosition.relative(facing);
        BlockEntity be = level.getBlockEntity(frontPos);

        if (be instanceof TurbofanFrontBlockEntity front) {
            return front;
        }
        return null;
    }

    public boolean isReverseThrust() {
        return reverseThrust;
    }

    public void setReverseThrust(boolean reverse) {
        if (level != null && !level.isClientSide()) {
            BlockState state = getBlockState();
            if (state.getValue(TurbofanRearBlock.ASSEMBLED)) {
                level.setBlock(worldPosition, state.setValue(TurbofanRearBlock.REVERSE_THRUST, reverse), 3);
                this.reverseThrust = reverse;
            }
        }
    }

    public float getReverserAngle() {
        return reverserAngle;
    }

    public float getEffectiveThrust() {
        TurbofanFrontBlockEntity front = getFrontTurbofan();
        if (front == null) return 0;

        float thrust = front.getCurrentThrust();
        return reverseThrust ? -thrust * 0.4f : thrust;
    }
}
