package com.redcrow.dragonsfirstflight.game.engine

import com.redcrow.dragonsfirstflight.game.score.ScoreTracker

class GameEngine(
    gameAreaWidth: Double,
    private val onDamageEvent: (DamageEvent) -> Unit = {},
) {
    private var dragonY: Double =
        GameEngineConfig.DRAGON_INITIAL_Y

    private var verticalVelocity: Double = 0.0

    private val obstacleGenerator =
        ObstacleGenerator(gameAreaWidth)

    private var lives: Int =
        GameEngineConfig.INITIAL_LIVES

    private val scoreTracker =
        ScoreTracker(
            pointsPerSecond = GameEngineConfig.POINTS_PER_SECOND,
        )

    private val collisionRegistered = mutableSetOf<Int>()
    private val pointRegistered = mutableSetOf<Int>()
    private var impactFeedbackRemainingSeconds: Double = 0.0
    private var impactObstacleId: Int? = null
    private var invulnerabilityRemainingSeconds: Double = 0.0

    val status: GameStatus
        get() =
            when {
                lives <= 0 -> {
                    GameStatus.LOST
                }

                scoreTracker.score.activeTimeSeconds >=
                    GameEngineConfig.TIME_TO_WIN_SECONDS -> {
                    GameStatus.WON
                }

                else -> {
                    GameStatus.RUNNING
                }
            }

    val isRunning: Boolean
        get() = status == GameStatus.RUNNING

    fun resizeGameArea(
        leftBoundary: Double,
        rightBoundary: Double,
    ) {
        obstacleGenerator.resize(
            newLeftBoundary = leftBoundary,
            newRightBoundary = rightBoundary,
        )
    }

    fun flap() {
        if (!isRunning) {
            return
        }

        verticalVelocity =
            GameEngineConfig.FLAP_VELOCITY
    }

    fun update(elapsedSeconds: Double) {
        if (
            !elapsedSeconds.isFinite() ||
            elapsedSeconds <= 0.0
        ) {
            return
        }

        val delta =
            elapsedSeconds.coerceAtMost(
                GameEngineConfig.MAX_DELTA_SECONDS,
            )

        impactFeedbackRemainingSeconds =
            (impactFeedbackRemainingSeconds - delta).coerceAtLeast(0.0)
        if (impactFeedbackRemainingSeconds <= 0.0) {
            impactObstacleId = null
        }
        invulnerabilityRemainingSeconds =
            (invulnerabilityRemainingSeconds - delta).coerceAtLeast(0.0)

        if (!isRunning) {
            return
        }

        updateDragon(delta)
        evaluateCollisionAndScore()
        obstacleGenerator.update(delta)
        scoreTracker.update(elapsedSeconds)
    }

    fun restart() {
        dragonY = GameEngineConfig.DRAGON_INITIAL_Y
        verticalVelocity = 0.0
        lives = GameEngineConfig.INITIAL_LIVES
        scoreTracker.reset()
        impactFeedbackRemainingSeconds = 0.0
        impactObstacleId = null
        invulnerabilityRemainingSeconds = 0.0

        obstacleGenerator.reset()
        collisionRegistered.clear()
        pointRegistered.clear()
    }

    fun snapshot(): GameSnapshot {
        val isInvulnerable = invulnerabilityRemainingSeconds > 0.0
        val isInvulnerabilityBlinkDim =
            isInvulnerable &&
                (
                    (
                        GameEngineConfig.INVULNERABILITY_DURATION_SECONDS -
                            invulnerabilityRemainingSeconds
                    ) /
                        GameEngineConfig.BLINK_HALF_PERIOD_SECONDS
                ).toInt() % 2 == 0

        return GameSnapshot(
            dragonX = GameEngineConfig.DRAGON_X,
            dragonY = dragonY,
            dragonSize = GameEngineConfig.DRAGON_SIZE,
            obstacles = obstacleGenerator.snapshot(),
            destroyedObstacleIds = collisionRegistered.toSet(),
            verticalVelocity = verticalVelocity,
            lives = lives,
            points = scoreTracker.score.points,
            isImpactFeedbackActive =
                impactFeedbackRemainingSeconds > 0.0,
            impactObstacleId = impactObstacleId,
            isInvulnerable = isInvulnerable,
            isInvulnerabilityBlinkDim = isInvulnerabilityBlinkDim,
            status = status,
        )
    }

    private fun updateDragon(delta: Double) {
        val previousVelocity = verticalVelocity

        val newVelocity =
            (
                previousVelocity +
                    GameEngineConfig.GRAVITY * delta
            ).coerceAtMost(
                GameEngineConfig.MAX_FALL_VELOCITY,
            )

        val displacement =
            (
                (previousVelocity + newVelocity) / 2.0
            ) * delta

        val newY = dragonY + displacement

        when {
            newY <= GameEngineConfig.DRAGON_MIN_Y -> {
                dragonY = GameEngineConfig.DRAGON_MIN_Y
                verticalVelocity = 0.0
            }

            newY >= GameEngineConfig.DRAGON_MAX_Y -> {
                dragonY = GameEngineConfig.DRAGON_MAX_Y
                verticalVelocity = 0.0
            }

            else -> {
                dragonY = newY
                verticalVelocity = newVelocity
            }
        }
    }

    private fun evaluateCollisionAndScore() {
        val damageEvent = detectDamageEvent()
        if (damageEvent != null) {
            handleDamageEvent(damageEvent)
            return
        }

        for (obstacle in obstacleGenerator.snapshot()) {
            if (
                obstacle.id !in pointRegistered &&
                obstacle.x + obstacle.width <= GameEngineConfig.DRAGON_X
            ) {
                pointRegistered += obstacle.id
            }
        }
    }

    private fun detectDamageEvent(): DamageEvent? {
        for (obstacle in obstacleGenerator.snapshot()) {
            if (obstacle.id in collisionRegistered) {
                continue
            }

            val overlapsObstacle =
                rectanglesOverlap(
                    firstX = GameEngineConfig.DRAGON_X,
                    firstY = dragonY,
                    firstWidth = GameEngineConfig.DRAGON_SIZE,
                    firstHeight = GameEngineConfig.DRAGON_SIZE,
                    secondX = obstacle.x,
                    secondY = obstacle.y,
                    secondWidth = obstacle.width,
                    secondHeight = obstacle.height,
                )

            if (overlapsObstacle) {
                collisionRegistered += obstacle.id
                pointRegistered += obstacle.id
                impactObstacleId = obstacle.id
                return DamageEvent
            }
        }

        return null
    }

    internal fun handleDamageEvent(event: DamageEvent) {
        if (!isRunning) {
            return
        }

        onDamageEvent(event)
        impactFeedbackRemainingSeconds =
            GameEngineConfig.IMPACT_FEEDBACK_DURATION_SECONDS
        if (invulnerabilityRemainingSeconds > 0.0) {
            return
        }

        lives = (lives - 1).coerceAtLeast(0)
        if (lives > 0) {
            invulnerabilityRemainingSeconds =
                GameEngineConfig.INVULNERABILITY_DURATION_SECONDS
        }
    }
}

object DamageEvent
