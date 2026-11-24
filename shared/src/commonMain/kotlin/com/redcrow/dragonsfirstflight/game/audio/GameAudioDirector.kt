package com.redcrow.dragonsfirstflight.game.audio

import com.redcrow.dragonsfirstflight.game.engine.GameMode
import com.redcrow.dragonsfirstflight.game.engine.GameSnapshot
import com.redcrow.dragonsfirstflight.game.engine.GameStatus

/** Converts state changes into cues once, independently of rendering frame rate. */
internal class GameAudioDirector(
    private val output: GameAudioOutput,
) {
    private var previous: GameSnapshot? = null
    private var previousRun = -1
    private var wasVisible = false
    private var wasPaused = false
    private var flapIndex = 0

    fun flap() {
        val sounds = listOf(GameSound.FLAP_ONE, GameSound.FLAP_TWO, GameSound.FLAP_THREE)
        output.play(sounds[flapIndex])
        flapIndex = (flapIndex + 1) % sounds.size
    }

    fun update(
        snapshot: GameSnapshot,
        runNumber: Int,
        visible: Boolean,
        paused: Boolean,
    ) {
        if (!visible) {
            previous = null
            wasVisible = false
            wasPaused = false
            return
        }
        val newRun = runNumber != previousRun || !wasVisible
        if (newRun) {
            output.stopEffects()
            previous = null
            flapIndex = 0
            wasPaused = false
            output.play(GameSound.START)
        }
        val before = previous
        val running = snapshot.status == GameStatus.RUNNING
        if (paused && !wasPaused) {
            output.stopEffects()
            output.play(GameSound.PAUSE)
        } else if (!paused && wasPaused) {
            output.play(GameSound.RESUME)
        }
        if (!paused) {
            val continued = before?.mode == GameMode.NORMAL && snapshot.mode == GameMode.ENDLESS
            if (continued) {
                output.stopEffects()
                output.play(GameSound.ENDLESS)
            } else if (snapshot.isZoneTransitioning && before?.isZoneTransitioning == false) {
                output.play(GameSound.TRANSITION)
            }
            if (before != null && !continued) {
                if (snapshot.runCoins > before.runCoins) output.play(GameSound.COIN)
                if (snapshot.extraLivesCollected > before.extraLivesCollected) output.play(GameSound.EXTRA_LIFE)
                if (snapshot.damageCount > before.damageCount && running) output.play(GameSound.DAMAGE)
                if (snapshot.status != before.status) {
                    when (snapshot.status) {
                        GameStatus.LOST -> {
                            output.stopEffects()
                            output.play(GameSound.DEFEAT)
                            output.play(GameSound.GAME_OVER, delaySeconds = 0.86)
                        }
                        GameStatus.WON -> {
                            output.stopEffects()
                            output.play(GameSound.VICTORY)
                        }
                        GameStatus.RUNNING -> Unit
                    }
                }
                if (running && !snapshot.isZoneTransitioning) {
                    val warnedIds =
                        before.obstacles
                            .filter { it.rushWarningRemainingSeconds > 0.0 }
                            .map { it.id }
                            .toSet()
                    if (snapshot.obstacles.any { it.rushWarningRemainingSeconds > 0.0 && it.id !in warnedIds }) {
                        output.play(GameSound.RUSH)
                    }
                }
            }
        }
        previous = snapshot
        previousRun = runNumber
        wasVisible = true
        wasPaused = paused
    }
}
