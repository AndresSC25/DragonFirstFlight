package com.redcrow.dragonsfirstflight.game.audio

import korlibs.audio.format.WAV
import korlibs.audio.sound.AudioData
import korlibs.audio.sound.PlaybackParameters
import korlibs.audio.sound.PlaybackTimes
import korlibs.audio.sound.Sound
import korlibs.audio.sound.SoundChannel
import korlibs.audio.sound.SoundChannelState
import korlibs.audio.sound.readAudioData
import korlibs.event.FocusEvent
import korlibs.event.ResumeEvent
import korlibs.io.file.std.resourcesVfs
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
import kotlin.time.Duration.Companion.seconds

class GameAudioTest {
    @Test
    fun everyBundledClipDecodesAndContainsAudibleSamples() =
        runBlocking {
            val paths = GameSound.entries.map { it.path } + GameMusic.entries.map { it.path } + INTRO_EFFECTS_PATH
            for (path in paths) {
                val data = resourcesVfs[path].readAudioData(WAV)
                assertEquals(44100, data.rate, path)
                val stereo = path == GameMusic.INTRO.path || path == GameMusic.BOSS.path
                assertEquals(if (stereo) 2 else 1, data.channels, path)
                assertTrue(data.totalSamples > 1000, path)
                assertTrue((0 until data.totalSamples).any { data.samples[0, it].toInt() != 0 }, path)
                val seconds = data.totalSamples.toDouble() / data.rate
                when (path) {
                    GameMusic.STAGES.path -> assertTrue(seconds > 90, path)
                    GameMusic.BOSS.path -> assertTrue(seconds in 60.0..90.0, path)
                    GameMusic.INTRO.path -> assertEquals(12.5, seconds, path)
                    INTRO_EFFECTS_PATH -> assertEquals(12.5, seconds, path)
                }
            }
        }

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
    fun introFollowsRestoredVideoPositionsAndStopsPermanentlyOnSkip() {
        val sound = RecordingSound(EmptyCoroutineContext)
        val intro = IntroMusicPlayback(EmptyCoroutineContext, sound)
        intro.update(null, paused = false)
        assertTrue(sound.channels.isEmpty())
        intro.update(4.0, paused = false)
        assertEquals(4.seconds, sound.parameters.single().startTime)
        assertEquals(PlaybackTimes.ONE, sound.parameters.single().times)
        intro.pause()
        assertEquals(SoundChannelState.PAUSED, sound.channels.single().state)
        intro.update(4.1, paused = false)
        assertEquals(1, sound.channels.size)
        assertEquals(4.1.seconds, sound.channels.single().current)
        intro.update(8.0, paused = false)
        assertEquals(4.1.seconds, sound.channels.single().current)
        assertEquals(1, sound.channels.single().seekCount)
        intro.close()
        intro.close()
        intro.update(9.0, paused = false)
        assertEquals(SoundChannelState.STOPPED, sound.channels.single().state)
        assertEquals(1, sound.channels.size)
    }

    @Test
    fun bufferedAudioClockAndIrregularVideoFramesDoNotCauseRepeatedSeeks() {
        val music = RecordingSound(EmptyCoroutineContext)
        val effects = RecordingSound(EmptyCoroutineContext)
        val intro = IntroMusicPlayback(EmptyCoroutineContext, music, effects)
        intro.update(0.0, paused = false)
        repeat(300) { frame ->
            val videoTime = (frame / 24.0).coerceAtMost(12.49)
            // Audio backends can include buffered samples in their reported cursor.
            music.channels.single().reportedPosition = (videoTime + 0.45).seconds
            effects.channels.single().reportedPosition = (videoTime + 0.6).seconds
            intro.update(videoTime, paused = false)
        }
        assertEquals(0, music.channels.single().seekCount)
        assertEquals(0, effects.channels.single().seekCount)
        assertEquals(1, music.channels.size)
        assertEquals(1, effects.channels.size)
        intro.close()
    }

    @Test
    fun anAsynchronousResumeIsIssuedOnceEvenIfTheBackendStillReportsPaused() {
        val sound = RecordingSound(EmptyCoroutineContext)
        val intro = IntroMusicPlayback(EmptyCoroutineContext, sound)
        intro.update(1.0, paused = false)
        val channel = sound.channels.single()
        channel.deferResume = true
        repeat(40) { intro.update(1.2, paused = true) }
        repeat(40) { intro.update(1.2 + it / 24.0, paused = false) }
        assertEquals(1, channel.seekCount)
        assertEquals(1, channel.resumeCount)
        assertEquals(1, channel.pauseCount)
        intro.close()
    }

    @Test
    fun completedIntroMusicDoesNotReplayAndMusicMuteAlsoAppliesToTheIntro() =
        runBlocking {
            val sound = RecordingSound(EmptyCoroutineContext)
            val intro = IntroMusicPlayback(EmptyCoroutineContext, sound)
            intro.update(0.0, paused = false)
            sound.channels.single().stop()
            repeat(20) { intro.update(12.5, paused = false) }
            assertEquals(1, sound.channels.size)
            intro.close()
            val muted = IntroMusicPlayback.load(EmptyCoroutineContext, enabled = false)
            muted.update(0.0, paused = false)
            muted.pause()
            muted.close()
        }

    @Test
    fun introMusicAndEffectsStayTogetherAcrossPauseSeekAndSkip() {
        val music = RecordingSound(EmptyCoroutineContext)
        val effects = RecordingSound(EmptyCoroutineContext)
        val intro = IntroMusicPlayback(EmptyCoroutineContext, music, effects)
        intro.update(2.5, paused = false)
        assertEquals(2.5.seconds, music.parameters.single().startTime)
        assertEquals(2.5.seconds, effects.parameters.single().startTime)
        assertEquals(0.30, music.parameters.single().volume)
        assertEquals(INTRO_EFFECTS_VOLUME, effects.parameters.single().volume)
        intro.pause()
        assertEquals(SoundChannelState.PAUSED, music.channels.single().state)
        assertEquals(SoundChannelState.PAUSED, effects.channels.single().state)
        intro.update(2.7, paused = false)
        assertEquals(2.7.seconds, music.channels.single().current)
        assertEquals(2.7.seconds, effects.channels.single().current)
        intro.close()
        intro.update(3.0, paused = false)
        assertEquals(SoundChannelState.STOPPED, music.channels.single().state)
        assertEquals(SoundChannelState.STOPPED, effects.channels.single().state)
        assertEquals(1, effects.channels.size)
    }

    @Test
    fun introEffectsWorkWithMusicMutedAndMusicWorksWithEffectsMuted() {
        val effects = RecordingSound(EmptyCoroutineContext)
        val onlyEffects = IntroMusicPlayback(EmptyCoroutineContext, null, effects)
        onlyEffects.update(0.0, paused = false)
        assertEquals(1, effects.channels.size)
        onlyEffects.pause()
        assertEquals(SoundChannelState.PAUSED, effects.channels.single().state)
        onlyEffects.close()
        assertEquals(SoundChannelState.STOPPED, effects.channels.single().state)
        val music = RecordingSound(EmptyCoroutineContext)
        val onlyMusic = IntroMusicPlayback(EmptyCoroutineContext, music)
        onlyMusic.update(0.0, paused = false)
        assertEquals(1, music.channels.size)
        onlyMusic.close()
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
