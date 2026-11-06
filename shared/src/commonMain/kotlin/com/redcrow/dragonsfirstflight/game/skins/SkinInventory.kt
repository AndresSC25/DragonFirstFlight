package com.redcrow.dragonsfirstflight.game.skins

import korlibs.korge.service.storage.IStorage

/** Writes go to storage synchronously, so call these from menu actions, never per frame. */
class SkinInventory(
    private val storage: IStorage,
) {
    var unlocked: Set<DragonSkin> = loadUnlocked()
        private set

    var equipped: DragonSkin = loadEquipped()
        private set

    fun isUnlocked(skin: DragonSkin): Boolean = skin in unlocked

    fun unlock(skin: DragonSkin) {
        if (isUnlocked(skin)) return

        unlocked = unlocked + skin
        storage[UNLOCKED_KEY] = unlocked.joinToString(separator = ",") { it.name }
    }

    fun equip(skin: DragonSkin) {
        if (!isUnlocked(skin)) return
        if (equipped != skin) {
            equipped = skin
            storage[EQUIPPED_KEY] = skin.name
        }
    }

    private fun loadUnlocked(): Set<DragonSkin> =
        storage
            .getOrNull(UNLOCKED_KEY)
            ?.split(',')
            ?.mapNotNull(::skinNamed)
            .orEmpty()
            .toSet() + DragonSkin.DEFAULT

    private fun loadEquipped(): DragonSkin {
        val storedSkin =
            storage
                .getOrNull(EQUIPPED_KEY)
                ?.let(::skinNamed)
        return storedSkin?.takeIf(::isUnlocked) ?: DragonSkin.DEFAULT
    }

    private fun skinNamed(name: String?): DragonSkin? = DragonSkin.entries.firstOrNull { it.name == name }

    private companion object {
        const val UNLOCKED_KEY = "skins.unlocked"
        const val EQUIPPED_KEY = "skins.equipped"
    }
}
