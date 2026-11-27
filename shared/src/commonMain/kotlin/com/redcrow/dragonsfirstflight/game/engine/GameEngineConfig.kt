package com.redcrow.dragonsfirstflight.game.engine

/** Defines flight physics, spawn rules and the Normal timeline in game pixels and seconds. */
object GameEngineConfig {
    const val INITIAL_LIVES: Int = 3
    const val MAX_LIVES: Int = 5
    const val POINTS_PER_SECOND: Int = 1
    const val ZONE_DURATION_SECONDS: Double = 20.0
    const val ZONE_COUNT: Int = 4
    const val BOSS_START_SECONDS: Double = ZONE_DURATION_SECONDS * (ZONE_COUNT - 1)
    const val BOSS_STAGE_DURATION_SECONDS: Double = 20.0
    const val BOSS_DURATION_SECONDS: Double = BOSS_STAGE_DURATION_SECONDS * 3
    const val BOSS_FURY_DURATION_SECONDS: Double = 10.0

    /** Measures the start of fury in seconds from boss entry. */
    const val BOSS_FURY_START_SECONDS: Double = BOSS_DURATION_SECONDS - BOSS_FURY_DURATION_SECONDS
    const val TIME_TO_WIN_SECONDS: Double =
        BOSS_START_SECONDS + BOSS_DURATION_SECONDS
    const val ZONE_TRANSITION_DURATION_SECONDS: Double = 2.0
    const val EXTRA_LIFE_SIZE: Double = 56.0

    /** Limits extra-life spawns per Normal run or per Endless extra-life window. */
    const val EXTRA_LIFE_MAX_PER_RUN: Int = 2
    const val ENDLESS_EXTRA_LIFE_WINDOW_SECONDS: Double = 60.0
    const val EXTRA_LIFE_FIRST_ATTEMPT_SECONDS: Double = 5.0
    const val EXTRA_LIFE_MIN_INTERVAL_SECONDS: Double = 18.0

    /** Horizontal gap in pixels from the obstacle's right edge to the pickup's left edge. */
    const val EXTRA_LIFE_OBSTACLE_GAP: Double = 72.0

    /** Maximum distance in pixels inside the right boundary at which a new obstacle can trigger a spawn. */
    const val EXTRA_LIFE_SPAWN_EDGE_TOLERANCE: Double = 24.0

    /** Minimum vertical separation in pixels between the pickup lane and the spawning obstacle's Y position. */
    const val EXTRA_LIFE_SAFE_LANE_DISTANCE: Double = 96.0

    const val FIRE_POWER_UP_SIZE: Double = 56.0
    const val FIRE_POWER_UP_DURATION_SECONDS: Double = 8.0
    const val FIRE_POWER_UP_FIRST_ATTEMPT_SECONDS: Double = 4.0
    const val FIRE_POWER_UP_MIN_INTERVAL_SECONDS: Double = 28.0

    /** Horizontal gap in pixels from the obstacle's right edge to the pickup's left edge. */
    const val FIRE_POWER_UP_OBSTACLE_GAP: Double = 72.0

    /** Maximum distance in pixels inside the right boundary at which a new obstacle can trigger a spawn. */
    const val FIRE_POWER_UP_SPAWN_EDGE_TOLERANCE: Double = 24.0

    /** Minimum vertical separation in pixels between the pickup lane and the spawning obstacle's Y position. */
    const val FIRE_POWER_UP_SAFE_LANE_DISTANCE: Double = 96.0
    const val ENDLESS_FIRE_POWER_UP_WINDOW_SECONDS: Double = 60.0
    const val FIRE_FLAME_SIZE: Double = 132.0
    const val FIRE_BALL_SIZE: Double = 48.0
    const val FIRE_BALL_SPEED: Double = 520.0
    const val FIRE_BALL_INTERVAL_SECONDS: Double = 0.42

    const val SHIELD_POWER_UP_SIZE: Double = 56.0
    const val SHIELD_POWER_UP_DURATION_SECONDS: Double = 8.0
    const val SHIELD_POWER_UP_FIRST_ATTEMPT_SECONDS: Double = 4.0
    const val SHIELD_POWER_UP_MIN_INTERVAL_SECONDS: Double = 28.0

