package com.redcrow.dragonsfirstflight.ui.screens

import com.redcrow.dragonsfirstflight.game.config.DragonGameConfig
import com.redcrow.dragonsfirstflight.game.controller.GameController
import com.redcrow.dragonsfirstflight.game.engine.GameEngineConfig
import com.redcrow.dragonsfirstflight.game.engine.GameSnapshot
import com.redcrow.dragonsfirstflight.game.engine.GameStatus
import com.redcrow.dragonsfirstflight.game.engine.GameZone
import com.redcrow.dragonsfirstflight.game.skins.SkinInventory
import com.redcrow.dragonsfirstflight.game.skins.rollFlightSpritePath
import com.redcrow.dragonsfirstflight.ui.components.GameplayHud
import com.redcrow.dragonsfirstflight.ui.components.SessionOverlay
import com.redcrow.dragonsfirstflight.ui.gameplay.ANIMATED_OBSTACLE_CONFIGS
import com.redcrow.dragonsfirstflight.ui.gameplay.CATAPULT_PROJECTILE_PATH
import com.redcrow.dragonsfirstflight.ui.gameplay.CATAPULT_SPRITE_PATH
import com.redcrow.dragonsfirstflight.ui.gameplay.CATAPULT_WARNING_PATH
import com.redcrow.dragonsfirstflight.ui.gameplay.CatapultView
import com.redcrow.dragonsfirstflight.ui.gameplay.CoinView
import com.redcrow.dragonsfirstflight.ui.gameplay.DragonView
import com.redcrow.dragonsfirstflight.ui.gameplay.FLAMING_ARROW_SPRITE_PATH
import com.redcrow.dragonsfirstflight.ui.gameplay.FLYING_BAT_SPRITE_PATH
import com.redcrow.dragonsfirstflight.ui.gameplay.FLYING_CROWS_SPRITE_PATH
import com.redcrow.dragonsfirstflight.ui.gameplay.FLYING_SWORD_SPRITE_PATH
import com.redcrow.dragonsfirstflight.ui.gameplay.FOREST_MAGE_SPRITE_PATH
import com.redcrow.dragonsfirstflight.ui.gameplay.HitSparkView
import com.redcrow.dragonsfirstflight.ui.gameplay.KNIGHT_SWORD_SPRITE_PATH
import com.redcrow.dragonsfirstflight.ui.gameplay.ObstacleView
import com.redcrow.dragonsfirstflight.ui.gameplay.ProjectileView
import com.redcrow.dragonsfirstflight.ui.gameplay.ScrollingBackground
import com.redcrow.dragonsfirstflight.ui.gameplay.ZONE_THREE_FAIRY_SPRITE_PATH
import com.redcrow.dragonsfirstflight.ui.gameplay.ZONE_THREE_FLYING_FOREST_MONSTER_SPRITE_PATH
import com.redcrow.dragonsfirstflight.ui.gameplay.ZONE_THREE_GIANT_HORNET_SPRITE_PATH
import com.redcrow.dragonsfirstflight.ui.gameplay.ZONE_THREE_WIND_SPIRIT_SPRITE_PATH
import com.redcrow.dragonsfirstflight.ui.gameplay.ZONE_TWO_FLYING_CURSED_BOOK_SPRITE_PATH
import com.redcrow.dragonsfirstflight.ui.gameplay.ZONE_TWO_FLYING_GARGOYLE_SPRITE_PATH
import com.redcrow.dragonsfirstflight.ui.gameplay.ZONE_TWO_FLYING_SKULL_WISP_SPRITE_PATH
import com.redcrow.dragonsfirstflight.ui.gameplay.ZONE_TWO_GHOST_KNIGHT_SPRITE_PATH
import com.redcrow.dragonsfirstflight.ui.gameplay.ZONE_TWO_WINGED_IMP_SPRITE_PATH
import com.redcrow.dragonsfirstflight.ui.gameplay.ZoneBackground
import com.redcrow.dragonsfirstflight.ui.gameplay.ZoneTransitionOverlay
import com.redcrow.dragonsfirstflight.ui.layout.createResponsiveShade
import korlibs.image.color.Colors
import korlibs.image.format.readBitmap
import korlibs.io.file.std.resourcesVfs
import korlibs.korge.input.onClick
import korlibs.korge.view.Container
import korlibs.korge.view.SpriteAnimation
import korlibs.korge.view.Views
import korlibs.korge.view.addUpdater
import korlibs.korge.view.clipContainer
import korlibs.korge.view.container
import korlibs.math.geom.Size
import kotlin.random.Random
import kotlin.time.DurationUnit

private const val SESSION_PANEL_PATH: String =
    "ui/panels/panel_medieval.png"

private const val GAME_BUTTON_PATH: String =
    "ui/buttons/button_normal.png"

private const val SCORE_PLATE_PATH: String =
    "ui/buttons/score_plate.png"

private const val PAUSE_BUTTON_PATH: String =
    "ui/buttons/button_pause.png"

