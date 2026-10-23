package com.redcrow.dragonsfirstflight.game.engine

object GameEngineConfig {
    const val INITIAL_LIVES: Int = 3
    const val POINTS_PER_SECOND: Int = 1
    const val TIME_TO_WIN_SECONDS: Double = 20.0

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
    const val INITIAL_OBSTACLE_MARGIN: Double = 90.0
    const val MIN_OBSTACLE_SEPARATION: Double = 300.0
    const val OBSTACLE_TOP_Y: Double = 190.0
    const val OBSTACLE_BOTTOM_Y: Double = DRAGON_MAX_Y
    const val OBSTACLE_SPEED: Double = 120.0

    const val MAX_DELTA_SECONDS: Double = 0.05
    const val IMPACT_FEEDBACK_DURATION_SECONDS: Double = 0.18
    const val INVULNERABILITY_DURATION_SECONDS: Double = 1.5
    const val BLINK_HALF_PERIOD_SECONDS: Double = 0.2

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

        require(MIN_OBSTACLE_SEPARATION > OBSTACLE_SIZE) {
            "MIN_OBSTACLE_SEPARATION must be greater than OBSTACLE_SIZE."
        }

        require(OBSTACLE_TOP_Y >= DRAGON_MIN_Y) {
            "OBSTACLE_TOP_Y must be inside the game area."
        }

        require(
            OBSTACLE_BOTTOM_Y + OBSTACLE_SIZE <= DRAGON_MAX_Y + DRAGON_SIZE,
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
    }
}
