package com.redcrow.dragonsfirstflight

import androidx.compose.ui.unit.dp

internal class GameController(
    private val estado: GameState
) {
    fun aletear() {
        if (!estado.partidaActiva) return

        // Cada pulsación establece el impulso, sin acumularlo.
        estado.velocidadVertical.floatValue =
            GameConfig.VELOCIDAD_IMPULSO
    }

    fun actualizarVuelo(segundosTranscurridos: Float) {
        if (!estado.partidaActiva) return

        if (
            !segundosTranscurridos.isFinite() ||
            segundosTranscurridos <= 0f
        ) {
            return
        }

        val delta = segundosTranscurridos.coerceAtMost(
            GameConfig.MAX_DELTA_SEGUNDOS
        )

        val velocidadActual =
            estado.velocidadVertical.floatValue

        val velocidadNueva =
            (
                    velocidadActual +
                            GameConfig.GRAVEDAD * delta
                    ).coerceAtMost(
                    GameConfig.VELOCIDAD_MAXIMA_CAIDA
                )

        // Velocidad media para calcular el desplazamiento.
        val desplazamiento =
            ((velocidadActual + velocidadNueva) / 2f) * delta

        val posicionNueva =
            estado.posicionDragon.value + desplazamiento.dp

        when {
            posicionNueva <= 0.dp -> {
                estado.posicionDragon.value = 0.dp
                estado.velocidadVertical.floatValue = 0f
            }

            posicionNueva >= GameConfig.DRAGON_Y_MAXIMA -> {
                estado.posicionDragon.value =
                    GameConfig.DRAGON_Y_MAXIMA

                estado.velocidadVertical.floatValue = 0f
            }

            else -> {
                estado.posicionDragon.value = posicionNueva
                estado.velocidadVertical.floatValue =
                    velocidadNueva
            }
        }
    }
}
