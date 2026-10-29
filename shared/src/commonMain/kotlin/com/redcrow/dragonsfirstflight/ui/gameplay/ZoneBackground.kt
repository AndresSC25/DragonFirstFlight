package com.redcrow.dragonsfirstflight.ui.gameplay

import korlibs.image.format.readBitmap
import korlibs.io.file.std.resourcesVfs

internal enum class ZoneBackground(
    private val layers: List<LayerAsset>,
) {
    FORTRESS_COURTYARD(
        listOf(
            LayerAsset("backgrounds/zone1/parallax/00_sky.png", speedFactor = 0.03, mirrorTiles = true),
            LayerAsset("backgrounds/zone1/parallax/01_distant_landscape.png", speedFactor = 0.12, mirrorTiles = true),
            LayerAsset("backgrounds/zone1/parallax/02_castle_wall.png", speedFactor = 0.35, offsetYFraction = 0.035),
            LayerAsset(
                "backgrounds/zone1/parallax/03_rampart_floor.png",
                speedFactor = 0.75,
                heightFraction = 0.65,
                offsetYFraction = 0.07,
            ),
        ),
    ),
    TOWERS_AND_WALLS(
        listOf(
            LayerAsset("backgrounds/zone2/parallax/00_sky.png", speedFactor = 0.03, mirrorTiles = true),
            LayerAsset("backgrounds/zone2/parallax/01_mountains.png", speedFactor = 0.10, mirrorTiles = true),
            LayerAsset("backgrounds/zone2/parallax/02_far_castle.png", speedFactor = 0.20, mirrorTiles = true),
            LayerAsset("backgrounds/zone2/parallax/03_castle_walls.png", speedFactor = 0.40),
            LayerAsset(
                "backgrounds/zone2/parallax/04_foreground_wall.png",
                speedFactor = 0.75,
                heightFraction = 0.65,
                offsetYFraction = 0.10,
            ),
        ),
    ),
    OUTER_FOREST(
        listOf(
            LayerAsset("backgrounds/zone3/parallax/00_sky.png", speedFactor = 0.03, mirrorTiles = true),
            LayerAsset("backgrounds/zone3/parallax/01_mountains.png", speedFactor = 0.12, mirrorTiles = true),
            LayerAsset("backgrounds/zone3/parallax/02_forest_mid.png", speedFactor = 0.40, mirrorTiles = true),
            LayerAsset("backgrounds/zone3/parallax/03_forest_front.png", speedFactor = 0.75, heightFraction = 0.65),
        ),
    ),
    ;

    suspend fun loadLayers(): List<ScrollingBackground.Layer> =
        layers.map { asset ->
            ScrollingBackground.Layer(
                bitmap = resourcesVfs[asset.path].readBitmap(),
                speedFactor = asset.speedFactor,
                mirrorTiles = asset.mirrorTiles,
                heightFraction = asset.heightFraction,
                offsetYFraction = asset.offsetYFraction,
            )
        }
}

private class LayerAsset(
    val path: String,
    val speedFactor: Double,
    val mirrorTiles: Boolean = false,
    val heightFraction: Double = 1.0,
    val offsetYFraction: Double = 0.0,
)
