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
    fun pauseFreezesLoopsAndReturningDoesNotResumeUntilRequested() {
        val loading = initial.copy(catapult = boss(frame = 2))
        update(loading)
        output.cues.clear()
        repeat(50) { update(loading, paused = true) }
        assertEquals(listOf(GameSound.PAUSE to 0.0), output.cues)
        assertTrue(output.musicPaused)
        assertTrue(output.chargePaused)
        update(loading)
        assertEquals(GameSound.RESUME, output.cues.last().first)
        assertFalse(output.musicPaused)
        assertFalse(output.chargePaused)
    }

    @Test
    fun catapultHasOneIntroWarningAndShotPerEvent() {
        val entering = initial.copy(catapult = boss(frame = 0), isZoneTransitioning = true)
        update(entering)
        assertFalse(output.cues.any { it.first == GameSound.BOSS_INTRO })
        val loading = entering.copy(catapult = boss(frame = 1), isZoneTransitioning = false)
        repeat(20) { update(loading) }
        assertTrue(output.chargeActive)
        val warning = loading.copy(catapult = boss(frame = 4))
        repeat(20) { update(warning) }
        assertTrue(output.chargeActive)
        val fired =
            warning.copy(
                catapult = boss(frame = 5),
                projectiles = listOf(projectile(1, 800.0), projectile(2, 820.0)),
            )
        repeat(20) { update(fired) }
        assertFalse(output.chargeActive)
        assertEquals(1, output.cues.count { it.first == GameSound.BOSS_INTRO })
        assertEquals(1, output.cues.count { it.first == GameSound.CATAPULT_WARNING })
        assertEquals(1, output.cues.count { it.first == GameSound.CATAPULT_FIRE })
        update(fired.copy(projectiles = listOf(projectile(1, 340.0), projectile(2, 350.0))))
        assertEquals(1, output.cues.count { it.first == GameSound.PROJECTILE })
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
    fun finalBattlefieldInEndlessDoesNotAnnounceABoss() {
        repeat(50) { update(initial.copy(mode = GameMode.ENDLESS, zone = GameZone.BOSS)) }
        assertFalse(output.cues.any { it.first == GameSound.BOSS_INTRO })
        assertFalse(output.chargeActive)
        assertTrue(output.musicActive)
    }

    @Test
    fun defeatFinishesBeforeGameOverAndVictoryFollowsDestruction() {
        update(initial)
        output.cues.clear()
        repeat(20) { update(initial.copy(status = GameStatus.LOST, lives = 0)) }
        assertEquals(listOf(GameSound.DEFEAT to 0.0, GameSound.GAME_OVER to 0.86), output.cues)
        assertFalse(output.musicActive)
        update(initial, run = 2)
        output.cues.clear()
        repeat(20) { update(initial.copy(catapult = boss(0), status = GameStatus.WON), run = 2) }
        assertEquals(listOf(GameSound.CATAPULT_DESTROY to 0.0, GameSound.VICTORY to 0.8), output.cues)
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
