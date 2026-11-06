package com.redcrow.dragonsfirstflight.ui.components

import korlibs.image.bitmap.Bitmap
import korlibs.image.bitmap.sliceWithSize
import korlibs.korge.view.Container
import korlibs.korge.view.container
import korlibs.korge.view.image

private const val PANEL_INSET = 21
internal const val MEDIEVAL_PANEL_BORDER = PANEL_INSET * 3.0

internal fun Container.medievalPanel(
    bitmap: Bitmap,
    x: Double,
    y: Double,
    width: Double,
    height: Double,
): Container =
    container {
        this.x = x
        this.y = y
        val sourceX = intArrayOf(0, PANEL_INSET, bitmap.width - PANEL_INSET, bitmap.width)
        val sourceY = intArrayOf(0, PANEL_INSET, bitmap.height - PANEL_INSET, bitmap.height)
        val targetX = doubleArrayOf(0.0, MEDIEVAL_PANEL_BORDER, width - MEDIEVAL_PANEL_BORDER, width)
        val targetY = doubleArrayOf(0.0, MEDIEVAL_PANEL_BORDER, height - MEDIEVAL_PANEL_BORDER, height)
        for (row in 0..2) {
            for (column in 0..2) {
                val sliceWidth = sourceX[column + 1] - sourceX[column]
                val sliceHeight = sourceY[row + 1] - sourceY[row]
                image(
                    texture =
                        bitmap.sliceWithSize(
                            x = sourceX[column],
                            y = sourceY[row],
                            width = sliceWidth,
                            height = sliceHeight,
                        ),
                ) {
                    this.x = targetX[column]
                    this.y = targetY[row]
                    scaleX = (targetX[column + 1] - targetX[column]) / sliceWidth
                    scaleY = (targetY[row + 1] - targetY[row]) / sliceHeight
                    smoothing = false
                }
            }
        }
    }
