package com.redcrow.dragonsfirstflight.game

import com.redcrow.dragonsfirstflight.game.coins.CoinWallet
import com.redcrow.dragonsfirstflight.game.config.DragonGameConfig
import com.redcrow.dragonsfirstflight.game.controller.GameController
import com.redcrow.dragonsfirstflight.game.skins.DragonSkin
import com.redcrow.dragonsfirstflight.game.skins.NyanSkin
import com.redcrow.dragonsfirstflight.game.skins.SkinInventory
import com.redcrow.dragonsfirstflight.ui.gameplay.createDragonFlightAnimation
import com.redcrow.dragonsfirstflight.ui.screens.createGameplayScreen
import com.redcrow.dragonsfirstflight.ui.screens.createInventoryScreen
import com.redcrow.dragonsfirstflight.ui.screens.createMainMenuScreen
import korlibs.image.color.Colors
import korlibs.image.format.readBitmap
import korlibs.io.file.std.resourcesVfs
import korlibs.korge.service.storage.storage
import korlibs.korge.view.Container
import korlibs.korge.view.Views
import korlibs.korge.view.solidRect

suspend fun Container.createDragonGame(views: Views) {
    val controller =
        GameController(
            gameAreaWidth = DragonGameConfig.GAME_AREA_WIDTH,
            coinWallet = CoinWallet(views.storage),
        )

    val skinInventory = SkinInventory(views.storage)
    val skinAnimations =
        (DragonSkin.entries.map { it.spritePath } + NyanSkin.entries.map { it.spritePath })
            .distinct()
            .associateWith { path ->
                createDragonFlightAnimation(resourcesVfs[path].readBitmap())
            }

    solidRect(
        width = DragonGameConfig.VIRTUAL_SIZE.width,
        height = DragonGameConfig.VIRTUAL_SIZE.height,
        color = Colors["#101827"],
    )

    lateinit var menuContainer: Container
    var gameContainer: Container? = null
    var inventoryContainer: Container? = null

    fun updateScreenVisibility() {
        menuContainer.visible = controller.isMenuVisible
        gameContainer?.visible = controller.isGameVisible
        inventoryContainer?.visible = controller.isInventoryVisible
    }

    menuContainer =
        createMainMenuScreen(
            views = views,
            skinInventory = skinInventory,
            skinAnimations = skinAnimations,
            onPlay = {
                controller.startGame()
                updateScreenVisibility()
            },
            onInventory = {
                controller.openInventory()
                updateScreenVisibility()
            },
        )

    inventoryContainer =
        createInventoryScreen(
            views = views,
            skinInventory = skinInventory,
            skinAnimations = skinAnimations,
            onBack = {
                controller.returnToMenu()
                updateScreenVisibility()
            },
        )

    gameContainer =
        createGameplayScreen(
            views = views,
            controller = controller,
            skinInventory = skinInventory,
            skinAnimations = skinAnimations,
            onBackToMenu = {
                controller.returnToMenu()
                updateScreenVisibility()
            },
        )

    updateScreenVisibility()
}
