package com.redcrow.dragonsfirstflight.game.engine

object GameEngineConfig {
    const val INITIAL_LIVES: Int = 3
    const val POINTS_TO_WIN: Int = 5

    const val GAME_AREA_HEIGHT: Double = 240.0

    const val DRAGON_X: Double = 24.0
    const val DRAGON_INITIAL_Y: Double = 92.0
    const val DRAGON_SIZE: Double = 56.0
    const val DRAGON_MAX_Y: Double =
        GAME_AREA_HEIGHT - DRAGON_SIZE

    const val GRAVITY: Double = 600.0
    const val FLAP_VELOCITY: Double = -240.0
    const val MAX_FALL_VELOCITY: Double = 360.0

    const val OBSTACLE_Y: Double = 96.0
    const val OBSTACLE_SIZE: Double = 48.0
    const val INITIAL_OBSTACLE_MARGIN: Double = 72.0
    const val OBSTACLE_SPEED: Double = 120.0

    const val MAX_DELTA_SECONDS: Double = 0.05

    init {
        require(INITIAL_LIVES > 0) {
            "INITIAL_LIVES must be greater than zero."
        }

        require(POINTS_TO_WIN > 0) {
            "POINTS_TO_WIN must be greater than zero."
        }

        require(GAME_AREA_HEIGHT > 0.0) {
            "GAME_AREA_HEIGHT must be greater than zero."
        }

        require(DRAGON_SIZE > 0.0) {
            "DRAGON_SIZE must be greater than zero."
        }

        require(OBSTACLE_SIZE > 0.0) {
            "OBSTACLE_SIZE must be greater than zero."
        }

        require(MAX_DELTA_SECONDS > 0.0) {
            "MAX_DELTA_SECONDS must be greater than zero."
        }
    }
}