private const val LIFE_FULL_PATH: String =
    "sprites/dragon/lives/dragon_life_full.png"

private const val LIFE_EMPTY_PATH: String =
    "sprites/dragon/lives/dragon_life_empty.png"

private const val HIT_SPARK_PATH: String =
    "effects/impact/hit_spark.png"

private const val COIN_ATLAS_PATH: String =
    "sprites/coins/coin_spin.png"

private const val COIN_ICON_PATH: String =
    "sprites/coins/coin_icon.png"

private val OBSTACLE_PATHS =
    listOf(
        FLYING_BAT_SPRITE_PATH,
        FLYING_CROWS_SPRITE_PATH,
        FLYING_SWORD_SPRITE_PATH,
        FLAMING_ARROW_SPRITE_PATH,
        FOREST_MAGE_SPRITE_PATH,
        KNIGHT_SWORD_SPRITE_PATH,
        ZONE_TWO_FLYING_SKULL_WISP_SPRITE_PATH,
        ZONE_TWO_FLYING_CURSED_BOOK_SPRITE_PATH,
        ZONE_TWO_FLYING_GARGOYLE_SPRITE_PATH,
        ZONE_TWO_GHOST_KNIGHT_SPRITE_PATH,
        ZONE_TWO_WINGED_IMP_SPRITE_PATH,
        ZONE_THREE_FAIRY_SPRITE_PATH,
        ZONE_THREE_FLYING_FOREST_MONSTER_SPRITE_PATH,
        ZONE_THREE_GIANT_HORNET_SPRITE_PATH,
        ZONE_THREE_WIND_SPIRIT_SPRITE_PATH,
    )

