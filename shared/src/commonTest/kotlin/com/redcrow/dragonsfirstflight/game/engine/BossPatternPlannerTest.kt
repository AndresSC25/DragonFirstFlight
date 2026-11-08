package com.redcrow.dragonsfirstflight.game.engine

import kotlin.math.abs
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BossPatternPlannerTest {
    @Test
    fun progressionStartsWithSinglesThenPairsThenCorridorsAndResets() {
        val planner = BossPatternPlanner(Random(7))
        val first = planner.next(4.0)
        assertEquals(BossAttackStage.SINGLE, first.stage)
        assertEquals(1, first.projectileCenterYs.size)
        assertEquals(1, planner.next(19.999).projectileCenterYs.size)
        val pair = planner.next(20.0)
        assertEquals(BossAttackStage.DOUBLE, pair.stage)
        assertEquals(2, pair.projectileCenterYs.size)
        assertEquals(2, planner.next(39.999).projectileCenterYs.size)
        val wall = planner.next(40.0)
        assertEquals(BossAttackStage.CORRIDOR, wall.stage)
        assertTrue(wall.projectileCenterYs.size > 2)
        planner.reset()
        val restarted = planner.next(4.0)
        assertEquals(first.stage, restarted.stage)
        assertEquals(1, restarted.projectileCenterYs.size)
    }

    @Test
    fun eachWallHasExactlyOnePassageAndKeepsAdvertisedLaneClearThroughoutFloating() {
        val planner = BossPatternPlanner(Random(7))
        repeat(30) {
            val pattern = planner.next(44.0)
            val center = pattern.safeLaneCenterY!!
            val safeDragonY = center - GameEngineConfig.DRAGON_SIZE / 2.0
            assertTrue(safeDragonY in GameEngineConfig.DRAGON_MIN_Y..GameEngineConfig.DRAGON_MAX_Y)
            repeat(60) { step ->
                val age = 0.5 + step * 0.05
                val rockBounds =
                    pattern.projectileCenterYs.map { rockCenter ->
                        val projectile =
                            Projectile(
                                id = 0,
                                x = GameEngineConfig.DRAGON_X,
                                y = 395.2,
                                size = GameEngineConfig.BOSS_PROJECTILE_SIZE,
                                velocityX = -GameEngineConfig.BOSS_PROJECTILE_SPEED,
                                velocityY = 0.0,
                                targetY = rockCenter - GameEngineConfig.BOSS_PROJECTILE_SIZE / 2.0,
                                formationDistance = GameEngineConfig.BOSS_FORMATION_DISTANCE,
                                floatAmplitude = GameEngineConfig.BOSS_PROJECTILE_FLOAT_AMPLITUDE,
                            )
                        CollisionBoundsCalculator.calculate(
                            x = projectile.x,
                            y = ProjectileMovementCalculator.yAt(projectile, age),
                            width = projectile.size,
                            height = projectile.size,
                            margins = CollisionProfileCatalog.bossProjectileMargins,
                        )
                    }

                fun isSafe(top: Double): Boolean {
                    val dragon =
                        CollisionBoundsCalculator.calculate(
                            x = GameEngineConfig.DRAGON_X,
                            y = top,
                            width = GameEngineConfig.DRAGON_SIZE,
                            height = GameEngineConfig.DRAGON_SIZE,
                            margins = CollisionProfileCatalog.dragonMargins,
                        )
                    return rockBounds.none { rock -> collisionBoundsOverlap(dragon, rock) }
                }

                assertTrue(isSafe(safeDragonY), "Advertised passage closed at age $age: $pattern")
                var passages = 0
                var wasSafe = false
                for (top in GameEngineConfig.DRAGON_MIN_Y.toInt()..GameEngineConfig.DRAGON_MAX_Y.toInt()) {
                    val safe = isSafe(top.toDouble())
                    if (safe && !wasSafe) passages++
                    wasSafe = safe
                }
                assertEquals(1, passages, "Unexpected alternate passage at age $age: $pattern")
            }
        }
    }

    @Test
    fun corridorMovesByAtMostOneLaneBetweenWarnings() {
        val planner = BossPatternPlanner(Random(7))
        var previous = planner.next(44.0).safeLaneCenterY!!
        repeat(200) {
            val next = planner.next(44.0).safeLaneCenterY!!
            assertTrue(abs(next - previous) <= 145.0)
            previous = next
        }
    }

    @Test
    fun corridorBuildsUpFromFloorWithoutAnyRockFloatingIntoIt() {
        val planner = BossPatternPlanner(Random(7))
        val clearance = GameEngineConfig.BOSS_PROJECTILE_SIZE / 2.0 + GameEngineConfig.BOSS_PROJECTILE_FLOAT_AMPLITUDE
        repeat(100) {
            val pattern = planner.next(44.0)
            val lowerRocks = pattern.projectileCenterYs.filter { it > pattern.safeLaneCenterY!! }
            if (lowerRocks.isNotEmpty()) {
                assertEquals(GameEngineConfig.CATAPULT_GROUND_Y - clearance, lowerRocks.last())
            }
            pattern.projectileCenterYs.forEach { center ->
                assertTrue(center + clearance <= GameEngineConfig.CATAPULT_GROUND_Y)
            }
        }
    }

    @Test
    fun aimedAndRegularVolleysBothAppearInFirstTwoStagesAndKeepCapturedHeight() {
        for (seed in 0..20) {
            val planner = BossPatternPlanner(Random(seed))
            val first = planner.next(4.0, dragonCenterY = 371.25)
            val second = planner.next(8.0, dragonCenterY = 450.0)
            assertTrue(first.isAimed != second.isAimed)
            for (single in listOf(first, second)) {
                val expected = if (single.isAimed) ProjectileTrajectory.FLOATING else ProjectileTrajectory.ARC
                assertEquals(expected, single.trajectory)
            }
            if (first.isAimed) assertEquals(listOf(371.25), first.projectileCenterYs)
            if (second.isAimed) assertEquals(listOf(450.0), second.projectileCenterYs)
            val pairOne = planner.next(24.0, dragonCenterY = 371.25)
            val pairTwo = planner.next(28.0, dragonCenterY = 450.0)
            assertTrue(pairOne.isAimed != pairTwo.isAimed)
            assertEquals(2, pairOne.projectileCenterYs.size)
            assertEquals(2, pairTwo.projectileCenterYs.size)
            assertEquals(ProjectileTrajectory.ARC, pairOne.trajectory)
            assertEquals(ProjectileTrajectory.ARC, pairTwo.trajectory)
            if (pairOne.isAimed) {
                assertTrue(371.25 in pairOne.projectileCenterYs)
                assertTrue(pairOne.projectileCenterYs.last() - pairOne.projectileCenterYs.first() >= 198.5)
            }
            if (pairTwo.isAimed) {
                assertTrue(450.0 in pairTwo.projectileCenterYs)
                assertTrue(pairTwo.projectileCenterYs.last() - pairTwo.projectileCenterYs.first() >= 198.5)
            }
            assertTrue(!planner.next(44.0, dragonCenterY = 400.0).isAimed)
        }
    }

    @Test
    fun seededSequencesAreReproducibleButDifferentSeedsAndRestartsVary() {
        fun sequence(seed: Int): List<BossAttackPattern> {
            val planner = BossPatternPlanner(Random(seed))
            return List(6) { planner.next(4.0) } +
                List(6) { planner.next(24.0) } + List(6) { planner.next(44.0) }
        }

        assertEquals(sequence(7), sequence(7))
        assertTrue((0..20).map(::sequence).toSet().size > 15)
        for (time in listOf(4.0, 24.0, 44.0)) {
            val planner = BossPatternPlanner(Random(42))
            val patterns = List(50) { planner.next(time) }
            assertTrue(patterns.zipWithNext().all { (first, second) -> first != second })
            assertTrue(patterns.toSet().size >= 3)
        }
        val planner = BossPatternPlanner(Random(42))
        val restartedPatterns =
            List(30) {
                planner.reset()
                planner.next(4.0)
            }
        assertTrue(restartedPatterns.toSet().size > 1)
    }
}
