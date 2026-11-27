package com.redcrow.dragonsfirstflight.game.engine

/** Chooses which patterns a zone and difficulty allow and expands a pattern into obstacle lanes. */
internal object ObstaclePatternPlanner {
    private val top = GameEngineConfig.OBSTACLE_TOP_Y
    private val middle = GameEngineConfig.OBSTACLE_Y
    private val lowerMiddle = GameEngineConfig.OBSTACLE_LOWER_MIDDLE_Y
    private val bottom = GameEngineConfig.OBSTACLE_BOTTOM_Y

    /** Returns the patterns allowed for the zone and difficulty; the boss zone has none. */
    fun availablePatterns(
        zone: GameZone,
        difficulty: GameDifficulty,
    ): List<ObstaclePattern> {
        if (zone == GameZone.BOSS) return emptyList()

        val easyPatterns =
            when (zone) {
                GameZone.ZONE_ONE ->
                    listOf(
                        ObstaclePattern.SINGLE,
                        ObstaclePattern.STAIR_UP,
                        ObstaclePattern.STAIR_DOWN,
                    )
                GameZone.ZONE_TWO,
                GameZone.ZONE_THREE,
                ->
                    listOf(
                        ObstaclePattern.SINGLE,
                        ObstaclePattern.STAIR_UP,
                        ObstaclePattern.STAIR_DOWN,
                        ObstaclePattern.MIDDLE_WEAVE,
                    )
                GameZone.BOSS -> emptyList()
            }

        if (difficulty == GameDifficulty.EASY) return easyPatterns

        val mediumAdditions =
            when (zone) {
                GameZone.ZONE_ONE ->
                    listOf(
                        ObstaclePattern.MIDDLE_WEAVE,
                        ObstaclePattern.FAKE_OPENING,
                    )
                GameZone.ZONE_TWO ->
                    listOf(
                        ObstaclePattern.ZIGZAG,
                        ObstaclePattern.DIAMOND,
                        ObstaclePattern.FAKE_OPENING,
                        ObstaclePattern.ALTERNATING,
                    )
                GameZone.ZONE_THREE ->
                    listOf(
                        ObstaclePattern.ZIGZAG,
                        ObstaclePattern.DIAMOND,
                        ObstaclePattern.FAKE_OPENING,
                        ObstaclePattern.ALTERNATING,
                        ObstaclePattern.DOUBLE_PRESSURE,
                    )
                GameZone.BOSS -> emptyList()
            }

        if (difficulty == GameDifficulty.MEDIUM) {
            return easyPatterns + mediumAdditions
        }

        val hardAdditions =
            when (zone) {
                GameZone.ZONE_ONE ->
                    listOf(
                        ObstaclePattern.ZIGZAG,
                        ObstaclePattern.ALTERNATING,
                    )
                GameZone.ZONE_TWO ->
                    listOf(
                        ObstaclePattern.ALTERNATING,
                        ObstaclePattern.DOUBLE_PRESSURE,
                    )
                GameZone.ZONE_THREE ->
                    listOf(
                        ObstaclePattern.ALTERNATING,
                        ObstaclePattern.DOUBLE_PRESSURE,
                        ObstaclePattern.ZIGZAG,
                        ObstaclePattern.DIAMOND,
                    )
                GameZone.BOSS -> emptyList()
            }

        return easyPatterns + mediumAdditions + hardAdditions
    }

    /** Returns the obstacle lane y values in spawn order; [reverseVariant] picks the mirrored form where one exists. */
    fun lanesFor(
        pattern: ObstaclePattern,
        singleLane: Double,
        reverseVariant: Boolean = false,
    ): List<Double> {
        require(singleLane in allLanes()) {
            "Single obstacle lane must be a configured obstacle lane."
        }

        return when (pattern) {
            ObstaclePattern.SINGLE -> listOf(singleLane)
            ObstaclePattern.STAIR_UP ->
                listOf(bottom, lowerMiddle, middle, top)
            ObstaclePattern.STAIR_DOWN ->
                listOf(top, middle, lowerMiddle, bottom)
            ObstaclePattern.ZIGZAG ->
                listOf(top, lowerMiddle, middle, bottom)
            ObstaclePattern.DIAMOND ->
                listOf(middle, top, middle, bottom, middle)
            ObstaclePattern.ALTERNATING ->
                if (reverseVariant) {
                    listOf(bottom, top, bottom, top)
                } else {
                    listOf(top, bottom, top, bottom)
                }
            ObstaclePattern.MIDDLE_WEAVE ->
                if (reverseVariant) {
                    listOf(middle, lowerMiddle, top, middle)
                } else {
                    listOf(middle, top, lowerMiddle, middle)
                }
            ObstaclePattern.FAKE_OPENING ->
                if (reverseVariant) {
                    listOf(bottom, lowerMiddle, bottom)
                } else {
                    listOf(top, middle, top)
                }
            ObstaclePattern.DOUBLE_PRESSURE ->
                if (reverseVariant) {
                    listOf(bottom, bottom, top)
                } else {
                    listOf(top, top, bottom)
                }
        }
    }

    /** Returns the four obstacle lane y values. */
    fun allLanes(): List<Double> = listOf(top, middle, lowerMiddle, bottom)
}
