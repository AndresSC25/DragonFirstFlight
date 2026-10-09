package com.redcrow.dragonsfirstflight.game.engine

class GameEngine(
    gameAreaWidth: Double
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

    private var points: Int = 0

    private var collisionRegistered: Boolean = false
    private var pointRegistered: Boolean = false

    val status: GameStatus
        get() = when {
            lives <= 0 -> GameStatus.LOST

            points >= GameEngineConfig.POINTS_TO_WIN ->
                GameStatus.WON

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
        if (!isRunning) {
            return
        }

        if (
            !elapsedSeconds.isFinite() ||
            elapsedSeconds <= 0.0
        ) {
            return
        }

        val delta = elapsedSeconds.coerceAtMost(
            GameEngineConfig.MAX_DELTA_SECONDS
        )

        updateDragon(delta)
        updateObstacle(delta)
        evaluateCollisionAndScore()
    }

    fun restart() {
        dragonY = GameEngineConfig.DRAGON_INITIAL_Y
        verticalVelocity = 0.0
        lives = GameEngineConfig.INITIAL_LIVES
        points = 0

        resetObstacle()
    }

    fun snapshot(): GameSnapshot {
        return GameSnapshot(
            dragonX = GameEngineConfig.DRAGON_X,
            dragonY = dragonY,
            dragonSize = GameEngineConfig.DRAGON_SIZE,
            obstacleX = obstacleX,
            obstacleY = GameEngineConfig.OBSTACLE_Y,
            obstacleSize = GameEngineConfig.OBSTACLE_SIZE,
            verticalVelocity = verticalVelocity,
            lives = lives,
            points = points,
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

    private fun evaluateCollisionAndScore() {
        if (
            !collisionRegistered &&
            !pointRegistered &&
            rectanglesOverlap(
                firstX = GameEngineConfig.DRAGON_X,
                firstY = dragonY,
                firstWidth = GameEngineConfig.DRAGON_SIZE,
                firstHeight = GameEngineConfig.DRAGON_SIZE,
                secondX = obstacleX,
                secondY = GameEngineConfig.OBSTACLE_Y,
                secondWidth = GameEngineConfig.OBSTACLE_SIZE,
                secondHeight = GameEngineConfig.OBSTACLE_SIZE
            )
        ) {
            lives = (lives - 1).coerceAtLeast(0)
            collisionRegistered = true
            return
        }

        if (
            !collisionRegistered &&
            !pointRegistered &&
            obstacleX + GameEngineConfig.OBSTACLE_SIZE <=
            GameEngineConfig.DRAGON_X
        ) {
            points += 1
            pointRegistered = true
        }
    }

    private fun resetObstacle() {
        obstacleX = initialObstacleX(areaWidth)
        collisionRegistered = false
        pointRegistered = false
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