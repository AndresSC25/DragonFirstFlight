package com.redcrow.dragonsfirstflight.ui.components

import com.redcrow.dragonsfirstflight.game.engine.GameDifficulty
import korlibs.image.bitmap.Bitmap
import korlibs.image.color.Colors
import korlibs.korge.input.onClick
import korlibs.korge.view.Container
import korlibs.korge.view.Text
import korlibs.korge.view.image
import korlibs.korge.view.solidRect
import korlibs.korge.view.text

private const val SELECTOR_TITLE_SIZE = 17.0
private const val SELECTOR_VALUE_SIZE = 20.0
private const val SELECTOR_DESCRIPTION_SIZE = 14.0
private const val SELECTOR_ARROW_SIZE = 24.0
private const val SELECTOR_ARROW_AREA_WIDTH = 48.0
private const val SELECTOR_TITLE_GAP = 24.0
private const val SELECTOR_DESCRIPTION_GAP = 6.0

internal fun Container.createCompactDifficultySelector(
    x: Double,
    y: Double,
    width: Double,
    height: Double,
    texture: Bitmap,
    initialDifficulty: GameDifficulty = GameDifficulty.EASY,
    onInteraction: () -> Unit = {},
    onSelectionChanged: (GameDifficulty) -> Unit,
) {
    require(width > SELECTOR_ARROW_AREA_WIDTH * 2.0) {
        "Difficulty selector width must accommodate both arrow controls."
    }
    require(height > 0.0) {
        "Difficulty selector height must be greater than zero."
    }

    var selectedDifficulty = initialDifficulty

    val title =
        text(
            text = "DIFFICULTY",
            textSize = SELECTOR_TITLE_SIZE,
            color = Colors["#F8D66D"],
        )
    title.x = x + (width - title.width) / 2.0
    title.y = y

    val selectorY = y + SELECTOR_TITLE_GAP

    val selectorPlate =
        image(texture = texture) {
            this.x = x
            this.y = selectorY
            scaleX = width / texture.width.toDouble()
            scaleY = height / texture.height.toDouble()
            smoothing = false
        }

    val leftHitArea =
        solidRect(
            width = SELECTOR_ARROW_AREA_WIDTH,
            height = height,
            color = Colors.WHITE,
        ) {
            this.x = x
            this.y = selectorY
            alpha = 0.001
        }

    val rightHitArea =
        solidRect(
            width = SELECTOR_ARROW_AREA_WIDTH,
            height = height,
            color = Colors.WHITE,
        ) {
            this.x = x + width - SELECTOR_ARROW_AREA_WIDTH
            this.y = selectorY
            alpha = 0.001
        }

    val leftArrow =
        text(
            text = "<",
            textSize = SELECTOR_ARROW_SIZE,
            color = Colors.WHITE,
        )
    val rightArrow =
        text(
            text = ">",
            textSize = SELECTOR_ARROW_SIZE,
            color = Colors.WHITE,
        )

    fun centerVertically(view: Text) {
        view.y = selectorY + (height - view.height) / 2.0 + 2.0
    }

    leftArrow.x = x + (SELECTOR_ARROW_AREA_WIDTH - leftArrow.width) / 2.0
    rightArrow.x =
        x +
        width -
        SELECTOR_ARROW_AREA_WIDTH +
        (SELECTOR_ARROW_AREA_WIDTH - rightArrow.width) / 2.0
    centerVertically(leftArrow)
    centerVertically(rightArrow)

    val valueText =
        text(
            text = "",
            textSize = SELECTOR_VALUE_SIZE,
            color = Colors.WHITE,
        )

    val descriptionText =
        text(
            text = "",
            textSize = SELECTOR_DESCRIPTION_SIZE,
            color = Colors["#F8D66D"],
        )

    fun difficultyDescription(difficulty: GameDifficulty): String =
        when (difficulty) {
            GameDifficulty.EASY -> "Classic flight"
            GameDifficulty.MEDIUM -> "Faster obstacles"
            GameDifficulty.HARD -> "Rush hazards"
        }

    fun renderSelection() {
        valueText.text = selectedDifficulty.displayName.uppercase()
        valueText.x = x + (width - valueText.width) / 2.0
        valueText.y = selectorY + (height - valueText.height) / 2.0 + 2.0

        descriptionText.text = difficultyDescription(selectedDifficulty)
        descriptionText.x = x + (width - descriptionText.width) / 2.0
        descriptionText.y = selectorY + height + SELECTOR_DESCRIPTION_GAP

        val accentColor =
            when (selectedDifficulty) {
                GameDifficulty.EASY -> Colors["#9ED98A"]
                GameDifficulty.MEDIUM -> Colors["#F8D66D"]
                GameDifficulty.HARD -> Colors["#FF8A72"]
            }
        valueText.color = accentColor
        descriptionText.color = accentColor
    }

    fun moveSelection(step: Int) {
        onInteraction()
        val difficulties = GameDifficulty.entries
        val currentIndex = difficulties.indexOf(selectedDifficulty)
        val nextIndex =
            (currentIndex + step + difficulties.size) % difficulties.size
        selectedDifficulty = difficulties[nextIndex]
        renderSelection()
        onSelectionChanged(selectedDifficulty)
    }

    leftHitArea.onClick { moveSelection(-1) }
    leftArrow.onClick { moveSelection(-1) }
    rightHitArea.onClick { moveSelection(1) }
    rightArrow.onClick { moveSelection(1) }

    selectorPlate.onClick {
        moveSelection(1)
    }

    renderSelection()
    onSelectionChanged(selectedDifficulty)
}