    /** Horizontal gap in pixels from the obstacle's right edge to the pickup's left edge. */
    const val SHIELD_POWER_UP_OBSTACLE_GAP: Double = 72.0

    /** Maximum distance in pixels inside the right boundary at which a new obstacle can trigger a spawn. */
    const val SHIELD_POWER_UP_SPAWN_EDGE_TOLERANCE: Double = 24.0

    /** Minimum vertical separation in pixels between the pickup lane and the spawning obstacle's Y position. */
    const val SHIELD_POWER_UP_SAFE_LANE_DISTANCE: Double = 96.0
    const val ENDLESS_SHIELD_POWER_UP_WINDOW_SECONDS: Double = 60.0
    const val GREEN_SHIELD_SIZE: Double = 116.0

    const val RAGE_POWER_UP_SIZE: Double = 56.0
    const val RAGE_POWER_UP_DURATION_SECONDS: Double = 8.0
    const val RAGE_POWER_UP_FIRST_ATTEMPT_SECONDS: Double = 4.0
    const val RAGE_POWER_UP_MIN_INTERVAL_SECONDS: Double = 28.0

    /** Horizontal gap in pixels from the obstacle's right edge to the pickup's left edge. */
    const val RAGE_POWER_UP_OBSTACLE_GAP: Double = 72.0

    /** Maximum distance in pixels inside the right boundary at which a new obstacle can trigger a spawn. */
    const val RAGE_POWER_UP_SPAWN_EDGE_TOLERANCE: Double = 24.0

    /** Minimum vertical separation in pixels between the pickup lane and the spawning obstacle's Y position. */
    const val RAGE_POWER_UP_SAFE_LANE_DISTANCE: Double = 96.0
    const val ENDLESS_RAGE_POWER_UP_WINDOW_SECONDS: Double = 60.0

    /** Scales obstacle scrolling and time-based point accrual during rage. */
    const val RAGE_SPEED_MULTIPLIER: Double = 2.5

    /** Returns the configured fire pickup probability for zone one and zero for the remaining zones. */
    fun firePowerUpChance(zone: GameZone): Double =
        when (zone) {
            GameZone.ZONE_ONE -> 0.20
            else -> 0.0
        }

    /** Returns the probability used to attempt an extra-life spawn at an eligible obstacle. */
    fun extraLifeChance(zone: GameZone): Double =
        when (zone) {
            GameZone.ZONE_ONE -> 0.16
            GameZone.ZONE_TWO -> 0.18
            GameZone.ZONE_THREE -> 0.20
            GameZone.BOSS -> 0.0
        }

    const val CATAPULT_SIZE: Double = 512.0
    const val CATAPULT_GROUND_Y: Double = 660.0

    // Anchor the wheels rather than the transparent bottom of the sprite cell.
    const val CATAPULT_GROUND_ANCHOR_RATIO: Double = 332.0 / 384.0

    const val CATAPULT_RIGHT_MARGIN: Double = 24.0
    const val CATAPULT_ENTRY_DURATION_SECONDS: Double = 2.0
    const val CATAPULT_ATTACK_INTERVAL_SECONDS: Double = 2.0
    const val CATAPULT_FURY_ATTACK_INTERVAL_SECONDS: Double = 0.9
    const val CATAPULT_FURY_LOAD_FRAME_SECONDS: Double = 0.11
    const val CATAPULT_LOAD_FRAME_SECONDS: Double = 0.18
    const val CATAPULT_WARNING_SECONDS: Double = 1.2
    const val CATAPULT_FIRE_SECONDS: Double = 0.12
    const val CATAPULT_RECOVERY_FRAME_SECONDS: Double = 0.16

    /** Projectile launch center measured from the catapult's left edge as a fraction of its sprite width. */
    const val CATAPULT_RELEASE_X_FRACTION: Double = 0.20

    /** Projectile launch center measured from the catapult's top edge as a fraction of its sprite height. */
    const val CATAPULT_RELEASE_Y_FRACTION: Double = 0.40
    const val BOSS_PROJECTILE_SIZE: Double = 64.0
    const val BOSS_PROJECTILE_SPEED: Double = 400.0
    const val BOSS_DOUBLE_SHOT_DELAY_SECONDS: Double = 0.35
    const val BOSS_ARC_GRAVITY: Double = 300.0
    const val BOSS_ARC_FLIGHT_SECONDS: Double = 1.8
    const val BOSS_PROJECTILE_FLOAT_AMPLITUDE: Double = 16.0
    const val BOSS_PROJECTILE_FLOAT_CYCLES_PER_SECOND: Double = 0.52

