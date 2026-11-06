package com.redcrow.dragonsfirstflight.game.engine

internal object ObstaclePatternPlanner {
    private val top = GameEngineConfig.OBSTACLE_TOP_Y
    private val middle = GameEngineConfig.OBSTACLE_Y
    private val lowerMiddle = GameEngineConfig.OBSTACLE_LOWER_MIDDLE_Y
    private val bottom = GameEngineConfig.OBSTACLE_BOTTOM_Y

    fun availablePatterns(zone: GameZone): List<ObstaclePattern> =
        when (zone) {
            GameZone.ZONE_ONE ->
                listOf(
                    ObstaclePattern.SINGLE,
                    ObstaclePattern.STAIR_UP,
                    ObstaclePattern.STAIR_DOWN,
                )

            GameZone.ZONE_TWO ->
                listOf(
                    ObstaclePattern.SINGLE,
                    ObstaclePattern.STAIR_UP,
                    ObstaclePattern.STAIR_DOWN,
                    ObstaclePattern.ZIGZAG,
                )

            GameZone.ZONE_THREE ->
                listOf(
                    ObstaclePattern.STAIR_UP,
                    ObstaclePattern.STAIR_DOWN,
                    ObstaclePattern.ZIGZAG,
                    ObstaclePattern.DIAMOND,
                )
        }

    fun lanesFor(
        pattern: ObstaclePattern,
        singleLane: Double,
    ): List<Double> {
        require(singleLane in allLanes()) {
            "Single obstacle lane must be a configured obstacle lane."
        }

        return when (pattern) {
            ObstaclePattern.SINGLE -> listOf(singleLane)
            ObstaclePattern.STAIR_UP -> listOf(bottom, lowerMiddle, middle, top)
            ObstaclePattern.STAIR_DOWN -> listOf(top, middle, lowerMiddle, bottom)
            ObstaclePattern.ZIGZAG -> listOf(top, lowerMiddle, middle, bottom)
            ObstaclePattern.DIAMOND -> listOf(middle, top, middle, bottom, middle)
        }
    }

    fun allLanes(): List<Double> = listOf(top, middle, lowerMiddle, bottom)
}
