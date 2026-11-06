package com.redcrow.dragonsfirstflight.game.skins

import korlibs.korge.service.storage.InmemoryStorage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SkinInventoryTest {
    @Test
    fun emptyStorageUnlocksAndEquipsDefaultOnly() {
        val inventory = SkinInventory(InmemoryStorage())

        assertEquals(setOf(DragonSkin.DEFAULT), inventory.unlocked)
        assertEquals(DragonSkin.DEFAULT, inventory.equipped)
        assertFalse(inventory.isUnlocked(DragonSkin.LDA))
    }

    @Test
    fun equippingLockedSkinKeepsCurrentSkin() {
        val inventory = SkinInventory(InmemoryStorage())

        inventory.equip(DragonSkin.LDA)
        assertEquals(DragonSkin.DEFAULT, inventory.equipped)
    }

    @Test
    fun unlockAndEquipPersist() {
        val storage = InmemoryStorage()
        val inventory = SkinInventory(storage)

        inventory.unlock(DragonSkin.LDA)
        inventory.equip(DragonSkin.LDA)
        assertEquals(DragonSkin.LDA, inventory.equipped)

        val reloaded = SkinInventory(storage)
        assertTrue(reloaded.isUnlocked(DragonSkin.LDA))
        assertEquals(DragonSkin.LDA, reloaded.equipped)
    }

    @Test
    fun corruptStorageLoadsDefaultOnlyAndEquippedDefault() {
        val storage = InmemoryStorage()
        storage["skins.unlocked"] = "UNKNOWN,,NO_SKIN"
        storage["skins.equipped"] = "LDA"

        val inventory = SkinInventory(storage)

        assertEquals(setOf(DragonSkin.DEFAULT), inventory.unlocked)
        assertEquals(DragonSkin.DEFAULT, inventory.equipped)
    }
}
