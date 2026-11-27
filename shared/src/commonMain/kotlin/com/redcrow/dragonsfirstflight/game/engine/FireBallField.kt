package com.redcrow.dragonsfirstflight.game.engine

/** Tracks the dragon's fireballs independently of the catapult's stones. */
internal class FireBallField {
    private val active = mutableListOf<Projectile>()
    private var nextId = 0

    val fireBalls: List<Projectile>
        get() = active

    fun reset() {
        active.clear()
        nextId = 0
    }

    /** Launches one fireball if requested, advances all fireballs by [delta] seconds, and culls off-screen shots. */
    fun update(
        delta: Double,
        dragonY: Double,
        leftBoundary: Double,
        rightBoundary: Double,
        shouldLaunch: Boolean,
    ) {
        if (shouldLaunch) {
            active +=
                Projectile(
                    id = nextId++,
                    x = GameEngineConfig.DRAGON_X + GameEngineConfig.DRAGON_SIZE - 8.0,
                    y = dragonY + (GameEngineConfig.DRAGON_SIZE - GameEngineConfig.FIRE_BALL_SIZE) / 2.0,
                    size = GameEngineConfig.FIRE_BALL_SIZE,
                    velocityX = GameEngineConfig.FIRE_BALL_SPEED,
                    velocityY = 0.0,
                )
        }

        for (index in active.indices) {
            val ball = active[index]
            active[index] =
                ball.copy(
                    x = ball.x + ball.velocityX * delta,
                    ageSeconds = ball.ageSeconds + delta,
                )
        }
        active.removeAll {
            it.x > rightBoundary ||
                it.x + it.size < leftBoundary ||
                it.y + it.size < 0.0 ||
                it.y > GameEngineConfig.GAME_AREA_HEIGHT
        }
    }

    /** Consumes each overlapping fireball and returns the IDs of the first overlapping obstacle for each. */
    fun removeColliding(obstacles: List<Obstacle>): Set<Int> {
        val destroyedIds = mutableSetOf<Int>()
        for (ballIndex in active.indices.reversed()) {
            val ball = active[ballIndex]
            for (obstacle in obstacles) {
                if (
                    rectanglesOverlap(
                        firstX = ball.x,
                        firstY = ball.y,
                        firstWidth = ball.size,
                        firstHeight = ball.size,
                        secondX = obstacle.x,
                        secondY = obstacle.y,
                        secondWidth = obstacle.width,
                        secondHeight = obstacle.height,
                    )
                ) {
                    destroyedIds += obstacle.id
                    active.removeAt(ballIndex)
                    break
                }
            }
        }
        return destroyedIds
    }
}
