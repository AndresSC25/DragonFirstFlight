package com.redcrow.dragonsfirstflight.game.engine

import com.redcrow.dragonsfirstflight.game.score.ScoreTracker
import kotlin.random.Random

class GameEngine(
    gameAreaWidth: Double,
    random: Random = Random.Default,
    private val onDamageEvent: (DamageEvent) -> Unit = {},
) {
    private var dragonY: Double =
        GameEngineConfig.DRAGON_INITIAL_Y

    private var verticalVelocity: Double = 0.0

    private val obstacleGenerator =
        ObstacleGenerator(
            areaWidth = gameAreaWidth,
            random = random,
        )

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
    private var impactPosition: ImpactPosition? = null
    private var invulnerabilityRemainingSeconds: Double = 0.0
    private var zoneTransitionRemainingSeconds: Double = 0.0
    private var catapultEntrySeconds: Double = 0.0
    private val catapultAttack = CatapultAttack()
    private val projectileField = ProjectileField()
    private val bossPatternPlanner = BossPatternPlanner(random)
    private var pendingBossPattern: BossAttackPattern? = null

    var runCoins: Int = 0
        private set

    private val coinField = CoinField(random)
    private var leftBoundary: Double = 0.0
    private var rightBoundary: Double = gameAreaWidth

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
        this.leftBoundary = leftBoundary
        this.rightBoundary = rightBoundary
        obstacleGenerator.resize(
            newLeftBoundary = leftBoundary,
            newRightBoundary = rightBoundary,
            spawnIfEmpty = !isZoneTransitioning && currentZone != GameZone.BOSS,
        )
    }

    fun flap() {
        if (!isRunning || isZoneTransitioning) {
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
            impactPosition = null
        }
        invulnerabilityRemainingSeconds =
            (invulnerabilityRemainingSeconds - delta).coerceAtLeast(0.0)

        if (!isRunning) {
            return
        }

        if (isZoneTransitioning) {
            zoneTransitionRemainingSeconds =
                (
                    zoneTransitionRemainingSeconds - delta
                ).coerceAtLeast(0.0)
            return
        }

        updateDragon(delta)
        evaluateCollisionAndScore()
        if (isRunning) {
            runCoins += coinField.collect(dragonY)
        }
        scoreTracker.update(elapsedSeconds)
        val currentZone =
            GameEngineConfig.zoneAt(
                scoreTracker.score.activeTimeSeconds,
            )
        val previousZone =
            GameEngineConfig.zoneAt(
                (
                    scoreTracker.score.activeTimeSeconds -
                        elapsedSeconds
                ).coerceAtLeast(0.0),
            )
        if (
            currentZone != previousZone &&
            scoreTracker.score.activeTimeSeconds <
            GameEngineConfig.TIME_TO_WIN_SECONDS
        ) {
            zoneTransitionRemainingSeconds =
                GameEngineConfig.ZONE_TRANSITION_DURATION_SECONDS
            obstacleGenerator.beginZoneTransition(currentZone)
            coinField.clear()
            return
        }
        val activeTimeSeconds = scoreTracker.score.activeTimeSeconds
        if (currentZone == GameZone.BOSS) {
            if (!isRunning) {
                projectileField.reset()
                return
            }
            projectileField.update(delta, leftBoundary, rightBoundary)
            val entryDelta = minOf(delta, GameEngineConfig.CATAPULT_ENTRY_DURATION_SECONDS - catapultEntrySeconds)
            catapultEntrySeconds += entryDelta
            val attackDelta = delta - entryDelta
            if (attackDelta > 0.0) {
                catapultAttack.update(
                    delta = attackDelta,
                    onPrepare = {
                        pendingBossPattern =
                            bossPatternPlanner.next(
                                bossElapsedSeconds = GameEngineConfig.zoneElapsedAt(activeTimeSeconds),
                                dragonCenterY = dragonY + GameEngineConfig.DRAGON_SIZE / 2.0,
                            )
                    },
                    onFire = ::launchBossProjectiles,
                )
            }
            return
        }
        obstacleGenerator.update(
            delta = delta,
            activeTimeSeconds = activeTimeSeconds,
        )
        coinField.advance(
            elapsedSeconds = delta,
            distance = GameEngineConfig.obstacleSpeedAt(activeTimeSeconds) * delta,
            leftBoundary = leftBoundary,
        )
        val zone = GameEngineConfig.zoneAt(activeTimeSeconds)
        coinField.spawnForNewObstacles(
            obstacles = obstacleGenerator.activeObstacles,
            rightBoundary = rightBoundary,
            rowIntervalSeconds = GameEngineConfig.coinRowIntervalAt(zone),
            fiveHundredChance = GameEngineConfig.coinFiveHundredChanceAt(zone),
        )
    }

    fun restart() {
        dragonY = GameEngineConfig.DRAGON_INITIAL_Y
        verticalVelocity = 0.0
        lives = GameEngineConfig.INITIAL_LIVES
        scoreTracker.reset()
        impactFeedbackRemainingSeconds = 0.0
        impactObstacleId = null
        impactPosition = null
        invulnerabilityRemainingSeconds = 0.0
        zoneTransitionRemainingSeconds = 0.0
        catapultEntrySeconds = 0.0
        catapultAttack.reset()
        projectileField.reset()
        bossPatternPlanner.reset()
        pendingBossPattern = null

        obstacleGenerator.reset()
        coinField.reset()
        runCoins = 0
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
            zone =
                GameEngineConfig.zoneAt(
                    scoreTracker.score.activeTimeSeconds,
                ),
            isZoneTransitioning = isZoneTransitioning,
            zoneTransitionProgress =
                (
                    1.0 -
                        zoneTransitionRemainingSeconds /
                        GameEngineConfig.ZONE_TRANSITION_DURATION_SECONDS
                ).coerceIn(0.0, 1.0),
            status = status,
            coins = coinField.coins.toList(),
            coinScrollX = coinField.scrollX,
            runCoins = runCoins,
            catapult = catapultSnapshot(),
            projectiles = projectileField.snapshot(),
            impactPosition = impactPosition,
        )
    }

    private val currentZone: GameZone
        get() = GameEngineConfig.zoneAt(scoreTracker.score.activeTimeSeconds)

    private fun catapultSnapshot(): CatapultSnapshot? {
        if (currentZone != GameZone.BOSS) return null

        val progress = catapultEntrySeconds / GameEngineConfig.CATAPULT_ENTRY_DURATION_SECONDS
        val easedProgress = progress * progress * (3.0 - 2.0 * progress)
        val startX = rightBoundary
        val targetX =
            (rightBoundary - GameEngineConfig.CATAPULT_SIZE - GameEngineConfig.CATAPULT_RIGHT_MARGIN)
                .coerceAtLeast(leftBoundary)
        return CatapultSnapshot(
            x = startX + (targetX - startX) * easedProgress,
            y = GameEngineConfig.GAME_AREA_HEIGHT - GameEngineConfig.CATAPULT_SIZE - 24.0,
            size = GameEngineConfig.CATAPULT_SIZE,
            isEntering = progress < 1.0,
            frameIndex = catapultAttack.frameIndex,
            warningFrameIndex = if (isRunning && !isZoneTransitioning) catapultAttack.warningFrameIndex else null,
            attackPattern = pendingBossPattern,
        )
    }

    private fun launchBossProjectiles() {
        val catapult = catapultSnapshot() ?: return
        val pattern = pendingBossPattern ?: return
        val size = GameEngineConfig.BOSS_PROJECTILE_SIZE
        val launchX = catapult.x + catapult.size * GameEngineConfig.CATAPULT_RELEASE_X_FRACTION - size / 2.0
        val launchY = catapult.y + catapult.size * GameEngineConfig.CATAPULT_RELEASE_Y_FRACTION - size / 2.0
        val isArc = pattern.trajectory == ProjectileTrajectory.ARC
        val gravity = if (isArc) GameEngineConfig.BOSS_ARC_GRAVITY else 0.0
        val flightTime = GameEngineConfig.BOSS_ARC_FLIGHT_SECONDS
        val targetX = GameEngineConfig.DRAGON_X + (GameEngineConfig.DRAGON_SIZE - size) / 2.0
        for (centerY in pattern.projectileCenterYs) {
            val targetY = centerY - size / 2.0
            val velocityY =
                if (isArc) (targetY - launchY - 0.5 * gravity * flightTime * flightTime) / flightTime else 0.0
            projectileField.launch(
                x = launchX,
                y = launchY,
                size = size,
                velocityX = if (isArc) (targetX - launchX) / flightTime else -GameEngineConfig.BOSS_PROJECTILE_SPEED,
                velocityY = velocityY,
                targetY = targetY,
                formationDistance = if (isArc) 0.0 else GameEngineConfig.BOSS_FORMATION_DISTANCE,
                floatAmplitude = if (isArc) 0.0 else GameEngineConfig.BOSS_PROJECTILE_FLOAT_AMPLITUDE,
                gravity = gravity,
            )
        }
    }

    private val isZoneTransitioning: Boolean
        get() = zoneTransitionRemainingSeconds > 0.0

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
        val dragonBounds =
            CollisionBoundsCalculator.calculate(
                x = GameEngineConfig.DRAGON_X,
                y = dragonY,
                width = GameEngineConfig.DRAGON_SIZE,
                height = GameEngineConfig.DRAGON_SIZE,
                margins = CollisionProfileCatalog.dragonMargins,
            )

        for (obstacle in obstacleGenerator.snapshot()) {
            if (obstacle.id in collisionRegistered) {
                continue
            }

            val obstacleBounds =
                CollisionBoundsCalculator.calculate(
                    x = obstacle.x,
                    y = obstacle.y,
                    width = obstacle.width,
                    height = obstacle.height,
                    margins =
                        CollisionProfileCatalog.obstacleMargins(
                            obstacle.spritePath,
                        ),
                )

            if (collisionBoundsOverlap(dragonBounds, obstacleBounds)) {
                collisionRegistered += obstacle.id
                pointRegistered += obstacle.id
                impactObstacleId = obstacle.id
                impactPosition = null
                return DamageEvent
            }
        }

        // Consume every touching rock, including during invulnerability, and apply damage once.
        val projectileHit = projectileField.removeColliding(dragonBounds).firstOrNull() ?: return null
        impactObstacleId = null
        impactPosition =
            ImpactPosition(
                x = projectileHit.x + projectileHit.size / 2.0,
                y = projectileHit.y + projectileHit.size / 2.0,
            )
        return DamageEvent
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
