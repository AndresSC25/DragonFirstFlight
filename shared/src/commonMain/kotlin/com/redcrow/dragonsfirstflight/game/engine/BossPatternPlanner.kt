package com.redcrow.dragonsfirstflight.game.engine

import kotlin.math.abs
import kotlin.random.Random

internal class BossPatternPlanner(
    private val random: Random = Random.Default,
) {
    private var previousPattern: BossAttackPattern? = null

    fun next(bossElapsedSeconds: Double): BossAttackPattern {
        val stage = stageAt(bossElapsedSeconds)
        val candidates =
            when (stage) {
                BossAttackStage.SINGLE ->
                    listOf(260.0, 320.0, 380.0, 440.0, 500.0, 560.0).map { center ->
                        BossAttackPattern(stage, listOf(center))
                    }
                BossAttackStage.DOUBLE ->
                    listOf(listOf(260.0, 400.0), listOf(260.0, 540.0), listOf(400.0, 540.0)).map { centers ->
                        BossAttackPattern(stage, centers)
                    }
                BossAttackStage.CORRIDOR ->
                    listOf(255.0, 327.5, 400.0, 472.5, 545.0).map(::corridor)
            }
        val previousCenter = previousPattern?.safeLaneCenterY
        val selected =
            candidates
                .filter { pattern ->
                    pattern != previousPattern &&
                        (
                            previousCenter == null ||
                                pattern.safeLaneCenterY == null ||
                                abs(pattern.safeLaneCenterY - previousCenter) <= 145.0
                        )
                }.random(random)
        previousPattern = selected
        return selected
    }

    fun reset() {
        previousPattern = null
    }

    private fun corridor(centerY: Double): BossAttackPattern {
        val halfGap = GameEngineConfig.BOSS_SAFE_LANE_HEIGHT / 2.0
        // Reserve the entire sprite plus its full floating range on either side of the safe lane.
        val clearance = GameEngineConfig.BOSS_PROJECTILE_SIZE / 2.0 + GameEngineConfig.BOSS_PROJECTILE_FLOAT_AMPLITUDE
        val centers = mutableListOf<Double>()
        var above = centerY - halfGap - clearance
        while (above >= GameEngineConfig.DRAGON_MIN_Y - GameEngineConfig.BOSS_PROJECTILE_SIZE) {
            centers += above
            above -= GameEngineConfig.BOSS_WALL_SPACING
        }
        var below = centerY + halfGap + clearance
        val bottom = GameEngineConfig.DRAGON_MAX_Y + GameEngineConfig.DRAGON_SIZE
        while (below <= bottom + GameEngineConfig.BOSS_PROJECTILE_SIZE) {
            centers += below
            below += GameEngineConfig.BOSS_WALL_SPACING
        }
        return BossAttackPattern(BossAttackStage.CORRIDOR, centers.sorted(), centerY)
    }

    companion object {
        fun stageAt(bossElapsedSeconds: Double): BossAttackStage =
            when {
                bossElapsedSeconds < GameEngineConfig.BOSS_STAGE_DURATION_SECONDS -> BossAttackStage.SINGLE
                bossElapsedSeconds < GameEngineConfig.BOSS_STAGE_DURATION_SECONDS * 2 -> BossAttackStage.DOUBLE
                else -> BossAttackStage.CORRIDOR
            }
    }
}
