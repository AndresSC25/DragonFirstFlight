package com.redcrow.dragonsfirstflight.game.audio

/** Resource path of the intro cinematic's sound-effects track. */
internal const val INTRO_EFFECTS_PATH = "audio/cinematic/intro_effects.wav"

/** Playback volume of the intro effects track, from 0.0 to 1.0. */
internal const val INTRO_EFFECTS_VOLUME = 0.65

/** Music tracks with their file name (without extension) and playback volume from 0.0 to 1.0. */
internal enum class GameMusic(
    val fileName: String,
    val volume: Double,
) {
    STAGES("stages_endless_loop", 0.20),
    BOSS("boss_battle_loop", 0.18),
    INTRO("intro_cinematic", 0.30),
    ;

    val path get() = "audio/music/$fileName.wav"
}
