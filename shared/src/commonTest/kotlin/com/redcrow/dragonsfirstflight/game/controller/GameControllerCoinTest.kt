package com.redcrow.dragonsfirstflight.game.controller

import com.redcrow.dragonsfirstflight.game.coins.CoinWallet
import korlibs.korge.service.storage.InmemoryStorage
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GameControllerCoinTest {
    @Test
    fun runCoinsAreBankedExactlyOnce() {
        var collected = false
        for (seed in 1..30) {
            val storage = InmemoryStorage()
            val controller =
                GameController(
                    gameAreaWidth = 1280.0,
                    coinWallet = CoinWallet(storage),
                    random = Random(seed),
                )
            controller.startGame()
            controller.resizeGameArea(0.0, 1280.0)

            var frame = 0
            while (controller.isGameRunning && controller.snapshot().runCoins == 0 && frame < 2000) {
                if (frame % 6 == 0) controller.handleGameAreaInput()
                controller.update(0.05)
                frame += 1
            }
            val runCoins = controller.snapshot().runCoins
            if (runCoins == 0) continue
            collected = true

            controller.pauseGame()
            assertEquals(runCoins.toString(), storage.getOrNull("coins.balance"))
            controller.resumeGame()
            while (controller.isGameRunning && frame < 2000) {
                if (frame % 6 == 0) controller.handleGameAreaInput()
                controller.update(0.05)
                frame += 1
            }
            assertTrue(!controller.isGameRunning, "run did not end for seed $seed")
            val finalRunCoins = controller.snapshot().runCoins
            assertEquals(finalRunCoins.toString(), storage.getOrNull("coins.balance"))
            assertEquals(finalRunCoins, controller.coinBalance)

            controller.returnToMenu()
            controller.startGame()
            assertEquals(finalRunCoins.toString(), storage.getOrNull("coins.balance"))
            assertEquals(finalRunCoins, controller.coinBalance)
            break
        }
        assertTrue(collected, "no coin was collected in any seeded run")
    }
}
