package net.flybywire.createkineticlift.jet;

import org.joml.Vector3d;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Thread-safe handler for queuing turbofan forces to be applied to VS2 ships.
 *
 * Forces are queued from the game thread and consumed by the physics thread.
 * This ensures thread safety when interacting with VS2's physics system.
 */
public class TurbofanPhysicsHandler {

    // Queue of forces to be applied, keyed by ship ID
    private static final Map<Long, ConcurrentLinkedQueue<ForceData>> pendingForces = new ConcurrentHashMap<>();

    /**
     * Queue a force to be applied to a ship
     *
     * @param shipId   The VS2 ship ID
     * @param force    Force vector in model space
     * @param position Position in model space where force is applied
     */
    public static void queueForce(long shipId, Vector3d force, Vector3d position) {
        pendingForces.computeIfAbsent(shipId, k -> new ConcurrentLinkedQueue<>())
                .add(new ForceData(force, position));
    }

    /**
     * Get and clear all pending forces for a ship
     *
     * @param shipId The ship ID
     * @return Queue of pending forces, or null if none
     */
    public static ConcurrentLinkedQueue<ForceData> consumeForces(long shipId) {
        return pendingForces.remove(shipId);
    }

    /**
     * Check if a ship has pending forces
     */
    public static boolean hasPendingForces(long shipId) {
        ConcurrentLinkedQueue<ForceData> queue = pendingForces.get(shipId);
        return queue != null && !queue.isEmpty();
    }

    /**
     * Clear all pending forces (for cleanup)
     */
    public static void clearAllForces() {
        pendingForces.clear();
    }

    /**
     * Data class for force information
     */
    public static class ForceData {
        public final Vector3d force;
        public final Vector3d position;

        public ForceData(Vector3d force, Vector3d position) {
            this.force = new Vector3d(force);
            this.position = new Vector3d(position);
        }
    }
}
