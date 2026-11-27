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
    private var bossAnnounced = false
    private var flapIndex = 0

    /** Plays the next of the three flap sounds in rotation. */
    fun flap() {
        val sounds = listOf(GameSound.FLAP_ONE, GameSound.FLAP_TWO, GameSound.FLAP_THREE)
        output.play(sounds[flapIndex])
        flapIndex = (flapIndex + 1) % sounds.size
    }

    /**
     * Call once per frame with the current [snapshot]; compares it with the previous one to trigger cues.
     * A new [runNumber] or a hidden screen resets tracking; a hidden screen also silences music and charge.
     */
    fun update(
        snapshot: GameSnapshot,
        runNumber: Int,
        visible: Boolean,
        paused: Boolean,
    ) {
        if (!visible) {
            output.music(active = false, paused = false, boss = false)
            output.charge(active = false, paused = false)
            previous = null
            wasVisible = false
            wasPaused = false
            return
        }
        val newRun = runNumber != previousRun || !wasVisible
        if (newRun) {
            output.stopEffects()
            previous = null
            bossAnnounced = false
            flapIndex = 0
            wasPaused = false
            output.play(GameSound.START)
        }
        val before = previous
        val running = snapshot.status == GameStatus.RUNNING
        val boss = snapshot.catapult
        if (paused && !wasPaused) {
            output.stopEffects()
            output.play(GameSound.PAUSE)
        } else if (!paused && wasPaused) {
            output.play(GameSound.RESUME)
        }
        output.music(active = running, paused = paused, boss = boss != null)
        output.charge(
            active = running && !snapshot.isZoneTransitioning && boss != null && boss.frameIndex in 1..4,
            paused = paused,
        )
        if (!paused) {
            val continued = before?.mode == GameMode.NORMAL && snapshot.mode == GameMode.ENDLESS
            if (continued) {
                output.stopEffects()
                output.play(GameSound.ENDLESS)
            } else if (snapshot.isZoneTransitioning && before?.isZoneTransitioning == false) {
                output.play(GameSound.TRANSITION)
            }
            if (boss != null && running && !snapshot.isZoneTransitioning && !bossAnnounced) {
                bossAnnounced = true
                output.play(GameSound.BOSS_INTRO)
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
                            if (boss != null) output.play(GameSound.CATAPULT_DESTROY)
                            output.play(GameSound.VICTORY, delaySeconds = if (boss != null) 0.8 else 0.0)
                        }
                        GameStatus.RUNNING -> Unit
                    }
                }
                if (running && !snapshot.isZoneTransitioning) {
                    if (boss?.frameIndex == 4 &&
                        before.catapult?.frameIndex != 4
                    ) {
                        output.play(GameSound.CATAPULT_WARNING)
                    }
                    val oldProjectiles = before.projectiles.associateBy { it.id }
                    if (snapshot.projectiles.any { it.id !in oldProjectiles }) output.play(GameSound.CATAPULT_FIRE)
                    val flyByX = snapshot.dragonX + 220.0
                    if (snapshot.projectiles.any { it.x <= flyByX && (oldProjectiles[it.id]?.x ?: it.x) > flyByX }) {
                        output.play(GameSound.PROJECTILE)
                    }
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
