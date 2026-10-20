package com.redcrow.dragonsfirstflight.game.engine

import com.redcrow.dragonsfirstflight.game.score.ScoreTracker

class GameEngine(
    gameAreaWidth: Double,
    private val onDamageEvent: (DamageEvent) -> Unit = {}
) {
    private var areaWidth: Double =
        validateAreaWidth(gameAreaWidth)

    private var dragonY: Double =
        GameEngineConfig.DRAGON_INITIAL_Y

    private var verticalVelocity: Double = 0.0

    private var obstacleX: Double =
        initialObstacleX(areaWidth)

    private var lives: Int =
        GameEngineConfig.INITIAL_LIVES

    private val scoreTracker = ScoreTracker(
        pointsPerSecond = GameEngineConfig.POINTS_PER_SECOND
    )

    private var collisionRegistered: Boolean = false
    private var impactFeedbackRemainingSeconds: Double = 0.0
    private var invulnerabilityRemainingSeconds: Double = 0.0

    val status: GameStatus
        get() = when {
            lives <= 0 -> GameStatus.LOST

            else -> GameStatus.RUNNING
        }

    val isRunning: Boolean
        get() = status == GameStatus.RUNNING

    fun resizeGameArea(newWidth: Double) {
        areaWidth = validateAreaWidth(newWidth)

        if (obstacleX > areaWidth) {
            resetObstacle()
        }
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

        val delta = elapsedSeconds.coerceAtMost(
            GameEngineConfig.MAX_DELTA_SECONDS
        )

        impactFeedbackRemainingSeconds =
            (impactFeedbackRemainingSeconds - delta).coerceAtLeast(0.0)
        invulnerabilityRemainingSeconds =
            (invulnerabilityRemainingSeconds - delta).coerceAtLeast(0.0)

        if (!isRunning) {
            return
        }

        updateDragon(delta)
        updateObstacle(delta)
        scoreTracker.update(elapsedSeconds)
        val damageEvent = detectDamageEvent()
        if (damageEvent != null) {
            handleDamageEvent(damageEvent)
        }
    }

    fun restart() {
        dragonY = GameEngineConfig.DRAGON_INITIAL_Y
        verticalVelocity = 0.0
        lives = GameEngineConfig.INITIAL_LIVES
        scoreTracker.reset()
        impactFeedbackRemainingSeconds = 0.0
        invulnerabilityRemainingSeconds = 0.0

        resetObstacle()
    }

    fun snapshot(): GameSnapshot {
        val isInvulnerable = invulnerabilityRemainingSeconds > 0.0
        val isInvulnerabilityBlinkDim = isInvulnerable &&
            ((GameEngineConfig.INVULNERABILITY_DURATION_SECONDS -
                invulnerabilityRemainingSeconds) /
                GameEngineConfig.BLINK_HALF_PERIOD_SECONDS).toInt() % 2 == 0

        return GameSnapshot(
            dragonX = GameEngineConfig.DRAGON_X,
            dragonY = dragonY,
            dragonSize = GameEngineConfig.DRAGON_SIZE,
            obstacleX = obstacleX,
            obstacleY = GameEngineConfig.OBSTACLE_Y,
            obstacleSize = GameEngineConfig.OBSTACLE_SIZE,
            verticalVelocity = verticalVelocity,
            lives = lives,
            points = scoreTracker.score.points,
            isImpactFeedbackActive =
                impactFeedbackRemainingSeconds > 0.0,
            isInvulnerable = isInvulnerable,
            isInvulnerabilityBlinkDim = isInvulnerabilityBlinkDim,
            status = status
        )
    }

    private fun updateDragon(delta: Double) {
        val previousVelocity = verticalVelocity

        val newVelocity = (
                previousVelocity +
                        GameEngineConfig.GRAVITY * delta
                ).coerceAtMost(
                GameEngineConfig.MAX_FALL_VELOCITY
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

    private fun updateObstacle(delta: Double) {
        obstacleX -=
            GameEngineConfig.OBSTACLE_SPEED * delta

        if (
            obstacleX <=
            -GameEngineConfig.OBSTACLE_SIZE
        ) {
            resetObstacle()
        }
    }

    private fun detectDamageEvent(): DamageEvent? {
        if (collisionRegistered) {
            return null
        }

        val overlapsObstacle = rectanglesOverlap(
            firstX = GameEngineConfig.DRAGON_X,
            firstY = dragonY,
            firstWidth = GameEngineConfig.DRAGON_SIZE,
            firstHeight = GameEngineConfig.DRAGON_SIZE,
            secondX = obstacleX,
            secondY = GameEngineConfig.OBSTACLE_Y,
            secondWidth = GameEngineConfig.OBSTACLE_SIZE,
            secondHeight = GameEngineConfig.OBSTACLE_SIZE
        )

        if (!overlapsObstacle) {
            return null
        }

        collisionRegistered = true
        return DamageEvent
    }

    internal fun handleDamageEvent(event: DamageEvent) {
        if (!isRunning) {
            return
        }

        onDamageEvent(event)
        if (invulnerabilityRemainingSeconds > 0.0) {
            return
        }

        lives = (lives - 1).coerceAtLeast(0)
        impactFeedbackRemainingSeconds =
            GameEngineConfig.IMPACT_FEEDBACK_DURATION_SECONDS
        if (lives > 0) {
            invulnerabilityRemainingSeconds =
                GameEngineConfig.INVULNERABILITY_DURATION_SECONDS
        }
    }

    private fun resetObstacle() {
        obstacleX = initialObstacleX(areaWidth)
        collisionRegistered = false
    }

    private fun initialObstacleX(width: Double): Double {
        return width -
                GameEngineConfig.INITIAL_OBSTACLE_MARGIN
    }

    private fun validateAreaWidth(width: Double): Double {
        require(width.isFinite()) {
            "The game area width must be finite."
        }

        require(
            width >
                    GameEngineConfig.INITIAL_OBSTACLE_MARGIN
        ) {
            "The game area width must be greater than " +
                    GameEngineConfig.INITIAL_OBSTACLE_MARGIN +
                    "."
        }

        return width
    }
}

object DamageEvent
