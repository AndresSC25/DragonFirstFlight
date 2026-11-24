package com.redcrow.dragonsfirstflight.game.audio

import korlibs.audio.sound.AudioData
import korlibs.audio.sound.PlaybackParameters
import korlibs.audio.sound.PlaybackTimes
import korlibs.audio.sound.Sound
import korlibs.audio.sound.SoundChannel
import korlibs.audio.sound.SoundChannelState
import korlibs.event.FocusEvent
import korlibs.event.ResumeEvent
import korlibs.korge.service.storage.InmemoryStorage
import korlibs.render.GameWindow
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration

class GameAudioTest {
    @Test
    fun musicAndChargeResumeTheSameChannelAndNeverDuplicateLoops() {
        val fixture = Fixture()
        repeat(100) {
            fixture.audio.music(active = true, paused = false, boss = false)
            fixture.audio.charge(active = true, paused = false)
        }
        assertEquals(2, fixture.sound.channels.size)
        assertTrue(fixture.sound.parameters.all { it.times == PlaybackTimes.INFINITE })
        fixture.audio.music(active = true, paused = true, boss = false)
        fixture.audio.charge(active = true, paused = true)
        assertTrue(fixture.sound.channels.all { it.state == SoundChannelState.PAUSED })
        fixture.audio.music(active = true, paused = false, boss = true)
        fixture.audio.charge(active = true, paused = false)
        assertEquals(2, fixture.sound.channels.size)
        assertTrue(fixture.sound.channels.all { it.state == SoundChannelState.PLAYING })
        assertEquals(
            0.18,
            fixture.sound.channels
                .first()
                .volume,
        )
        fixture.audio.close()
        assertTrue(fixture.sound.channels.all { it.state == SoundChannelState.STOPPED })
    }

    @Test
    fun bossReplacesStageMusicAndEndlessReturnsToANewStageLoop() {
        val stage = RecordingSound(EmptyCoroutineContext)
        val boss = RecordingSound(EmptyCoroutineContext)
        val audio = GameAudio(EmptyCoroutineContext, InmemoryStorage(), mutableMapOf(), stage, boss)
        audio.music(active = true, paused = false, boss = false)
        repeat(100) { audio.music(active = true, paused = false, boss = true) }
        assertEquals(SoundChannelState.STOPPED, stage.channels.single().state)
        assertEquals(1, boss.channels.size)
        assertEquals(PlaybackTimes.INFINITE, boss.parameters.single().times)
        audio.music(active = true, paused = true, boss = true)
        assertEquals(SoundChannelState.PAUSED, boss.channels.single().state)
        audio.music(active = true, paused = false, boss = true)
        assertEquals(1, boss.channels.size)
        audio.music(active = true, paused = false, boss = false)
        assertEquals(SoundChannelState.STOPPED, boss.channels.single().state)
        assertEquals(2, stage.channels.size)
        audio.close()
    }

    @Test
    fun backgroundStopsEffectsAndFocusAloneDoesNotRestartTheMusic() {
        val fixture = Fixture()
        val window = GameWindow()
        fixture.audio.bindWindow(window)
        fixture.audio.music(active = true, paused = false, boss = false)
        fixture.audio.play(GameSound.COIN)
        window.dispatch(FocusEvent(FocusEvent.Type.BLUR))
        assertEquals(
            SoundChannelState.PAUSED,
            fixture.sound.channels
                .first()
                .state,
        )
        assertEquals(
            SoundChannelState.STOPPED,
            fixture.sound.channels
                .last()
                .state,
        )
        fixture.audio.play(GameSound.DAMAGE)
        assertEquals(2, fixture.sound.channels.size)
        window.dispatch(FocusEvent(FocusEvent.Type.FOCUS))
        fixture.audio.play(GameSound.DAMAGE)
        assertEquals(3, fixture.sound.channels.size)
        window.dispatch(ResumeEvent())
        assertEquals(
            SoundChannelState.PAUSED,
            fixture.sound.channels
                .first()
                .state,
        )
        fixture.audio.music(active = true, paused = true, boss = false)
        assertEquals(
            SoundChannelState.PAUSED,
            fixture.sound.channels
                .first()
                .state,
        )
        fixture.audio.close()
    }

