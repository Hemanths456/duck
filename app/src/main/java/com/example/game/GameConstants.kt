package com.example.game

/**
 * Central game configuration constants.
 * Modify these values to adjust duck speed, scoring, starting lives, and difficulty progression!
 */
object GameConstants {
    // ------------------------------------------------------------------------
    // LIVES & HEALTH
    // ------------------------------------------------------------------------
    /** Starting number of player lives (standard is 3) */
    const val INITIAL_LIVES = 3

    // ------------------------------------------------------------------------
    // SCORING
    // ------------------------------------------------------------------------
    /** Points awarded per hit duck */
    const val POINTS_PER_DUCK = 1

    /** Number of points required to advance to the next level */
    const val POINTS_PER_LEVEL = 5

    /** Milestone score interval to trigger the "Great Shot!" celebration banner */
    const val GREAT_SHOT_INTERVAL = 10

    // ------------------------------------------------------------------------
    // DUCK FLIGHT & SPEED
    // ------------------------------------------------------------------------
    /** Base duck horizontal speed in pixels/second at Level 1 */
    const val BASE_DUCK_SPEED = 240f

    /** Additional speed added to ducks per level (higher = faster difficulty ramp) */
    const val SPEED_INCREASE_PER_LEVEL = 38f

    /** Maximum allowed duck speed to keep the game fair on high levels */
    const val MAX_DUCK_SPEED = 720f

    /** Vertical flight bobbing amplitude in pixels */
    const val DUCK_BOB_AMPLITUDE = 14f

    /** Vertical flight bobbing frequency */
    const val DUCK_BOB_FREQUENCY = 4.5f

    /** Wing flapping animation speed multiplier */
    const val WING_FLAP_SPEED = 12f

    // ------------------------------------------------------------------------
    // SPAWNING & CONCURRENCY
    // ------------------------------------------------------------------------
    /** Minimum delay (in seconds) between duck spawns */
    const val MIN_SPAWN_INTERVAL_SEC = 0.8f

    /** Maximum delay (in seconds) between duck spawns */
    const val MAX_SPAWN_INTERVAL_SEC = 2.2f

    /** Maximum ducks allowed on screen simultaneously */
    const val MAX_DUCKS_ON_SCREEN = 3

    // ------------------------------------------------------------------------
    // CONTROLS & HITBOX
    // ------------------------------------------------------------------------
    /** Touch hit radius expansion (in pixels) for generous, responsive mobile shooting */
    const val TOUCH_HIT_TOLERANCE = 45f

    /** Duration (in seconds) to display the "Great Shot!" notification */
    const val GREAT_SHOT_DURATION_SEC = 1.0f

    /** Duration (in seconds) to display the "Duck Escaped!" notification */
    const val ESCAPED_ALERT_DURATION_SEC = 1.0f
}
