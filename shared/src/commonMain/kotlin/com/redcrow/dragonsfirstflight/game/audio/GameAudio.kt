package com.redcrow.dragonsfirstflight.game.audio

import korlibs.audio.sound.PlaybackParameters
import korlibs.audio.sound.PlaybackTimes
import korlibs.audio.sound.Sound
import korlibs.audio.sound.SoundChannel
import korlibs.audio.sound.SoundChannelState
import korlibs.audio.sound.readMusic
import korlibs.audio.sound.readSound
import korlibs.event.FocusEvent
import korlibs.event.PauseEvent
import korlibs.event.ResumeEvent
import korlibs.io.file.std.resourcesVfs
import korlibs.korge.service.storage.IStorage
import korlibs.render.GameWindow
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.time.TimeSource

internal class GameAudio(
    private val context: CoroutineContext,
    private val storage: IStorage,
    private val sounds: MutableMap<GameSound, Sound>,
    private val stageMusic: Sound?,
    private val bossMusic: Sound? = null,
    private val introMusic: Sound? = null,
    private val introEffects: Sound? = null,
) : GameAudioOutput {
    var soundEnabled = storage.getOrNull("audio.sound") != "false"
        private set
    var musicEnabled = storage.getOrNull("audio.music") != "false"
        private set

    private val scope = CoroutineScope(context)
    private val clock = TimeSource.Monotonic.markNow()
    private val lastPlayed = mutableMapOf<GameSound, Double>()
    private val effects = mutableListOf<SoundChannel>()
    private val pending = mutableListOf<Job>()
    private var musicChannel: SoundChannel? = null
    private var chargeChannel: SoundChannel? = null
    private var foreground = true
    private var closed = false
    private var lastFlap = Double.NEGATIVE_INFINITY

    fun bindWindow(window: GameWindow) {
        fun background() {
            foreground = false
            stopEffects()
            musicChannel?.pause()
            chargeChannel?.pause()
        }
        window.onEvent(FocusEvent.Type.BLUR) { background() }
        window.onEvent(PauseEvent) { background() }
        window.onEvent(FocusEvent.Type.FOCUS) { foreground = true }
        window.onEvent(ResumeEvent) { foreground = true }
        context[Job]?.invokeOnCompletion { close() }
    }

    fun toggleSound() {
        soundEnabled = !soundEnabled
        storage["audio.sound"] = soundEnabled.toString()
        if (!soundEnabled) {
            stopEffects()
            chargeChannel?.stop()
            chargeChannel = null
        } else {
            play(GameSound.MENU_CLICK)
        }
    }

    fun toggleMusic() {
        musicEnabled = !musicEnabled
        storage["audio.music"] = musicEnabled.toString()
        if (!musicEnabled) musicChannel?.pause()
        play(GameSound.MENU_CLICK)
    }

    override fun play(
        sound: GameSound,
        delaySeconds: Double,
    ) {
        if (!soundEnabled || !foreground || closed) return
        if (delaySeconds > 0.0) {
            pending.removeAll { it.isCompleted }
            pending +=
                scope.launch {
                    delay((delaySeconds * 1000).toLong())
                    play(sound)
                }
            return
        }
        val now = clock.elapsedNow().inWholeMilliseconds / 1000.0
        if (now - (lastPlayed[sound] ?: Double.NEGATIVE_INFINITY) < sound.minimumIntervalSeconds) return
        val flap = sound == GameSound.FLAP_ONE || sound == GameSound.FLAP_TWO || sound == GameSound.FLAP_THREE
        if (flap && now - lastFlap < 0.09) return
        if (flap) lastFlap = now
        lastPlayed[sound] = now
        effects.removeAll { it.state == SoundChannelState.STOPPED }
        if (effects.size >= 12) effects.removeAt(0).stop()
        playChannel(sounds[sound], sound.volume)?.let(effects::add)
    }

    override fun music(
        active: Boolean,
        paused: Boolean,
        boss: Boolean,
    ) {
        val selectedMusic = if (boss) bossMusic ?: stageMusic else stageMusic
        if (musicChannel != null && musicChannel?.sound !== selectedMusic) {
            musicChannel?.stop()
            musicChannel = null
        }
        val volume = if (boss) GameMusic.BOSS.volume else GameMusic.STAGES.volume
        if (!active || closed) {
            musicChannel?.stop()
            musicChannel = null
        } else if (paused || !foreground || !musicEnabled) {
            musicChannel?.pause()
        } else {
            if (musicChannel == null || musicChannel?.state == SoundChannelState.STOPPED) {
                musicChannel = playChannel(selectedMusic, volume, loop = true)
            }
            musicChannel?.volume = volume
            if (musicChannel?.state == SoundChannelState.PAUSED) musicChannel?.resume()
        }
    }

    override fun charge(
        active: Boolean,
        paused: Boolean,
    ) {
        if (!active || closed || !soundEnabled) {
            chargeChannel?.stop()
            chargeChannel = null
        } else if (paused || !foreground) {
            chargeChannel?.pause()
        } else {
            if (chargeChannel == null || chargeChannel?.state == SoundChannelState.STOPPED) {
                chargeChannel =
                    playChannel(sounds[GameSound.CATAPULT_CHARGE], GameSound.CATAPULT_CHARGE.volume, loop = true)
            }
            if (chargeChannel?.state == SoundChannelState.PAUSED) chargeChannel?.resume()
        }
    }

    override fun stopEffects() {
        pending.forEach { it.cancel() }
        pending.clear()
        effects.forEach { it.stop() }
        effects.clear()
    }

    fun previewIntro() {
        stopEffects()
        if (musicEnabled && foreground && !closed) {
            playChannel(introMusic, GameMusic.INTRO.volume)?.let(effects::add)
        }
    }

    fun previewIntroEffects() {
        stopEffects()
        if (soundEnabled && foreground && !closed) {
            playChannel(introEffects, INTRO_EFFECTS_VOLUME)?.let(effects::add)
        }
    }

    private fun playChannel(
        sound: Sound?,
        volume: Double,
        loop: Boolean = false,
    ): SoundChannel? =
        try {
            sound?.play(
                context,
                PlaybackParameters(volume = volume, times = if (loop) PlaybackTimes.INFINITE else PlaybackTimes.ONE),
            )
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            println("Game audio playback unavailable: ${error.message}")
            null
        }

    fun close() {
        if (closed) return
        closed = true
        stopEffects()
        musicChannel?.stop()
        chargeChannel?.stop()
        sounds.clear()
    }

    companion object {
        suspend fun load(
            context: CoroutineContext,
            storage: IStorage,
        ): GameAudio {
            val sounds = mutableMapOf<GameSound, Sound>()
            for (sound in GameSound.entries) {
                try {
                    sounds[sound] = resourcesVfs[sound.path].readSound()
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (error: Exception) {
                    println("Game audio unavailable (${sound.fileName}): ${error.message}")
                }
            }
            val music = mutableMapOf<GameMusic, Sound>()
            for (track in GameMusic.entries) {
                try {
                    music[track] = resourcesVfs[track.path].readMusic()
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (error: Exception) {
                    println("Game music unavailable (${track.fileName}): ${error.message}")
                }
            }
            val introEffects =
                try {
                    resourcesVfs[INTRO_EFFECTS_PATH].readMusic()
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (error: Exception) {
                    println("Intro effects unavailable: ${error.message}")
                    null
                }
            return GameAudio(
                context,
                storage,
                sounds,
                music[GameMusic.STAGES],
                music[GameMusic.BOSS],
                music[GameMusic.INTRO],
                introEffects,
            )
        }
    }
}