    @Test
    fun independentMutePreferencesPersistAcrossLaunches() {
        val fixture = Fixture()
        fixture.audio.toggleSound()
        assertFalse(fixture.audio.soundEnabled)
        assertTrue(fixture.audio.musicEnabled)
        fixture.audio.play(GameSound.COIN)
        assertTrue(fixture.sound.channels.isEmpty())
        fixture.audio.music(active = true, paused = false, boss = false)
        assertEquals(1, fixture.sound.channels.size)
        fixture.audio.toggleMusic()
        assertFalse(fixture.audio.musicEnabled)
        assertEquals(
            SoundChannelState.PAUSED,
            fixture.sound.channels
                .single()
                .state,
        )
        val restored = GameAudio(EmptyCoroutineContext, fixture.storage, mutableMapOf(), null)
        assertFalse(restored.musicEnabled)
        assertFalse(restored.soundEnabled)
        restored.close()
        fixture.audio.close()
    }

    @Test
    fun returningToMenuCancelsDelayedResultSounds() =
        runBlocking {
            val fixture = Fixture(coroutineContext)
            fixture.audio.play(GameSound.GAME_OVER, delaySeconds = 0.03)
            fixture.audio.stopEffects()
            delay(60)
            assertTrue(fixture.sound.channels.isEmpty())
            fixture.audio.close()
        }

    @Test
    fun repeatedPickupsAndFlapVariantsHaveBoundedPolyphony() {
        val fixture = Fixture()
        repeat(100) { fixture.audio.play(GameSound.COIN) }
        assertEquals(1, fixture.sound.channels.size)
        fixture.audio.play(GameSound.FLAP_ONE)
        fixture.audio.play(GameSound.FLAP_TWO)
        fixture.audio.play(GameSound.FLAP_THREE)
        assertEquals(2, fixture.sound.channels.size)
        GameSound.entries.forEach { fixture.audio.play(it) }
        assertTrue(fixture.sound.channels.count { it.state == SoundChannelState.PLAYING } <= 12)
        fixture.audio.close()
    }

    private class Fixture(
        context: CoroutineContext = EmptyCoroutineContext,
    ) {
        val storage = InmemoryStorage()
        val sound = RecordingSound(context)
        val audio =
            GameAudio(context, storage, GameSound.entries.associateWith { sound as Sound }.toMutableMap(), sound)
    }

    private class RecordingSound(
        context: CoroutineContext,
    ) : Sound(context) {
        val channels = mutableListOf<RecordingChannel>()
        val parameters = mutableListOf<PlaybackParameters>()

        override fun play(
            coroutineContext: CoroutineContext,
            params: PlaybackParameters,
        ): SoundChannel {
            parameters += params
            return RecordingChannel(this).also {
                it.volume = params.volume
                channels += it
            }
        }

        override suspend fun decode(maxSamples: Int): AudioData = error("Recording sound does not decode.")
    }

    private class RecordingChannel(
        sound: Sound,
    ) : SoundChannel(sound) {
        private var playbackState = SoundChannelState.PLAYING
        override val state get() = playbackState
        var reportedPosition: Duration = Duration.ZERO
        var seekCount = 0
        var resumeCount = 0
        var pauseCount = 0
        var deferResume = false
        override var current: Duration
            get() = reportedPosition
            set(value) {
                seekCount++
                reportedPosition = value
            }

        override fun stop() {
            playbackState = SoundChannelState.STOPPED
        }

        override fun pause() {
            pauseCount++
            playbackState = SoundChannelState.PAUSED
        }

        override fun resume() {
            resumeCount++
            if (!deferResume) playbackState = SoundChannelState.PLAYING
        }
    }
}