    /** Horizontal travel in pixels over which a non-arc boss projectile moves into its target lane. */
    const val BOSS_FORMATION_DISTANCE: Double = 160.0
    const val BOSS_SAFE_LANE_HEIGHT: Double = 184.0
    const val BOSS_WALL_SPACING: Double = 48.0

    const val GAME_AREA_HEIGHT: Double = 720.0

    const val DRAGON_X: Double = 140.0
    const val DRAGON_INITIAL_Y: Double = 320.0
    const val DRAGON_SIZE: Double = 88.0

    const val DRAGON_MIN_Y: Double = 155.0
    const val DRAGON_MAX_Y: Double = 560.0

    /** Downward dragon acceleration in game pixels per second squared. */
    const val GRAVITY: Double = 600.0

    /** Upward speed applied by a flap, in game pixels per second along the downward-positive Y axis. */
    const val FLAP_VELOCITY: Double = -240.0

    /** Maximum downward dragon speed in game pixels per second. */
    const val MAX_FALL_VELOCITY: Double = 360.0

    const val OBSTACLE_Y: Double = 328.0
    const val OBSTACLE_SIZE: Double = 72.0
    const val ZONE_TWO_FLYING_GARGOYLE_SIZE: Double = 96.0
    const val ZONE_TWO_GHOST_KNIGHT_SIZE: Double = 96.0
    const val KNIGHT_SWORD_SIZE: Double = 120.0
    const val INITIAL_OBSTACLE_MARGIN: Double = 90.0
    const val RUSH_WARNING_DURATION_SECONDS: Double = 0.80
    const val MIN_OBSTACLE_SEPARATION: Double = 285.0
    const val ZONE_TWO_MIN_OBSTACLE_SEPARATION: Double = 270.0
    const val ZONE_THREE_MIN_OBSTACLE_SEPARATION: Double = 240.0
    const val OBSTACLE_TOP_Y: Double = 190.0
    const val OBSTACLE_BOTTOM_Y: Double = 580.0
    const val OBSTACLE_LOWER_MIDDLE_Y: Double = 454.0
    const val OBSTACLE_SPEED: Double = 135.0
    const val MAX_OBSTACLE_SPEED: Double = 195.0
    const val ZONE_TWO_OBSTACLE_SPEED: Double = MAX_OBSTACLE_SPEED
    const val ZONE_TWO_MAX_OBSTACLE_SPEED: Double = 240.0
    const val ZONE_THREE_OBSTACLE_SPEED: Double =
        ZONE_TWO_MAX_OBSTACLE_SPEED
    const val ZONE_THREE_MAX_OBSTACLE_SPEED: Double = 300.0

    /** Candidate vertical positions in game pixels for extra lives and power-ups. */
    val extraLifeLanes: List<Double> =
        listOf(OBSTACLE_TOP_Y, OBSTACLE_Y, OBSTACLE_LOWER_MIDDLE_Y, OBSTACLE_BOTTOM_Y)

    /** Limits movement and effect timers to one simulation step per update, even after a long frame. */
    const val MAX_DELTA_SECONDS: Double = 0.05
    const val IMPACT_FEEDBACK_DURATION_SECONDS: Double = 0.18
    const val INVULNERABILITY_DURATION_SECONDS: Double = 1.5
    const val BLINK_HALF_PERIOD_SECONDS: Double = 0.2

    const val COIN_SIZE: Double = 44.0
    const val COIN_SPACING: Double = 56.0
    const val COIN_PATTERN_LENGTH: Int = 3
    const val COIN_PATTERN_STEP_Y: Double = 24.0
    const val COIN_OBSTACLE_PADDING: Double = 16.0

    /** Maximum distance in pixels inside the right boundary at which a new obstacle can trigger a spawn. */
    const val COIN_SPAWN_EDGE_TOLERANCE: Double = 24.0

    // Extra vertical clearance for obstacles swaying around their lane (largest planned sway: 36 px).
    const val COIN_OBSTACLE_SWAY_CLEARANCE: Double = 40.0

