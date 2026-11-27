package com.redcrow.dragonsfirstflight.game.engine

/** Named lane sequences the generator spawns obstacles in; [isIntense] ones trigger a longer gap afterwards. */
internal enum class ObstaclePattern(
    val isIntense: Boolean = false,
) {
    SINGLE,
    STAIR_UP,
    STAIR_DOWN,
    ZIGZAG(isIntense = true),
    DIAMOND(isIntense = true),
    ALTERNATING(isIntense = true),
    MIDDLE_WEAVE,
    FAKE_OPENING(isIntense = true),
    DOUBLE_PRESSURE(isIntense = true),
}
