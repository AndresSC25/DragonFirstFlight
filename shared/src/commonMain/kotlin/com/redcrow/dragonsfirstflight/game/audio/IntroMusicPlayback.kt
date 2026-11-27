package com.redcrow.dragonsfirstflight.game.audio

import korlibs.audio.sound.PlaybackParameters
import korlibs.audio.sound.Sound
import korlibs.audio.sound.SoundChannel
import korlibs.audio.sound.SoundChannelState
import korlibs.audio.sound.readSound
import korlibs.io.file.std.resourcesVfs
import kotlinx.coroutines.CancellationException
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.seconds

/** Independent music and Foley tracks follow the native video's clock, including restored positions. */
class IntroMusicPlayback internal constructor(
    private val context: CoroutineContext,
    private val sound: Sound?,
    effects: Sound? = null,
    private val volume: Double = GameMusic.INTRO.volume,
) {
    private val effectsPlayback = effects?.let { IntroMusicPlayback(context, it, volume = INTRO_EFFECTS_VOLUME) }
    private var channel: SoundChannel? = null
    private var started = false
    private var resumeAfterPause = false
    private var closed = false

    /**
     * Syncs playback to the video clock; call every frame. Starts at [positionSeconds], pauses when [paused] or the
     * position is null or invalid, and seeks only when resuming. A playback error closes it.
     */
    fun update(
        positionSeconds: Double?,
        paused: Boolean,
    ) {
        if (closed) return
        effectsPlayback?.update(positionSeconds, paused)
        if (sound == null) return
        if (paused || positionSeconds == null || !positionSeconds.isFinite() || positionSeconds < 0.0) {
            pause()
            return
        }
        try {
            val target = positionSeconds.seconds
            if (!started) {
                channel = sound.play(context, PlaybackParameters(volume = volume, startTime = target))
                started = true
            } else {
                val current = channel ?: return
                // A completed clip must not restart while the last video frame is displayed.
                if (current.state == SoundChannelState.STOPPED) return
                // Buffered audio and decoded video frames report different clocks.
                // Seeking every frame makes an otherwise continuous track stutter.
                if (resumeAfterPause) {
                    current.current = target
                    current.resume()
                    resumeAfterPause = false
                }
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            println("Intro music unavailable: ${error.message}")
            close()
        }
    }

    /** Pauses music and effects until the next valid [update]. */
    fun pause() {
        effectsPlayback?.pause()
        val current = channel ?: return
        if (closed || resumeAfterPause || current.state == SoundChannelState.STOPPED) return
        current.pause()
        resumeAfterPause = true
    }

    /** Stops playback for good; later calls do nothing. */
    fun close() {
        if (closed) return
        closed = true
        effectsPlayback?.close()
        channel?.stop()
        channel = null
    }

    companion object {
        /** Loads the intro music if [enabled] and the effects track if [effectsEnabled]; failed loads are skipped. */
        suspend fun load(
            context: CoroutineContext,
            enabled: Boolean,
            effectsEnabled: Boolean = false,
        ): IntroMusicPlayback {
            val sound =
                if (enabled) {
                    try {
                        resourcesVfs[GameMusic.INTRO.path].readSound()
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (error: Exception) {
                        println("Intro music unavailable: ${error.message}")
                        null
                    }
                } else {
                    null
                }
            val effects =
                if (effectsEnabled) {
                    try {
                        resourcesVfs[INTRO_EFFECTS_PATH].readSound()
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (error: Exception) {
                        println("Intro effects unavailable: ${error.message}")
                        null
                    }
                } else {
                    null
                }
            return IntroMusicPlayback(context, sound, effects)
        }
    }
}