    /**
     * Rows use a per-zone timer (average interval, ±25% jitter) and attach to the next obstacle entering
     * from the right edge. Coins per zone are approximately duration / interval, independent of obstacle
     * speed and separation. Perfect collection yields ~₡46/s in zone 1 (₡300 / 6.5 s), ~₡62/s in zone 2,
     * and ~₡76/s in zone 3. With 20 s zones, a perfect winning run earns ~₡3,700; a typical run ~₡800-1,200.
     * Skin prices are defined by DragonSkin.price. Revisit if zone durations change; rates remain valid.
     */
    const val ZONE_ONE_COIN_ROW_INTERVAL_SECONDS: Double = 6.5
    const val ZONE_TWO_COIN_ROW_INTERVAL_SECONDS: Double = 5.5
    const val ZONE_THREE_COIN_ROW_INTERVAL_SECONDS: Double = 5.0

    /** Maximum fractional variation above or below a zone's coin row interval. */
    const val COIN_ROW_INTERVAL_JITTER: Double = 0.25
    const val ZONE_ONE_FIVE_HUNDRED_CHANCE: Double = 0.0
    const val ZONE_TWO_FIVE_HUNDRED_CHANCE: Double = 0.10
    const val ZONE_THREE_FIVE_HUNDRED_CHANCE: Double = 0.20

