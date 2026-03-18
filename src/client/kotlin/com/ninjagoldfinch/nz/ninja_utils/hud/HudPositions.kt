package com.ninjagoldfinch.nz.ninja_utils.hud

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.ninjagoldfinch.nz.ninja_utils.logging.ModLogger
import net.fabricmc.loader.api.FabricLoader
import java.nio.file.Files

/**
 * Persists HUD panel positions to a JSON file.
 * Two panels: "main" (combined HUD) and "debug_overlay".
 */
object HudPositions {
    private val logger = ModLogger.category("HudPositions")
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private val configFile = FabricLoader.getInstance().configDir.resolve("ninja_utils_hud.json")
    private val positions = mutableMapOf<String, ElementPosition>()

    data class ElementPosition(var x: Int = 5, var y: Int = 5)

    fun load() {
        try {
            if (Files.exists(configFile)) {
                val json = Files.readString(configFile)
                val type = object : TypeToken<Map<String, ElementPosition>>() {}.type
                val loaded: Map<String, ElementPosition> = gson.fromJson(json, type) ?: emptyMap()
                positions.clear()
                positions.putAll(loaded)
                logger.info("Loaded ${positions.size} HUD positions")
            }
        } catch (e: Exception) {
            logger.error("Failed to load HUD positions", e)
        }
    }

    fun save() {
        try {
            Files.writeString(configFile, gson.toJson(positions))
        } catch (e: Exception) {
            logger.error("Failed to save HUD positions", e)
        }
    }

    fun getPosition(id: String): ElementPosition {
        return positions.getOrPut(id) { ElementPosition() }
    }

    fun setPosition(id: String, x: Int, y: Int) {
        positions.getOrPut(id) { ElementPosition() }.also {
            it.x = x
            it.y = y
        }
    }

    /**
     * Assigns default positions for panels that don't have saved positions yet.
     */
    fun initDefaults(elements: List<HudElement>) {
        if (HudManager.MAIN_PANEL_ID !in positions) {
            positions[HudManager.MAIN_PANEL_ID] = ElementPosition(5, 5)
        }
        if ("debug_overlay" !in positions) {
            positions["debug_overlay"] = ElementPosition(500, 5)
        }
    }
}
