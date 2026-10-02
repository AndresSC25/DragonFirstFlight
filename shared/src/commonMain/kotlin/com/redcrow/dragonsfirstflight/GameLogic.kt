package com.redcrow.dragonsfirstflight

import androidx.compose.ui.unit.Dp

internal const val PUNTOS_PARA_GANAR = 5

internal fun hayColision(
    dragonX: Dp,
    dragonY: Dp,
    dragonTamano: Dp,
    obstaculoX: Dp,
    obstaculoY: Dp,
    obstaculoTamano: Dp
): Boolean {
    return dragonX < obstaculoX + obstaculoTamano &&
            dragonX + dragonTamano > obstaculoX &&
            dragonY < obstaculoY + obstaculoTamano &&
            dragonY + dragonTamano > obstaculoY
}