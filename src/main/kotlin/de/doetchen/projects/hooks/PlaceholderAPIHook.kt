/*
 * ==========================================
 * Fly's Plugin v1.2
 * Made by Dötchen with <3
 * https://github.com/Dotta4You/Flys
 * ==========================================
 */

package de.doetchen.projects.hooks

import de.doetchen.projects.Flys
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.entity.Player

class PlaceholderAPIHook(private val plugin: Flys) : PlaceholderExpansion() {

    override fun getIdentifier(): String = "flys"

    override fun getAuthor(): String = "Doetchen"

    override fun getVersion(): String = plugin.description.version

    override fun persist(): Boolean = true

    override fun canRegister(): Boolean = true

    override fun onPlaceholderRequest(player: Player?, params: String): String? {
        if (player == null) return null

        return when (params.lowercase()) {
            "flying" -> {
                if (plugin.flightManager.hasFlightEnabled(player)) "true" else "false"
            }
            "flying_status" -> {
                if (plugin.flightManager.hasFlightEnabled(player)) "Enabled" else "Disabled"
            }
            "flying_symbol" -> {
                if (plugin.flightManager.hasFlightEnabled(player)) "✔" else "✘"
            }
            "speed" -> {
                if (plugin.flightManager.hasFlightEnabled(player)) {
                    ((player.flySpeed * 10).toInt()).toString()
                } else {
                    "0"
                }
            }
            "speed_percent" -> {
                if (plugin.flightManager.hasFlightEnabled(player)) {
                    "${(player.flySpeed * 100).toInt()}%"
                } else {
                    "0%"
                }
            }
            "world_allowed" -> {
                if (plugin.flightManager.isFlightAllowedInWorldPublic(player.world.name)) "true" else "false"
            }
            "world_status" -> {
                if (plugin.flightManager.isFlightAllowedInWorldPublic(player.world.name)) "Allowed" else "Disabled"
            }
            "total_flying" -> {
                plugin.flightManager.getFlyingPlayers().size.toString()
            }
            else -> null
        }
    }
}

