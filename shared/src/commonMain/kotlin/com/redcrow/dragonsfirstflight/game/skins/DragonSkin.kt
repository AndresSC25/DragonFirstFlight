package com.redcrow.dragonsfirstflight.game.skins

enum class DragonSkin(
    val displayName: String,
    val spritePath: String,
) {
    BRASS(displayName = "Brass", spritePath = "sprites/dragon/YoungBrassDragon.png"),
    LDA(displayName = "LDA", spritePath = "sprites/dragon/skins/dragon_lda.png"),
    SAPRISSA(displayName = "Saprissa", spritePath = "sprites/dragon/skins/dragon_saprissa.png"),
    HEREDIA(displayName = "Heredia", spritePath = "sprites/dragon/skins/dragon_heredia.png"),
    CARTAGO(displayName = "Cartago", spritePath = "sprites/dragon/skins/dragon_cartago.png"),
    ;

    companion object {
        val DEFAULT: DragonSkin = BRASS
    }
}
