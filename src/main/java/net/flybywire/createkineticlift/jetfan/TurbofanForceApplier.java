package net.flybywire.createkineticlift.jetfan;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import org.valkyrienskies.mod.common.util.GameToPhysicsAdapter;

/**
 * Handles applying turbofan thrust forces to Valkyrien Skies ships.
 *
 * Thrust output: 185 Bps (blocks per second) max
 * Force calculation: thrust * FORCE_MULTIPLIER
 *
 * Uses VS2's GameToPhysicsAdapter for proper thread-safe force application.
 * Forces are applied in Model Space so they rotate with the ship.
 */
public class TurbofanForceApplier {

    // Force multiplier to convert thrust (Bps) to Newtons for VS2
    // Adjusted for realistic aircraft-like behavior
    private static final double FORCE_MULTIPLIER = 1000.0;

    /**
     * Apply thrust force from a turbofan to its containing ship
     *
     * @param level     The world level
     * @param rearPos   Position of the rear turbofan block
     * @param rearState BlockState of the rear turbofan
     * @param rear      The rear turbofan block entity
     */
    public static void applyThrustForce(Level level, BlockPos rearPos, BlockState rearState, TurbofanRearBlockEntity rear) {
        if (level.isClientSide()) return;
        if (!(level instanceof ServerLevel serverLevel)) return;

        // Get the ship this block belongs to
        Ship ship = VSGameUtilsKt.getShipManagingPos(level, rearPos);
        if (ship == null) return;

        // Get effective thrust (negative if reverse thrust)
        float effectiveThrust = rear.getEffectiveThrust();
        if (Math.abs(effectiveThrust) < 0.01f) return;

        // Get facing direction of the turbofan
        Direction facing = rearState.getValue(TurbofanRearBlock.FACING);

        // Calculate force direction in Model Space
        // Thrust pushes in the facing direction (engine faces forward, pushes forward)
        Vector3d forceDirection = new Vector3d(
                facing.getStepX(),
                facing.getStepY(),
                facing.getStepZ()
        );

        // Force magnitude
        double forceMagnitude = effectiveThrust * FORCE_MULTIPLIER;

        // Final force vector in Model Space
        Vector3d force = forceDirection.mul(forceMagnitude);

        // Position in Model Space (ship-local coordinates, center of the block)
        Vector3d position = new Vector3d(
                rearPos.getX() + 0.5,
                rearPos.getY() + 0.5,
                rearPos.getZ() + 0.5
        );

        // Apply force using VS2's GameToPhysicsAdapter
        // This queues the force to be applied in the physics thread
        applyModelForceToShip(serverLevel, ship, force, position);
    }

    /**
     * Apply a force in Model Space to a ship at a Model Space position.
     * Model Space forces rotate with the ship - perfect for thrusters.
     *
     * @param level    The server level
     * @param ship     The ship to apply force to
     * @param force    Force vector in Model Space
     * @param position Position in Model Space where force is applied
     */
    private static void applyModelForceToShip(ServerLevel level, Ship ship, Vector3d force, Vector3d position) {
        // Get the dimension ID for this level
        String dimensionId = VSGameUtilsKt.getDimensionId(level);

        // Get VS2's force adapter for this dimension
        GameToPhysicsAdapter forceApplier = ValkyrienSkiesMod.getOrCreateGTPA(dimensionId);

        // Apply force in Model Space at the specified Model Space position
        // This means the force rotates with the ship - essential for engines
        forceApplier.applyModelForce(ship.getId(), force, position);
    }

    /**
     * Calculate the thrust vector for display/debug purposes
     */
    public static Vector3d calculateThrustVector(TurbofanRearBlockEntity rear, BlockState state) {
        float thrust = rear.getEffectiveThrust();
        Direction facing = state.getValue(TurbofanRearBlock.FACING);

        return new Vector3d(
                facing.getStepX() * thrust,
                facing.getStepY() * thrust,
                facing.getStepZ() * thrust
        );
    }
}
