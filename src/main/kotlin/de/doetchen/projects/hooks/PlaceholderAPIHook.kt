/*
 * ==========================================
 * Fly's Plugin v1.4
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

        val flying = plugin.flightManager.hasFlightEnabled(player)
        val worldAllowed by lazy { plugin.flightManager.isFlightAllowedInWorld(player.world.name) }

        return when (params.lowercase()) {
            "flying" -> flying.toString()
            "flying_status" -> if (flying) "Enabled" else "Disabled"
            "flying_symbol" -> if (flying) "✔" else "✘"
            "speed" -> if (flying) (player.flySpeed * 10).toInt().toString() else "0"
            "speed_percent" -> if (flying) "${(player.flySpeed * 100).toInt()}%" else "0%"
            "world_allowed" -> worldAllowed.toString()
            "world_status" -> if (worldAllowed) "Allowed" else "Disabled"
            "total_flying" -> plugin.flightManager.getFlyingPlayerCount().toString()
            else -> null
        }
    }
}
