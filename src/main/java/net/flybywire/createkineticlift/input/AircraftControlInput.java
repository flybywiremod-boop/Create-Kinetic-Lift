package net.flybywire.createkineticlift.input;

/**
 * Aircraft control input types for the control seat.
 *
 * Based on MSFS-style controls:
 * - Thrust increase/decrease slowly
 * - W/S for pitch (rear flaps/elevator)
 * - A/D for roll (ailerons/sidestick)
 * - X/Y for yaw (rudder)
 * - R for reverse thrust
 */
public enum AircraftControlInput {
    // Thrust control
    THRUST_INCREASE,
    THRUST_DECREASE,

    // Pitch control (W/S) - affects rear flaps
    PITCH_DOWN,      // W - nose down
    PITCH_UP,        // S - nose up
    PITCH_NEUTRAL,   // Release

    // Roll control (A/D) - sidestick slowly
    ROLL_LEFT,       // A
    ROLL_RIGHT,      // D
    ROLL_NEUTRAL,    // Release

    // Yaw control (X/Y) - rudder
    YAW_LEFT,        // X
    YAW_RIGHT,       // Y
    YAW_NEUTRAL,     // Release

    // Special controls
    REVERSE_THRUST_TOGGLE,  // R
    FLAPS_INCREASE,         // GUI or keybind
    FLAPS_DECREASE,         // GUI or keybind
    SPOILERS_TOGGLE         // Wing spoilers for slowdown
}