internal suspend fun Container.createGameplayScreen(
    views: Views,
    controller: GameController,
    skinInventory: SkinInventory,
    skinAnimations: Map<String, SpriteAnimation>,
    onBackToMenu: () -> Unit,
): Container {
    val backgroundLayers =
        ZoneBackground.values().associateWith { it.loadLayers() }

    val sessionPanelBitmap =
        resourcesVfs[SESSION_PANEL_PATH].readBitmap()

    val gameButtonBitmap =
        resourcesVfs[GAME_BUTTON_PATH].readBitmap()

    val scorePlateBitmap =
        resourcesVfs[SCORE_PLATE_PATH].readBitmap()

    val pauseButtonBitmap =
        resourcesVfs[PAUSE_BUTTON_PATH].readBitmap()

    val lifeFullBitmap =
        resourcesVfs[LIFE_FULL_PATH].readBitmap()

    val lifeEmptyBitmap =
        resourcesVfs[LIFE_EMPTY_PATH].readBitmap()

    val hitSparkBitmap =
        resourcesVfs[HIT_SPARK_PATH].readBitmap()

    val coinAtlasBitmap =
        resourcesVfs[COIN_ATLAS_PATH].readBitmap()

    val coinIconBitmap =
        resourcesVfs[COIN_ICON_PATH].readBitmap()

    val catapultBitmap =
        resourcesVfs[CATAPULT_SPRITE_PATH].readBitmap()
    val catapultWarningBitmap = resourcesVfs[CATAPULT_WARNING_PATH].readBitmap()
    val catapultProjectileBitmap = resourcesVfs[CATAPULT_PROJECTILE_PATH].readBitmap()

    val obstacleBitmaps =
        OBSTACLE_PATHS.associateWith { path ->
            resourcesVfs[path].readBitmap()
        }

    return container {
        val scrollingBackgrounds =
            ZoneBackground.values().associateWith { zone ->
                ScrollingBackground.create(
                    container = this,
                    layers = backgroundLayers.getValue(zone),
                    views = views,
                )
            }
        scrollingBackgrounds.values.forEach { it.visible = false }
        scrollingBackgrounds.getValue(ZoneBackground.FORTRESS_COURTYARD).visible = true

        var displayedBackground =
            ZoneBackground.FORTRESS_COURTYARD

        val obstacleLayer =
            container()

        val obstacleView =
            ObstacleView.create(
                container = obstacleLayer,
                obstacleBitmaps = obstacleBitmaps,
                views = views,
                animatedObstacleConfigs = ANIMATED_OBSTACLE_CONFIGS,
            )

        val coinLayer =
            container()

        val coinView =
            CoinView.create(
                container = coinLayer,
                atlas = coinAtlasBitmap,
                views = views,
            )

        var appliedRunNumber = controller.runNumber
        val catapultView =
            CatapultView.create(
                container = this,
                spriteMap = catapultBitmap,
                warningMap = catapultWarningBitmap,
            )
        val projectileView = ProjectileView.create(container = container(), spriteMap = catapultProjectileBitmap)
        lateinit var dragonView: DragonView
        lateinit var hitSparkView: HitSparkView

        clipContainer(
            size =
                Size(
                    width = DragonGameConfig.GAME_AREA_WIDTH,
                    height = GameEngineConfig.GAME_AREA_HEIGHT,
                ),
        ) {
            x = DragonGameConfig.GAME_AREA_X
            y = DragonGameConfig.GAME_AREA_Y

            dragonView =
                DragonView.create(
                    container = this,
                    animation = skinAnimations.getValue(skinInventory.equipped.spritePath),
                )

            hitSparkView =
                HitSparkView.create(
                    container = this,
                    spriteMap = hitSparkBitmap,
                )
        }

        val gameplayInputSurface =
            createResponsiveShade(
                views = views,
                color = Colors.TRANSPARENT,
                shadeAlpha = 0.0,
            )

        gameplayInputSurface.onClick {
            if (controller.isGameRunning) {
                controller.handleGameAreaInput()
            }
        }

        val gameplayHud =
            GameplayHud.create(
                container = this,
                views = views,
                lifeFullBitmap = lifeFullBitmap,
                lifeEmptyBitmap = lifeEmptyBitmap,
                scorePlateBitmap = scorePlateBitmap,
                coinIcon = coinIconBitmap,
                pauseButtonBitmap = pauseButtonBitmap,
                onPause = controller::pauseGame,
            )

        val sessionOverlay =
            SessionOverlay.create(
                container = this,
                views = views,
                panelBitmap = sessionPanelBitmap,
                buttonBitmap = gameButtonBitmap,
                onResume = controller::resumeGame,
                onRestart = controller::startGame,
                onBackToMenu = onBackToMenu,
            )

        val zoneTransitionOverlay =
            ZoneTransitionOverlay.create(
                container = this,
                views = views,
                panelBitmap = sessionPanelBitmap,
            )

        fun updateView(
            snapshot: GameSnapshot,
            elapsedSeconds: Double,
        ) {
            catapultView.update(snapshot)
            projectileView.update(snapshot)
            obstacleView.update(
                snapshot = snapshot,
                isPaused = controller.isPaused,
            )

            coinView.update(
                snapshot = snapshot,
                isPaused = controller.isPaused,
                elapsedSeconds = elapsedSeconds,
            )

            gameplayHud.update(
                snapshot = snapshot,
                isPaused = controller.isPaused,
            )

            sessionOverlay.update(
                snapshot = snapshot,
                isPaused = controller.isPaused,
                coinBalance = controller.coinBalance,
            )
            zoneTransitionOverlay.update(snapshot)
        }

        addUpdater { delta ->
            if (controller.runNumber != appliedRunNumber) {
                appliedRunNumber = controller.runNumber
                val path = rollFlightSpritePath(skinInventory.equipped, Random.Default)
                dragonView.setAnimation(skinAnimations.getValue(path))
            }

            val viewportLeft =
                views.actualVirtualLeft.toDouble()

            val viewportRight =
                viewportLeft +
                    views.actualVirtualWidth.toDouble()

            controller.resizeGameArea(
                leftBoundary = viewportLeft,
                rightBoundary = viewportRight,
            )

            val elapsedSeconds =
                delta.toDouble(DurationUnit.SECONDS)

            controller.update(elapsedSeconds)

            val snapshot =
                controller.snapshot()

            val targetBackground =
                when (snapshot.zone) {
                    GameZone.ZONE_ONE -> ZoneBackground.FORTRESS_COURTYARD
                    GameZone.ZONE_TWO -> ZoneBackground.TOWERS_AND_WALLS
                    GameZone.ZONE_THREE, GameZone.BOSS -> ZoneBackground.OUTER_FOREST
                }

            if (
                !snapshot.isZoneTransitioning ||
                snapshot.zoneTransitionProgress >=
                ZoneTransitionOverlay.TRANSITION_MIDPOINT
            ) {
                displayedBackground = targetBackground
            }

            scrollingBackgrounds.forEach { (zone, background) ->
                background.visible = zone == displayedBackground
                background.update(
                    if (
                        zone == displayedBackground &&
                        snapshot.status == GameStatus.RUNNING &&
                        !snapshot.isZoneTransitioning &&
                        !controller.isPaused
                    ) {
                        elapsedSeconds
                    } else {
                        0.0
                    },
                )
            }

            dragonView.update(
                snapshot = snapshot,
                isPaused = controller.isPaused,
            )

            hitSparkView.update(
                snapshot = snapshot,
                isPaused = controller.isPaused,
            )

            updateView(snapshot, elapsedSeconds)
        }

        val initialViewportLeft =
            views.actualVirtualLeft.toDouble()

        val initialViewportRight =
            initialViewportLeft +
                views.actualVirtualWidth.toDouble()

        controller.resizeGameArea(
            leftBoundary = initialViewportLeft,
            rightBoundary = initialViewportRight,
        )

        val initialSnapshot =
            controller.snapshot()

        scrollingBackgrounds.values.forEach { it.update(0.0) }

        dragonView.update(
            snapshot = initialSnapshot,
            isPaused = controller.isPaused,
        )

        hitSparkView.update(
            snapshot = initialSnapshot,
            isPaused = controller.isPaused,
        )

        updateView(initialSnapshot, 0.0)
    }
}
