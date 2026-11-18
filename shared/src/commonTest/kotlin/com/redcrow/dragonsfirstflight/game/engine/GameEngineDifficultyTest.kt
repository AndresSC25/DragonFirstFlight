package com.redcrow.dragonsfirstflight.game.engine

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class GameEngineDifficultyTest {
 @Test
 fun mediumRemainsTheDefaultDifficulty() {
 val defaultEngine = GameEngine(gameAreaWidth = 1280.0, random = Random(7))
 val mediumEngine =
 GameEngine(
 gameAreaWidth = 1280.0,
 random = Random(7),
 initialDifficulty = GameDifficulty.MEDIUM,
 )

 defaultEngine.update(0.05)
 mediumEngine.update(0.05)

 assertEquals(
 mediumEngine.snapshot().obstacles,
 defaultEngine.snapshot().obstacles,
 )
 }

 @Test
 fun hardMovesObstaclesFartherThanEasy() {
 val easyEngine =
 GameEngine(
 gameAreaWidth = 1280.0,
 random = Random(7),
 initialDifficulty = GameDifficulty.EASY,
 )
 val hardEngine =
 GameEngine(
 gameAreaWidth = 1280.0,
 random = Random(7),
 initialDifficulty = GameDifficulty.HARD,
 )

 easyEngine.update(0.05)
 hardEngine.update(0.05)

 val easyObstacle = assertNotNull(easyEngine.snapshot().firstObstacle)
 val hardObstacle = assertNotNull(hardEngine.snapshot().firstObstacle)

 assertTrue(hardObstacle.x < easyObstacle.x)
 }

 @Test
 fun restartCanChangeDifficultyForTheNextRun() {
 val engine =
 GameEngine(
 gameAreaWidth = 1280.0,
 random = Random(7),
 initialDifficulty = GameDifficulty.EASY,
 )

 engine.restart(GameDifficulty.HARD)
 val initialX = assertNotNull(engine.snapshot().firstObstacle).x
 engine.update(0.05)
 val movedX = assertNotNull(engine.snapshot().firstObstacle).x

 assertEquals(
 GameDifficulty.HARD.obstacleSpeedAt(0.05) * 0.05,
 initialX - movedX,
 absoluteTolerance = 0.000001,
 )
 }

 @Test
 fun restartWithoutArgumentKeepsTheCurrentDifficulty() {
 val engine =
 GameEngine(
 gameAreaWidth = 1280.0,
 random = Random(7),
 initialDifficulty = GameDifficulty.HARD,
 )

 engine.restart()
 val initialX = assertNotNull(engine.snapshot().firstObstacle).x
 engine.update(0.05)
 val movedX = assertNotNull(engine.snapshot().firstObstacle).x

 assertEquals(
 GameDifficulty.HARD.obstacleSpeedAt(0.05) * 0.05,
 initialX - movedX,
 absoluteTolerance = 0.000001,
 )
 }
}
