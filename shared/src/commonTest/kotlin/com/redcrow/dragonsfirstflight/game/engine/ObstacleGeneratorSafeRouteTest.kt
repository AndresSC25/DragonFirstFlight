package com.redcrow.dragonsfirstflight.game.engine

import kotlin.math.abs
import kotlin.math.nextDown
import kotlin.math.nextUp
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.fail

class ObstacleGeneratorSafeRouteTest {
    private val dragon =
        CollisionBoundsCalculator.calculate(
            x = GameEngineConfig.DRAGON_X,
            y = 0.0,
            width = GameEngineConfig.DRAGON_SIZE,
            height = GameEngineConfig.DRAGON_SIZE,
            margins = CollisionProfileCatalog.dragonMargins,
        )
    private val flightRange = GameEngineConfig.DRAGON_MIN_Y..GameEngineConfig.DRAGON_MAX_Y
    private val climbPerFrame = abs(GameEngineConfig.FLAP_VELOCITY) * FRAME_DELTA
    private val fallPerFrame = GameEngineConfig.MAX_FALL_VELOCITY * FRAME_DELTA

    @Test
    fun generatedObstaclesLeaveAReachableGap() {
        for (seed in 0 until 48) {
            for (difficulty in GameDifficulty.entries) {
                for (zone in listOf(GameZone.ZONE_ONE, GameZone.ZONE_TWO, GameZone.ZONE_THREE)) {
                    for (viewport in listOf(0.0..1280.0, -200.0..1480.0)) {
                        for (mode in GameMode.entries) {
                            checkRoute(seed, difficulty, zone, viewport, mode)
                        }
                    }
                }
            }
        }
    }

    private fun checkRoute(
        seed: Int,
        difficulty: GameDifficulty,
        zone: GameZone,
        viewport: ClosedFloatingPointRange<Double>,
        mode: GameMode,
    ) {
        val generator =
            ObstacleGenerator(
                areaWidth = viewport.endInclusive,
                random = Random(seed),
                difficulty = difficulty,
                initialZone = zone,
            )
        generator.resize(
            newLeftBoundary = viewport.start,
            newRightBoundary = viewport.endInclusive,
        )
        val zoneStart = zone.ordinal * GameEngineConfig.ZONE_DURATION_SECONDS
        val endless = mode == GameMode.ENDLESS
        val initialY = GameEngineConfig.DRAGON_INITIAL_Y
        var reachable = listOf(initialY..initialY)

        repeat((GameEngineConfig.ZONE_DURATION_SECONDS / FRAME_DELTA).toInt()) { frame ->
            val elapsed = frame * FRAME_DELTA
            val profileTime = if (endless) zoneStart else zoneStart + elapsed
            val activeTime = if (endless) elapsed else profileTime
            val speed = if (endless) difficulty.endless.maximumSpeed else difficulty.obstacleSpeedAt(profileTime)
            val spacingScale = if (endless) speed / difficulty.endlessStartingSpeed(zone) else 1.0
            generator.update(
                delta = FRAME_DELTA,
                activeTimeSeconds = activeTime,
                profileTimeSeconds = profileTime,
                obstacleSpeed = speed,
                obstacleSeparation = difficulty.obstacleSeparationAt(profileTime) * spacingScale,
            )

            val blockers =
                generator.activeObstacles.mapNotNull { obstacle ->
                    val bounds =
                        CollisionBoundsCalculator.calculate(
                            x = obstacle.x,
                            y = obstacle.y,
                            width = obstacle.width,
                            height = obstacle.height,
                            margins = CollisionProfileCatalog.obstacleMargins(obstacle.spritePath),
                        )
                    if (bounds.x < dragon.x + dragon.width && dragon.x < bounds.x + bounds.width) {
                        obstacle to bounds
                    } else {
                        null
                    }
                }
            val safe = safeIntervals(blockers.map { it.second })
            reachable = reachableIntervals(reachable, safe)
            if (reachable.isEmpty()) {
                val reason = if (safe.isEmpty()) "No vertical gap" else "No reachable gap"
                fail(
                    "$reason: seed=$seed difficulty=$difficulty zone=$zone mode=$mode " +
                        "viewport=$viewport time=$activeTime blockers=$blockers",
                )
            }
        }
    }

    private fun safeIntervals(blockers: List<CollisionBounds>): List<ClosedFloatingPointRange<Double>> {
        if (blockers.isEmpty()) return listOf(flightRange)
        val safe = mutableListOf<ClosedFloatingPointRange<Double>>()

        fun fits(y: Double): Boolean {
            val bounds = dragon.copy(y = y + dragon.y)
            return blockers.none { collisionBoundsOverlap(bounds, it) }
        }

        fun addGap(
            start: Double,
            end: Double,
        ) {
            if (start > end) return
            val first = if (fits(start)) start else start.nextUp()
            val last = if (fits(end)) end else end.nextDown()
            if (first <= last && fits(first) && fits(last)) safe += first..last
        }

        var start = flightRange.start
        for (blocker in blockers.sortedBy { it.y }) {
            val above = blocker.y - dragon.height - dragon.y
            addGap(start, minOf(flightRange.endInclusive, above))
            val below = blocker.y + blocker.height - dragon.y
            start = maxOf(start, below)
        }
        addGap(start, flightRange.endInclusive)
        return safe
    }

    private fun reachableIntervals(
        previous: List<ClosedFloatingPointRange<Double>>,
        safe: List<ClosedFloatingPointRange<Double>>,
    ): List<ClosedFloatingPointRange<Double>> {
        val reachable = mutableListOf<ClosedFloatingPointRange<Double>>()
        for (gap in safe) {
            for (interval in previous) {
                val start = maxOf(gap.start, interval.start - climbPerFrame)
                val end = minOf(gap.endInclusive, interval.endInclusive + fallPerFrame)
                if (start > end) continue
                val last = reachable.lastOrNull()
                if (last != null && last.endInclusive >= start) {
                    reachable[reachable.lastIndex] = last.start..maxOf(last.endInclusive, end)
                } else {
                    reachable += start..end
                }
            }
        }
        return reachable
    }

    private companion object {
        const val FRAME_DELTA = 0.05
    }
}