    init {
        require(INITIAL_LIVES > 0) {
            "INITIAL_LIVES must be greater than zero."
        }
        require(MAX_LIVES >= INITIAL_LIVES) {
            "MAX_LIVES must not be less than INITIAL_LIVES."
        }
        require(EXTRA_LIFE_MAX_PER_RUN in 1..2) {
            "Extra lives must be limited to one or two per run."
        }

        require(POINTS_PER_SECOND > 0) {
            "POINTS_PER_SECOND must be greater than zero."
        }

        require(TIME_TO_WIN_SECONDS > 0.0) {
            "TIME_TO_WIN_SECONDS must be greater than zero."
        }

        require(ZONE_DURATION_SECONDS > 0.0) {
            "ZONE_DURATION_SECONDS must be greater than zero."
        }

        require(ZONE_TRANSITION_DURATION_SECONDS > 0.0) {
            "ZONE_TRANSITION_DURATION_SECONDS must be greater than zero."
        }

        require(ZONE_COUNT == GameZone.values().size) {
            "ZONE_COUNT must match the number of game zones."
        }

        require(GAME_AREA_HEIGHT > 0.0) {
            "GAME_AREA_HEIGHT must be greater than zero."
        }

        require(DRAGON_SIZE > 0.0) {
            "DRAGON_SIZE must be greater than zero."
        }

        require(DRAGON_MIN_Y >= 0.0) {
            "DRAGON_MIN_Y must not be negative."
        }

        require(DRAGON_MAX_Y > DRAGON_MIN_Y) {
            "DRAGON_MAX_Y must be greater than DRAGON_MIN_Y."
        }

        require(
            DRAGON_MAX_Y + DRAGON_SIZE <= GAME_AREA_HEIGHT,
        ) {
            "The dragon must remain inside the game area."
        }

        require(OBSTACLE_SIZE > 0.0) {
            "OBSTACLE_SIZE must be greater than zero."
        }

        require(MAX_OBSTACLE_SPEED >= OBSTACLE_SPEED) {
            "MAX_OBSTACLE_SPEED must not be less than OBSTACLE_SPEED."
        }

        require(
            ZONE_TWO_MAX_OBSTACLE_SPEED >= ZONE_TWO_OBSTACLE_SPEED,
        ) {
            "Zone two maximum speed must not be less than its initial speed."
        }

        require(
            ZONE_THREE_MAX_OBSTACLE_SPEED >= ZONE_THREE_OBSTACLE_SPEED,
        ) {
            "Zone three maximum speed must not be less than its initial speed."
        }

        require(MIN_OBSTACLE_SEPARATION > OBSTACLE_SIZE) {
            "MIN_OBSTACLE_SEPARATION must be greater than OBSTACLE_SIZE."
        }

        require(
            ZONE_TWO_MIN_OBSTACLE_SEPARATION >
                maxObstacleSize(),
        ) {
            "Zone two separation must keep a safe route."
        }

        require(
            ZONE_THREE_MIN_OBSTACLE_SEPARATION >
                maxObstacleSize(),
        ) {
            "Zone three separation must keep a safe route."
        }

        require(OBSTACLE_TOP_Y >= DRAGON_MIN_Y) {
            "OBSTACLE_TOP_Y must be inside the game area."
        }

        require(
            OBSTACLE_LOWER_MIDDLE_Y > OBSTACLE_Y &&
                OBSTACLE_LOWER_MIDDLE_Y < OBSTACLE_BOTTOM_Y,
        ) {
            "OBSTACLE_LOWER_MIDDLE_Y must be between the middle and bottom lanes."
        }

        require(
            OBSTACLE_BOTTOM_Y + KNIGHT_SWORD_SIZE <= GAME_AREA_HEIGHT,
        ) {
            "OBSTACLE_BOTTOM_Y must be inside the game area."
        }

        require(MAX_DELTA_SECONDS > 0.0) {
            "MAX_DELTA_SECONDS must be greater than zero."
        }

        require(IMPACT_FEEDBACK_DURATION_SECONDS > 0.0) {
            "IMPACT_FEEDBACK_DURATION_SECONDS must be greater than zero."
        }
        require(INVULNERABILITY_DURATION_SECONDS > 0.0) {
            "INVULNERABILITY_DURATION_SECONDS must be greater than zero."
        }
        require(BLINK_HALF_PERIOD_SECONDS > 0.0) {
            "BLINK_HALF_PERIOD_SECONDS must be greater than zero."
        }

        require(COIN_SIZE > 0.0 && COIN_SPACING > 0.0) {
            "COIN_SIZE and COIN_SPACING must be greater than zero."
        }
        require(COIN_PATTERN_LENGTH > 0) {
            "COIN_PATTERN_LENGTH must be greater than zero."
        }
        require(COIN_PATTERN_STEP_Y >= 0.0) {
            "COIN_PATTERN_STEP_Y must not be negative."
        }
        require(COIN_OBSTACLE_PADDING >= 0.0) {
            "COIN_OBSTACLE_PADDING must not be negative."
        }
        require(
            ZONE_ONE_COIN_ROW_INTERVAL_SECONDS > 0.0 &&
                ZONE_TWO_COIN_ROW_INTERVAL_SECONDS > 0.0 &&
                ZONE_THREE_COIN_ROW_INTERVAL_SECONDS > 0.0,
        ) {
            "Coin row intervals must be greater than zero."
        }
        require(COIN_ROW_INTERVAL_JITTER >= 0.0 && COIN_ROW_INTERVAL_JITTER < 1.0) {
            "COIN_ROW_INTERVAL_JITTER must be between zero inclusive and one exclusive."
        }
        require(
            ZONE_ONE_FIVE_HUNDRED_CHANCE in 0.0..1.0 &&
                ZONE_TWO_FIVE_HUNDRED_CHANCE in 0.0..1.0 &&
                ZONE_THREE_FIVE_HUNDRED_CHANCE in 0.0..1.0,
        ) {
            "Coin chances must be between zero and one."
        }
        // A coin row starts at its obstacle and must end (plus padding) before the next obstacle
        // can spawn. Zone three has the smallest separation, so it is the binding case.
        require(
            (COIN_PATTERN_LENGTH - 1) * COIN_SPACING + COIN_SIZE + COIN_OBSTACLE_PADDING <
                ZONE_THREE_MIN_OBSTACLE_SEPARATION,
        ) {
            "A coin pattern must fit between two obstacles."
        }
        // Obstacles spawn up to one frame of travel inside the right boundary.
        require(
            COIN_SPAWN_EDGE_TOLERANCE >
                ZONE_THREE_MAX_OBSTACLE_SPEED * MAX_DELTA_SECONDS,
        ) {
            "COIN_SPAWN_EDGE_TOLERANCE must cover one frame of obstacle travel."
        }
    }

    /** Returns the mean interval between coin rows in seconds, or positive infinity for the boss zone. */
    fun coinRowIntervalAt(zone: GameZone): Double =
        when (zone) {
            GameZone.ZONE_ONE -> ZONE_ONE_COIN_ROW_INTERVAL_SECONDS
            GameZone.ZONE_TWO -> ZONE_TWO_COIN_ROW_INTERVAL_SECONDS
            GameZone.ZONE_THREE -> ZONE_THREE_COIN_ROW_INTERVAL_SECONDS
            GameZone.BOSS -> Double.POSITIVE_INFINITY
        }

