package com.redcrow.dragonsfirstflight.game.audio

import com.redcrow.dragonsfirstflight.game.engine.CatapultSnapshot
import com.redcrow.dragonsfirstflight.game.engine.GameEngine
import com.redcrow.dragonsfirstflight.game.engine.GameMode
import com.redcrow.dragonsfirstflight.game.engine.GameSnapshot
import com.redcrow.dragonsfirstflight.game.engine.GameStatus
import com.redcrow.dragonsfirstflight.game.engine.GameZone
import com.redcrow.dragonsfirstflight.game.engine.Projectile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GameAudioDirectorTest {
    private val output = RecordingOutput()
    private val director = GameAudioDirector(output)
    private val initial = GameEngine(1280.0).snapshot()

    @Test
    fun menuIsSilentAndEachRunStartsOnlyOnce() {
        update(initial, visible = false)
        assertTrue(output.cues.isEmpty())
        assertFalse(output.musicActive)
        repeat(100) { update(initial) }
        assertEquals(listOf(GameSound.START to 0.0), output.cues)
        update(initial, run = 2)
        assertEquals(2, output.cues.count { it.first == GameSound.START })
    }

    @Test
    fun collectionsAndActualDamageAreNotRepeatedEveryFrame() {
        update(initial)
        output.cues.clear()
        val collected = initial.copy(runCoins = 5, damageCount = 1, extraLivesCollected = 1)
        repeat(50) { update(collected) }
        // A damage and a life pickup can happen together without changing the life count.
        assertEquals(listOf(GameSound.COIN, GameSound.EXTRA_LIFE, GameSound.DAMAGE), output.cues.map { it.first })
    }

    @Test
    fun rushWarningSoundsOncePerNewEnemy() {
        update(initial)
        val hazard = initial.obstacles.first().copy(rushWarningRemainingSeconds = 0.8)
        val warned = initial.copy(obstacles = listOf(hazard))
        repeat(20) { update(warned) }
        assertEquals(1, output.cues.count { it.first == GameSound.RUSH })
        update(warned.copy(obstacles = listOf(hazard.copy(id = hazard.id + 1))))
        assertEquals(2, output.cues.count { it.first == GameSound.RUSH })
    }

    @Test
    fun stageTransitionAndEndlessContinuationUseDifferentCues() {
        update(initial)
        val transition = initial.copy(zone = GameZone.ZONE_TWO, isZoneTransitioning = true)
        repeat(30) { update(transition) }
        assertEquals(1, output.cues.count { it.first == GameSound.TRANSITION })
        update(initial.copy(status = GameStatus.WON))
        output.cues.clear()
        val endless = initial.copy(mode = GameMode.ENDLESS, zone = GameZone.BOSS, isZoneTransitioning = true)
        repeat(30) { update(endless) }
        assertEquals(listOf(GameSound.ENDLESS to 0.0), output.cues)
        assertFalse(output.chargeActive)
    }

    @Test
    fun retryResetsCollectionHistoryAndAlternatesFlaps() {
        update(initial.copy(runCoins = 500, damageCount = 4))
        output.cues.clear()
        update(initial, run = 2)
        update(initial.copy(runCoins = 1), run = 2)
        repeat(4) { director.flap() }
        assertEquals(
            listOf(
                GameSound.START,
                GameSound.COIN,
                GameSound.FLAP_ONE,
                GameSound.FLAP_TWO,
                GameSound.FLAP_THREE,
                GameSound.FLAP_ONE,
            ),
            output.cues.map { it.first },
        )
        assertFalse(output.cues.any { it.first == GameSound.DAMAGE })
        update(initial, visible = false)
        assertFalse(output.musicActive)
        assertFalse(output.chargeActive)
    }

    private fun update(
        snapshot: GameSnapshot,
        run: Int = 1,
        visible: Boolean = true,
        paused: Boolean = false,
    ) = director.update(snapshot, run, visible, paused)

    private fun boss(frame: Int) = CatapultSnapshot(800.0, 200.0, 512.0, false, frameIndex = frame)

    private fun projectile(
        id: Int,
        x: Double,
    ) = Projectile(id, x, 300.0, 64.0, -400.0, 0.0)

    private class RecordingOutput : GameAudioOutput {
        val cues = mutableListOf<Pair<GameSound, Double>>()
        var musicActive = false
        var musicPaused = false
        var chargeActive = false
        var chargePaused = false

        override fun play(
            sound: GameSound,
            delaySeconds: Double,
        ) {
            cues += sound to delaySeconds
        }

        override fun music(
            active: Boolean,
            paused: Boolean,
            boss: Boolean,
        ) {
            musicActive = active
            musicPaused = paused
        }

        override fun charge(
            active: Boolean,
            paused: Boolean,
        ) {
            chargeActive = active
            chargePaused = paused
        }

        override fun stopEffects() = Unit
    }
}
