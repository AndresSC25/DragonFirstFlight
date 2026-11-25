package com.redcrow.dragonsfirstflight.game.audio

internal const val INTRO_EFFECTS_PATH = "audio/cinematic/intro_effects.wav"
internal const val INTRO_EFFECTS_VOLUME = 0.65

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
