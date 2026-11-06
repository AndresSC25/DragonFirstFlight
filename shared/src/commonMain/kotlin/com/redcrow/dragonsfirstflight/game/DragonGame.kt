package com.redcrow.dragonsfirstflight.game

import com.redcrow.dragonsfirstflight.game.coins.CoinWallet
import com.redcrow.dragonsfirstflight.game.config.DragonGameConfig
import com.redcrow.dragonsfirstflight.game.controller.GameController
import com.redcrow.dragonsfirstflight.ui.screens.createGameplayScreen
import com.redcrow.dragonsfirstflight.ui.screens.createMainMenuScreen
import korlibs.image.color.Colors
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

    solidRect(
        width = DragonGameConfig.VIRTUAL_SIZE.width,
        height = DragonGameConfig.VIRTUAL_SIZE.height,
        color = Colors["#101827"],
    )

    lateinit var menuContainer: Container
    var gameContainer: Container? = null

    fun updateScreenVisibility() {
        menuContainer.visible = controller.isMenuVisible
        gameContainer?.visible = controller.isGameVisible
    }

    menuContainer =
        createMainMenuScreen(
            views = views,
            onPlay = {
                controller.startGame()
                updateScreenVisibility()
            },
        )

    gameContainer =
        createGameplayScreen(
            views = views,
            controller = controller,
            onBackToMenu = {
                controller.returnToMenu()
                updateScreenVisibility()
            },
        )

    updateScreenVisibility()
}
