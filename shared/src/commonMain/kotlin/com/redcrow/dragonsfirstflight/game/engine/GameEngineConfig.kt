package com.redcrow.dragonsfirstflight.game.engine

object GameEngineConfig {
    const val INITIAL_LIVES: Int = 3
    const val POINTS_PER_SECOND: Int = 1
    const val ZONE_DURATION_SECONDS: Double = 20.0
    const val ZONE_COUNT: Int = 4
    const val BOSS_START_SECONDS: Double = ZONE_DURATION_SECONDS * (ZONE_COUNT - 1)
    const val BOSS_DURATION_SECONDS: Double = 30.0
    const val TIME_TO_WIN_SECONDS: Double =
        BOSS_START_SECONDS + BOSS_DURATION_SECONDS
    const val ZONE_TRANSITION_DURATION_SECONDS: Double = 5.0

    const val CATAPULT_SIZE: Double = 448.0
    const val CATAPULT_RIGHT_MARGIN: Double = 24.0
    const val CATAPULT_ENTRY_DURATION_SECONDS: Double = 2.0

    const val GAME_AREA_HEIGHT: Double = 720.0

    const val DRAGON_X: Double = 140.0
    const val DRAGON_INITIAL_Y: Double = 320.0
    const val DRAGON_SIZE: Double = 88.0

    const val DRAGON_MIN_Y: Double = 155.0
    const val DRAGON_MAX_Y: Double = 560.0

    const val GRAVITY: Double = 600.0
    const val FLAP_VELOCITY: Double = -240.0
    const val MAX_FALL_VELOCITY: Double = 360.0

    const val OBSTACLE_Y: Double = 328.0
    const val OBSTACLE_SIZE: Double = 72.0
    const val ZONE_TWO_FLYING_GARGOYLE_SIZE: Double = 96.0
    const val ZONE_TWO_GHOST_KNIGHT_SIZE: Double = 96.0
    const val FOREST_MAGE_SIZE: Double = 120.0
    const val KNIGHT_SWORD_SIZE: Double = 120.0
    const val INITIAL_OBSTACLE_MARGIN: Double = 90.0
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

    const val MAX_DELTA_SECONDS: Double = 0.05
    const val IMPACT_FEEDBACK_DURATION_SECONDS: Double = 0.18
    const val INVULNERABILITY_DURATION_SECONDS: Double = 1.5
    const val BLINK_HALF_PERIOD_SECONDS: Double = 0.2

    const val COIN_SIZE: Double = 44.0
    const val COIN_SPACING: Double = 56.0
    const val COIN_PATTERN_LENGTH: Int = 3
    const val COIN_PATTERN_STEP_Y: Double = 24.0
    const val COIN_OBSTACLE_PADDING: Double = 16.0
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
    const val COIN_ROW_INTERVAL_JITTER: Double = 0.25
    const val ZONE_ONE_FIVE_HUNDRED_CHANCE: Double = 0.0
    const val ZONE_TWO_FIVE_HUNDRED_CHANCE: Double = 0.10
    const val ZONE_THREE_FIVE_HUNDRED_CHANCE: Double = 0.20

    init {
        require(INITIAL_LIVES > 0) {
            "INITIAL_LIVES must be greater than zero."
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
            OBSTACLE_BOTTOM_Y + FOREST_MAGE_SIZE <= GAME_AREA_HEIGHT,
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

    fun coinRowIntervalAt(zone: GameZone): Double =
        when (zone) {
            GameZone.ZONE_ONE -> ZONE_ONE_COIN_ROW_INTERVAL_SECONDS
            GameZone.ZONE_TWO -> ZONE_TWO_COIN_ROW_INTERVAL_SECONDS
            GameZone.ZONE_THREE -> ZONE_THREE_COIN_ROW_INTERVAL_SECONDS
            GameZone.BOSS -> Double.POSITIVE_INFINITY
        }

    fun coinFiveHundredChanceAt(zone: GameZone): Double =
        when (zone) {
            GameZone.ZONE_ONE -> ZONE_ONE_FIVE_HUNDRED_CHANCE
            GameZone.ZONE_TWO -> ZONE_TWO_FIVE_HUNDRED_CHANCE
            GameZone.ZONE_THREE -> ZONE_THREE_FIVE_HUNDRED_CHANCE
            GameZone.BOSS -> 0.0
        }

    fun zoneAt(activeTimeSeconds: Double): GameZone {
        requireValidActiveTime(activeTimeSeconds)

        val zoneIndex =
            (activeTimeSeconds / ZONE_DURATION_SECONDS)
                .toInt()
                .coerceAtMost(GameZone.values().lastIndex)

        return GameZone.values()[zoneIndex]
    }

    fun zoneElapsedAt(activeTimeSeconds: Double): Double {
        requireValidActiveTime(activeTimeSeconds)

        return if (zoneAt(activeTimeSeconds) == GameZone.BOSS) {
            activeTimeSeconds - BOSS_START_SECONDS
        } else {
            activeTimeSeconds % ZONE_DURATION_SECONDS
        }
    }

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
            FOREST_MAGE_SIZE,
            KNIGHT_SWORD_SIZE,
        )
}