    /** Returns the probability that the middle coin of a row is worth 500 colones. */
    fun coinFiveHundredChanceAt(zone: GameZone): Double =
        when (zone) {
            GameZone.ZONE_ONE -> ZONE_ONE_FIVE_HUNDRED_CHANCE
            GameZone.ZONE_TWO -> ZONE_TWO_FIVE_HUNDRED_CHANCE
            GameZone.ZONE_THREE -> ZONE_THREE_FIVE_HUNDRED_CHANCE
            GameZone.BOSS -> 0.0
        }

    /** Maps finite, nonnegative active run time to a Normal zone and stays in the boss zone after victory. */
    fun zoneAt(activeTimeSeconds: Double): GameZone {
        requireValidActiveTime(activeTimeSeconds)

        val zoneIndex =
            (activeTimeSeconds / ZONE_DURATION_SECONDS)
                .toInt()
                .coerceAtMost(GameZone.values().lastIndex)

        return GameZone.values()[zoneIndex]
    }

    /** Returns seconds since entering the current Normal zone, measured from boss entry throughout the encounter. */
    fun zoneElapsedAt(activeTimeSeconds: Double): Double {
        requireValidActiveTime(activeTimeSeconds)

        return if (zoneAt(activeTimeSeconds) == GameZone.BOSS) {
            activeTimeSeconds - BOSS_START_SECONDS
        } else {
            activeTimeSeconds % ZONE_DURATION_SECONDS
        }
    }

    /** Interpolates base scrolling speed in pixels per second within each forest zone and returns zero for the boss. */
    fun obstacleSpeedAt(activeTimeSeconds: Double): Double {
        requireValidActiveTime(activeTimeSeconds)

        val zone = zoneAt(activeTimeSeconds)
        val zoneElapsedSeconds =
            if (activeTimeSeconds >= TIME_TO_WIN_SECONDS) {
                ZONE_DURATION_SECONDS
            } else {
                zoneElapsedAt(activeTimeSeconds)
            }
        val progress =
            (zoneElapsedSeconds / ZONE_DURATION_SECONDS)
                .coerceIn(0.0, 1.0)
        val initialSpeed =
            when (zone) {
                GameZone.ZONE_ONE -> OBSTACLE_SPEED
                GameZone.ZONE_TWO -> ZONE_TWO_OBSTACLE_SPEED
                GameZone.ZONE_THREE -> ZONE_THREE_OBSTACLE_SPEED
                GameZone.BOSS -> 0.0
            }
        val maximumSpeed =
            when (zone) {
                GameZone.ZONE_ONE -> MAX_OBSTACLE_SPEED
                GameZone.ZONE_TWO -> ZONE_TWO_MAX_OBSTACLE_SPEED
                GameZone.ZONE_THREE -> ZONE_THREE_MAX_OBSTACLE_SPEED
                GameZone.BOSS -> 0.0
            }

        return initialSpeed +
            (maximumSpeed - initialSpeed) * progress
    }

    /** Returns base horizontal obstacle spacing in pixels for the zone at the given active run time. */
    fun obstacleSeparationAt(activeTimeSeconds: Double): Double {
        requireValidActiveTime(activeTimeSeconds)

        return when (zoneAt(activeTimeSeconds)) {
            GameZone.ZONE_ONE -> MIN_OBSTACLE_SEPARATION
            GameZone.ZONE_TWO -> ZONE_TWO_MIN_OBSTACLE_SEPARATION
            GameZone.ZONE_THREE -> ZONE_THREE_MIN_OBSTACLE_SEPARATION
            GameZone.BOSS -> ZONE_THREE_MIN_OBSTACLE_SEPARATION
        }
    }

    private fun requireValidActiveTime(activeTimeSeconds: Double) {
        require(activeTimeSeconds.isFinite() && activeTimeSeconds >= 0.0) {
            "Active time must be finite and must not be negative."
        }
    }

    private fun maxObstacleSize(): Double =
        maxOf(
            OBSTACLE_SIZE,
            ZONE_TWO_FLYING_GARGOYLE_SIZE,
            ZONE_TWO_GHOST_KNIGHT_SIZE,
            KNIGHT_SWORD_SIZE,
        )
}
