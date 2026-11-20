package com.redcrow.dragonsfirstflight.game.controller

import com.redcrow.dragonsfirstflight.game.engine.GameDifficulty
import com.redcrow.dragonsfirstflight.game.engine.GameEngineConfig
import com.redcrow.dragonsfirstflight.game.engine.GameStatus
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class GameControllerDifficultyTest {
    @Test
    fun easyIsTheDefaultDifficulty() {
        val controller =
            GameController(
                gameAreaWidth = 1280.0,
                random = Random(7),
            )

        assertEquals(GameDifficulty.EASY, controller.activeDifficulty)

        controller.startGame()

        assertEquals(GameDifficulty.EASY, controller.activeDifficulty)
    }

    @Test
    fun startingAGameAppliesTheSelectedDifficulty() {
        val controller =
            GameController(
                gameAreaWidth = 1280.0,
                random = Random(7),
            )

        controller.startGame(GameDifficulty.HARD)
        val initialX = assertNotNull(controller.snapshot().firstObstacle).x
        controller.update(0.05)
        val movedX = assertNotNull(controller.snapshot().firstObstacle).x

        assertEquals(GameDifficulty.HARD, controller.activeDifficulty)
        assertEquals(
            GameDifficulty.HARD.obstacleSpeedAt(0.05) * 0.05,
            initialX - movedX,
            absoluteTolerance = 0.000001,
        )
    }

    @Test
    fun retryKeepsTheSelectedDifficulty() {
        val controller =
            GameController(
                gameAreaWidth = 240.0,
                random = Random(7),
            )

        controller.startGame(GameDifficulty.HARD)
        val firstRunNumber = controller.runNumber

        var frames = 0
        while (controller.isGameRunning && frames < MAX_FRAMES_UNTIL_LOSS) {
            controller.update(0.05)
            frames++
        }

        assertTrue(frames < MAX_FRAMES_UNTIL_LOSS, "The deterministic run did not reach defeat.")
        assertEquals(GameStatus.LOST, controller.snapshot().status)

        controller.handleGameAreaInput()

        assertEquals(firstRunNumber + 1, controller.runNumber)
        assertEquals(GameDifficulty.HARD, controller.activeDifficulty)
        assertEquals(GameEngineConfig.INITIAL_LIVES, controller.snapshot().lives)
        assertEquals(GameStatus.RUNNING, controller.snapshot().status)

        val initialX = assertNotNull(controller.snapshot().firstObstacle).x
        controller.update(0.05)
        val movedX = assertNotNull(controller.snapshot().firstObstacle).x

        assertEquals(
            GameDifficulty.HARD.obstacleSpeedAt(0.05) * 0.05,
            initialX - movedX,
            absoluteTolerance = 0.000001,
        )
    }

    @Test
    fun aNewGameCanReplaceThePreviousDifficulty() {
        val controller =
            GameController(
                gameAreaWidth = 1280.0,
                random = Random(7),
            )

        controller.startGame(GameDifficulty.HARD)
        controller.returnToMenu()
        controller.startGame(GameDifficulty.EASY)

        assertEquals(GameDifficulty.EASY, controller.activeDifficulty)
    }

    private companion object {
        const val MAX_FRAMES_UNTIL_LOSS = 1000
    }

    @Test
    fun restartAfterFinishingPreservesTheSelectedDifficulty() {
        val controller = GameController(gameAreaWidth = 1280.0)

        controller.startGame(GameDifficulty.HARD)
        assertEquals(GameDifficulty.HARD, controller.activeDifficulty)

        controller.startGame()

        assertEquals(GameDifficulty.HARD, controller.activeDifficulty)
    }
}
