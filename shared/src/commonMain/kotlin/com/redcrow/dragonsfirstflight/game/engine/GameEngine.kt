package com.redcrow.dragonsfirstflight.game.engine

import com.redcrow.dragonsfirstflight.game.score.ScoreTracker
import kotlin.math.PI
import kotlin.math.cos
import kotlin.random.Random

class GameEngine(
    gameAreaWidth: Double,
    random: Random = Random.Default,
    initialDifficulty: GameDifficulty = GameDifficulty.EASY,
    initialMode: GameMode = GameMode.NORMAL,
    initialEndlessZone: GameZone = GameZone.ZONE_ONE,
    private val onDamageEvent: (DamageEvent) -> Unit = {},
) {
    private var difficulty: GameDifficulty = initialDifficulty
    var mode: GameMode = initialMode
        private set
    private var endlessZone: GameZone = initialEndlessZone
    private var endlessElapsedSeconds = 0.0
    private var endlessInitialSpeed = difficulty.endlessStartingSpeed(endlessZone)
    private var dragonY: Double =
        GameEngineConfig.DRAGON_INITIAL_Y

    private var verticalVelocity: Double = 0.0

    private val obstacleGenerator =
        ObstacleGenerator(
            areaWidth = gameAreaWidth,
            random = random,
            difficulty = initialDifficulty,
            initialZone = GameEngineConfig.zoneAt(obstacleProfileTimeAt(0.0)),
        )

    private var lives: Int =
        GameEngineConfig.INITIAL_LIVES

    private var damageCount = 0
    private var extraLivesCollected = 0

    private val scoreTracker =
        ScoreTracker(
            pointsPerSecond = GameEngineConfig.POINTS_PER_SECOND,
        )

    private val collisionRegistered = mutableSetOf<Int>()
    private val pointRegistered = mutableSetOf<Int>()
    private var impactFeedbackRemainingSeconds: Double = 0.0
    private var impactObstacleId: Int? = null
    private var impactPosition: ImpactPosition? = null
    private val rockImpacts = mutableListOf<RockImpact>()
    private var invulnerabilityRemainingSeconds: Double = 0.0
    private var zoneTransitionRemainingSeconds: Double = 0.0
    private var catapultEntrySeconds: Double = 0.0
    private val catapultAttack = CatapultAttack()
    private val projectileField = ProjectileField()
    private val bossPatternPlanner = BossPatternPlanner(random)
    private val bossVolley = BossVolley()
    private var pendingBossPattern: BossAttackPattern? = null

    var runCoins: Int = 0
        private set

    private val coinField = CoinField(random)
    private val extraLifeField = ExtraLifeField(random)
    private val firePowerUpField = FirePowerUpField(random)
    private val fireBallField = FireBallField()
    private val shieldPowerUpField = ShieldPowerUpField(random)
    private val ragePowerUpField = RagePowerUpField(random)
    private var firePowerRemainingSeconds = 0.0
    private var shieldRemainingSeconds = 0.0
    private var rageRemainingSeconds = 0.0
    private var fireBallLaunchRemainingSeconds = 0.0
    private var leftBoundary: Double = 0.0
    private var rightBoundary: Double = gameAreaWidth

    val status: GameStatus
        get() =
            when {
                lives <= 0 -> {
                    GameStatus.LOST
                }

                mode == GameMode.NORMAL &&
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
            spawnIfEmpty = !isZoneTransitioning && !isBossEncounter,
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

        for (index in rockImpacts.indices) {
            val impact = rockImpacts[index]
            rockImpacts[index] = impact.copy(ageSeconds = impact.ageSeconds + delta)
        }
        rockImpacts.removeAll { it.ageSeconds >= RockImpact.DURATION_SECONDS }

        impactFeedbackRemainingSeconds =
            (impactFeedbackRemainingSeconds - delta).coerceAtLeast(0.0)
        if (impactFeedbackRemainingSeconds <= 0.0) {
            impactObstacleId = null
            impactPosition = null
        }
        invulnerabilityRemainingSeconds =
            (invulnerabilityRemainingSeconds - delta).coerceAtLeast(0.0)
        firePowerRemainingSeconds =
            (firePowerRemainingSeconds - delta).coerceAtLeast(0.0)
        shieldRemainingSeconds =
            (shieldRemainingSeconds - delta).coerceAtLeast(0.0)
        rageRemainingSeconds =
            (rageRemainingSeconds - delta).coerceAtLeast(0.0)

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
        if (firePowerRemainingSeconds > 0.0) {
            fireBallLaunchRemainingSeconds -= delta
            val shouldLaunch = fireBallLaunchRemainingSeconds <= 0.0
            if (shouldLaunch) {
                fireBallLaunchRemainingSeconds =
                    GameEngineConfig.FIRE_BALL_INTERVAL_SECONDS
            }
            fireBallField.update(
                delta = delta,
                dragonY = dragonY,
                leftBoundary = leftBoundary,
                rightBoundary = rightBoundary,
                shouldLaunch = shouldLaunch,
            )
            val destroyedIds =
                fireBallField.removeColliding(obstacleGenerator.snapshot())
            if (destroyedIds.isNotEmpty()) {
                val destroyedObstacles =
                    obstacleGenerator.snapshot().filter { it.id in destroyedIds }
                destroyedObstacles.forEach { obstacle ->
                    registerPowerUpDestruction(obstacle)
                }
                obstacleGenerator.destroy(destroyedIds)
                collisionRegistered += destroyedIds
                pointRegistered += destroyedIds
            }
        } else {
            fireBallField.update(
                delta = delta,
                dragonY = dragonY,
                leftBoundary = leftBoundary,
                rightBoundary = rightBoundary,
                shouldLaunch = false,
            )
        }
        evaluateCollisionAndScore()
        if (isRunning) {
            runCoins += coinField.collect(dragonY)
            if (
                extraLifeField.collect(
                    dragonY = dragonY,
                    canIncreaseLives = lives < GameEngineConfig.MAX_LIVES,
                )
            ) {
                lives = (lives + 1).coerceAtMost(GameEngineConfig.MAX_LIVES)
                extraLivesCollected++
            }
            if (
                shieldRemainingSeconds <= 0.0 &&
                rageRemainingSeconds <= 0.0 &&
                firePowerUpField.collect(dragonY)
            ) {
                firePowerRemainingSeconds =
                    GameEngineConfig.FIRE_POWER_UP_DURATION_SECONDS
                fireBallLaunchRemainingSeconds = 0.0
            }
            if (firePowerRemainingSeconds <= 0.0 && rageRemainingSeconds <= 0.0) {
                if (shieldPowerUpField.collect(dragonY)) {
                    shieldRemainingSeconds =
                        GameEngineConfig.SHIELD_POWER_UP_DURATION_SECONDS
                }
            }
            if (
                rageRemainingSeconds <= 0.0 &&
                firePowerRemainingSeconds <= 0.0 &&
                shieldRemainingSeconds <= 0.0 &&
                ragePowerUpField.collect(dragonY)
            ) {
                rageRemainingSeconds =
                    GameEngineConfig.RAGE_POWER_UP_DURATION_SECONDS
            }
        }
        evaluateCollisionAndScore()
        if (mode == GameMode.ENDLESS && isRunning) endlessElapsedSeconds += delta
        scoreTracker.update(
            elapsedSeconds = elapsedSeconds,
            pointsMultiplier =
                1.0 +
                    (
                        if (rageRemainingSeconds > 0.0) delta / elapsedSeconds else 0.0
                    ) *
                    (GameEngineConfig.RAGE_SPEED_MULTIPLIER - 1.0),
        )
        val currentZone =
            zoneAt(
                scoreTracker.score.activeTimeSeconds,
            )
        val previousZone =
            zoneAt(
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
            rageRemainingSeconds = 0.0
            zoneTransitionRemainingSeconds =
                GameEngineConfig.ZONE_TRANSITION_DURATION_SECONDS
            obstacleGenerator.beginZoneTransition(currentZone)
            coinField.clear()
            extraLifeField.clear()
            ragePowerUpField.clear()
            return
        }
        val activeTimeSeconds = scoreTracker.score.activeTimeSeconds
        val profileTimeSeconds = obstacleProfileTimeAt(activeTimeSeconds)
        val obstacleSpeed = currentObstacleSpeed
        val obstacleSeparation =
            difficulty.obstacleSeparationAt(profileTimeSeconds) *
                if (mode == GameMode.ENDLESS) obstacleSpeed / endlessInitialSpeed else 1.0
        if (isBossEncounter) {
            if (!isRunning) {
                projectileField.reset()
                bossVolley.reset()
                return
            }
            projectileField.update(delta, leftBoundary, rightBoundary).forEach { projectile ->
                val size = projectile.size * 74.0 / 128.0
                rockImpacts +=
                    RockImpact(
                        x = projectile.x + projectile.size / 2.0,
                        y = GameEngineConfig.CATAPULT_GROUND_Y - size / 2.0,
                        size = size,
                        isGroundImpact = true,
                    )
            }
            bossVolley.update(delta).forEach(::launchBossProjectile)
            val entryDelta = minOf(delta, GameEngineConfig.CATAPULT_ENTRY_DURATION_SECONDS - catapultEntrySeconds)
            catapultEntrySeconds += entryDelta
            val attackDelta = delta - entryDelta
            if (attackDelta > 0.0) {
                catapultAttack.update(
                    delta = attackDelta,
                    isFurious = isBossFurious,
                    onPrepare = {
                        pendingBossPattern =
                            bossPatternPlanner.next(
                                bossElapsedSeconds = GameEngineConfig.zoneElapsedAt(activeTimeSeconds),
                                dragonCenterY = dragonY + GameEngineConfig.DRAGON_SIZE / 2.0,
                            )
                    },
                    onFire = ::startBossVolley,
                )
            }
            return
        }
        obstacleGenerator.update(
            delta = delta,
            activeTimeSeconds = activeTimeSeconds,
            profileTimeSeconds = profileTimeSeconds,
            obstacleSpeed = obstacleSpeed,
            obstacleSeparation = obstacleSeparation,
        )
        coinField.advance(
            elapsedSeconds = delta,
            distance = obstacleSpeed * delta,
            leftBoundary = leftBoundary,
        )
        extraLifeField.advance(
            elapsedSeconds = delta,
            distance = obstacleSpeed * delta,
            leftBoundary = leftBoundary,
            isEndless = mode == GameMode.ENDLESS,
        )
        firePowerUpField.advance(
            elapsedSeconds = delta,
            distance = obstacleSpeed * delta,
            leftBoundary = leftBoundary,
            isEndless = mode == GameMode.ENDLESS,
        )
        shieldPowerUpField.advance(
            elapsedSeconds = delta,
            distance = obstacleSpeed * delta,
            leftBoundary = leftBoundary,
            isEndless = mode == GameMode.ENDLESS,
        )
        ragePowerUpField.advance(
            elapsedSeconds = delta,
            distance = obstacleSpeed * delta,
            leftBoundary = leftBoundary,
            isEndless = mode == GameMode.ENDLESS,
        )
        val zone = GameEngineConfig.zoneAt(profileTimeSeconds)
        coinField.spawnForNewObstacles(
            obstacles = obstacleGenerator.activeObstacles,
            rightBoundary = rightBoundary,
            rowIntervalSeconds = GameEngineConfig.coinRowIntervalAt(zone),
            fiveHundredChance = GameEngineConfig.coinFiveHundredChanceAt(zone),
        )
        extraLifeField.spawnForNewObstacles(
            obstacles = obstacleGenerator.activeObstacles,
            rightBoundary = rightBoundary,
            zone = zone,
        )
        firePowerUpField.spawnForNewObstacles(
            obstacles = obstacleGenerator.activeObstacles,
            rightBoundary = rightBoundary,
            zone = zone,
            isEndless = mode == GameMode.ENDLESS,
            shieldPowerUpActive = shieldRemainingSeconds > 0.0,
            shieldPowerUpsPresent = shieldPowerUpField.powerUps.isNotEmpty(),
            ragePowerUpActive = rageRemainingSeconds > 0.0,
            ragePowerUpsPresent = ragePowerUpField.powerUps.isNotEmpty(),
        )
        shieldPowerUpField.spawnForNewObstacles(
            obstacles = obstacleGenerator.activeObstacles,
            rightBoundary = rightBoundary,
            zone = zone,
            isEndless = mode == GameMode.ENDLESS,
            firePowerUpActive = firePowerRemainingSeconds > 0.0,
            firePowerUpsPresent = firePowerUpField.powerUps.isNotEmpty(),
            ragePowerUpActive = rageRemainingSeconds > 0.0,
            ragePowerUpsPresent = ragePowerUpField.powerUps.isNotEmpty(),
        )
        ragePowerUpField.spawnForNewObstacles(
            obstacles = obstacleGenerator.activeObstacles,
            rightBoundary = rightBoundary,
            zone = zone,
            isEndless = mode == GameMode.ENDLESS,
            rageActive = rageRemainingSeconds > 0.0,
            firePowerUpActive = firePowerRemainingSeconds > 0.0,
            shieldPowerUpActive = shieldRemainingSeconds > 0.0,
            otherPowerUpsPresent =
                firePowerUpField.powerUps.isNotEmpty() ||
                    shieldPowerUpField.powerUps.isNotEmpty(),
        )
        // Endless sessions must not retain collision history for enemies that have left the field.
        if (mode == GameMode.ENDLESS) {
            val activeIds = obstacleGenerator.activeObstacles.mapTo(mutableSetOf()) { it.id }
            collisionRegistered.retainAll(activeIds)
            pointRegistered.retainAll(activeIds)
        }
    }

    fun continueEndless() {
        if (mode != GameMode.NORMAL || status != GameStatus.WON) return
        mode = GameMode.ENDLESS
        endlessZone = GameZone.BOSS
        endlessElapsedSeconds = 0.0
        endlessInitialSpeed = difficulty.endlessStartingSpeed(endlessZone, afterVictory = true)
        dragonY = GameEngineConfig.DRAGON_INITIAL_Y
        verticalVelocity = 0.0
        impactFeedbackRemainingSeconds = 0.0
        impactObstacleId = null
        impactPosition = null
        rockImpacts.clear()
        zoneTransitionRemainingSeconds = GameEngineConfig.ZONE_TRANSITION_DURATION_SECONDS
        resetBossEncounter()
        // Keep obstacle ids and reward limits: this continues the same run.
        obstacleGenerator.beginZoneTransition(GameZone.ZONE_THREE)
        coinField.clear()
        extraLifeField.clear()
        firePowerUpField.clear()
        shieldPowerUpField.clear()
        ragePowerUpField.clear()
        fireBallField.reset()
        firePowerRemainingSeconds = 0.0
        shieldRemainingSeconds = 0.0
        rageRemainingSeconds = 0.0
        collisionRegistered.clear()
        pointRegistered.clear()
    }

    fun restart(
        difficulty: GameDifficulty = this.difficulty,
        mode: GameMode = this.mode,
        endlessZone: GameZone = this.endlessZone,
    ) {
        this.difficulty = difficulty
        this.mode = mode
        this.endlessZone = endlessZone
        endlessElapsedSeconds = 0.0
        endlessInitialSpeed = difficulty.endlessStartingSpeed(endlessZone)
        dragonY = GameEngineConfig.DRAGON_INITIAL_Y
        verticalVelocity = 0.0
        lives = GameEngineConfig.INITIAL_LIVES
        damageCount = 0
        extraLivesCollected = 0
        scoreTracker.reset()
        rockImpacts.clear()
        impactFeedbackRemainingSeconds = 0.0
        impactObstacleId = null
        impactPosition = null
        invulnerabilityRemainingSeconds = 0.0
        zoneTransitionRemainingSeconds = 0.0
        resetBossEncounter()

        obstacleGenerator.reset(difficulty, GameEngineConfig.zoneAt(obstacleProfileTimeAt(0.0)))
        coinField.reset()
        extraLifeField.reset()
        firePowerUpField.reset()
        shieldPowerUpField.reset()
        ragePowerUpField.reset()
        firePowerRemainingSeconds = 0.0
        shieldRemainingSeconds = 0.0
        rageRemainingSeconds = 0.0
        fireBallLaunchRemainingSeconds = 0.0
        fireBallField.reset()
        runCoins = 0
        collisionRegistered.clear()
        pointRegistered.clear()
    }

    private fun resetBossEncounter() {
        catapultEntrySeconds = 0.0
        catapultAttack.reset()
        projectileField.reset()
        bossPatternPlanner.reset()
        bossVolley.reset()
        pendingBossPattern = null
    }

    fun snapshot(): GameSnapshot {
        val isInvulnerable =
            invulnerabilityRemainingSeconds > 0.0 ||
                rageRemainingSeconds > 0.0
        val isInvulnerabilityBlinkDim =
            invulnerabilityRemainingSeconds > 0.0 &&
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
                zoneAt(
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
            extraLives = extraLifeField.extraLives.toList(),
            extraLifeScrollX = extraLifeField.currentScrollX,
            firePowerUps = firePowerUpField.powerUps,
            firePowerUpScrollX = firePowerUpField.currentScrollX,
            isFirePowerActive = firePowerRemainingSeconds > 0.0,
            firePowerRemainingSeconds = firePowerRemainingSeconds,
            fireBalls = fireBallField.fireBalls,
            shieldPowerUps = shieldPowerUpField.powerUps,
            shieldPowerUpScrollX = shieldPowerUpField.currentScrollX,
            isShieldActive = shieldRemainingSeconds > 0.0,
            shieldRemainingSeconds = shieldRemainingSeconds,
            catapult = catapultSnapshot(),
            projectiles = projectileField.snapshot(),
            impactPosition = impactPosition,
            rockImpacts = rockImpacts.toList(),
            mode = mode,
            endlessElapsedSeconds = endlessElapsedSeconds,
            obstacleSpeed = currentObstacleSpeed,
            ragePowerUps = ragePowerUpField.powerUps,
            ragePowerUpScrollX = ragePowerUpField.currentScrollX,
            isRageActive = rageRemainingSeconds > 0.0,
            rageRemainingSeconds = rageRemainingSeconds,
            damageCount = damageCount,
            extraLivesCollected = extraLivesCollected,
        )
    }

    private val currentZone: GameZone
        get() = zoneAt(scoreTracker.score.activeTimeSeconds)

    private val currentObstacleSpeed: Double
        get() =
            if (mode == GameMode.ENDLESS) {
                difficulty.endless.speedAt(endlessElapsedSeconds, endlessInitialSpeed) *
                    if (rageRemainingSeconds > 0.0) GameEngineConfig.RAGE_SPEED_MULTIPLIER else 1.0
            } else {
                difficulty.obstacleSpeedAt(scoreTracker.score.activeTimeSeconds) *
                    if (rageRemainingSeconds > 0.0) GameEngineConfig.RAGE_SPEED_MULTIPLIER else 1.0
            }

    private fun zoneAt(activeTimeSeconds: Double): GameZone =
        if (mode == GameMode.ENDLESS) endlessZone else GameEngineConfig.zoneAt(activeTimeSeconds)

    private val isBossEncounter: Boolean
        get() = mode == GameMode.NORMAL && currentZone == GameZone.BOSS

    private fun obstacleProfileTimeAt(activeTimeSeconds: Double): Double {
        if (mode == GameMode.NORMAL) return activeTimeSeconds
        // The battlefield is a visual choice in Endless; use regular forest enemies there.
        val enemyZone = if (endlessZone == GameZone.BOSS) GameZone.ZONE_THREE else endlessZone
        return enemyZone.ordinal * GameEngineConfig.ZONE_DURATION_SECONDS
    }

    private val isBossFurious: Boolean
        get() =
            isBossEncounter &&
                isRunning &&
                bossElapsedSeconds >= GameEngineConfig.BOSS_FURY_START_SECONDS

    private val bossElapsedSeconds: Double
        get() = GameEngineConfig.zoneElapsedAt(scoreTracker.score.activeTimeSeconds)

    private fun catapultSnapshot(): CatapultSnapshot? {
        if (!isBossEncounter) return null

        val progress = catapultEntrySeconds / GameEngineConfig.CATAPULT_ENTRY_DURATION_SECONDS
        val easedProgress = progress * progress * (3.0 - 2.0 * progress)
        val startX = rightBoundary
        val targetX =
            (rightBoundary - GameEngineConfig.CATAPULT_SIZE - GameEngineConfig.CATAPULT_RIGHT_MARGIN)
                .coerceAtLeast(leftBoundary)
        val furyElapsed = bossElapsedSeconds - GameEngineConfig.BOSS_FURY_START_SECONDS
        val groundOffset = GameEngineConfig.CATAPULT_SIZE * GameEngineConfig.CATAPULT_GROUND_ANCHOR_RATIO
        return CatapultSnapshot(
            x = startX + (targetX - startX) * easedProgress,
            y = GameEngineConfig.CATAPULT_GROUND_Y - groundOffset,
            size = GameEngineConfig.CATAPULT_SIZE,
            isEntering = progress < 1.0,
            frameIndex = bossVolley.secondaryFrameIndex ?: catapultAttack.frameIndex,
            warningFrameIndex = if (isRunning && !isZoneTransitioning) catapultAttack.warningFrameIndex else null,
            attackPattern = pendingBossPattern,
            isFurious = isBossFurious,
            furyPulse = if (isBossFurious) (1.0 - cos(furyElapsed * 0.8 * 2.0 * PI)) / 2.0 else 0.0,
        )
    }

    private fun startBossVolley() {
        val pattern = pendingBossPattern ?: return
        bossVolley.start(pattern).forEach(::launchBossProjectile)
    }

    private fun launchBossProjectile(launch: BossProjectileLaunch) {
        val catapult = catapultSnapshot() ?: return
        val pattern = launch.pattern
        val size = GameEngineConfig.BOSS_PROJECTILE_SIZE
        val launchX = catapult.x + catapult.size * GameEngineConfig.CATAPULT_RELEASE_X_FRACTION - size / 2.0
        val launchY = catapult.y + catapult.size * GameEngineConfig.CATAPULT_RELEASE_Y_FRACTION - size / 2.0
        val isArc = pattern.trajectory == ProjectileTrajectory.ARC
        val gravity = if (isArc) GameEngineConfig.BOSS_ARC_GRAVITY else 0.0
        val flightTime = GameEngineConfig.BOSS_ARC_FLIGHT_SECONDS
        val targetX = GameEngineConfig.DRAGON_X + (GameEngineConfig.DRAGON_SIZE - size) / 2.0
        val targetY = launch.centerY - size / 2.0
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
                if (
                    shieldRemainingSeconds > 0.0 ||
                    rageRemainingSeconds > 0.0
                ) {
                    registerPowerUpDestruction(obstacle)
                    obstacleGenerator.destroy(setOf(obstacle.id))
                    continue
                }
                impactObstacleId = obstacle.id
                impactPosition = null
                return DamageEvent
            }
        }

        // Consume every touching rock, including during invulnerability, and apply damage once.
        val projectileHits = projectileField.removeColliding(dragonBounds)
        projectileHits.forEach { projectile ->
            rockImpacts +=
                RockImpact(
                    x = projectile.x + projectile.size / 2.0,
                    y = projectile.y + projectile.size / 2.0,
                    size = projectile.size * 74.0 / 128.0,
                )
        }
        val projectileHit = projectileHits.firstOrNull() ?: return null
        impactObstacleId = null
        impactPosition =
            ImpactPosition(
                x = projectileHit.x + projectileHit.size / 2.0,
                y = projectileHit.y + projectileHit.size / 2.0,
            )
        return DamageEvent
    }

    private fun registerPowerUpDestruction(obstacle: Obstacle) {
        impactObstacleId = null
        impactPosition =
            ImpactPosition(
                x = obstacle.x + obstacle.width / 2.0,
                y = obstacle.y + obstacle.height / 2.0,
            )
        impactFeedbackRemainingSeconds =
            GameEngineConfig.IMPACT_FEEDBACK_DURATION_SECONDS
    }

    internal fun handleDamageEvent(event: DamageEvent) {
        if (!isRunning) {
            return
        }
        if (
            shieldRemainingSeconds > 0.0 ||
            rageRemainingSeconds > 0.0
        ) {
            return
        }

        onDamageEvent(event)
        impactFeedbackRemainingSeconds =
            GameEngineConfig.IMPACT_FEEDBACK_DURATION_SECONDS
        if (invulnerabilityRemainingSeconds > 0.0) {
            return
        }

        lives = (lives - 1).coerceAtLeast(0)
        damageCount++
        if (lives > 0) {
            invulnerabilityRemainingSeconds =
                GameEngineConfig.INVULNERABILITY_DURATION_SECONDS
        }
    }
}

object DamageEvent
