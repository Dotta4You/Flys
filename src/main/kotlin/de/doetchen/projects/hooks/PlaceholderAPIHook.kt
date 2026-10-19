/*
 * ==========================================
 * Fly's Plugin v1.5
 * Made by Dötchen with <3
 * https://github.com/Dotta4You/Flys
 * ==========================================
 */

package de.doetchen.projects.hooks

import de.doetchen.projects.Flys
import de.doetchen.projects.utils.DurationUtils
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.entity.Player
import kotlin.math.roundToInt

class PlaceholderAPIHook(private val plugin: Flys) : PlaceholderExpansion() {

    override fun getIdentifier(): String = "flys"

    override fun getAuthor(): String = "Doetchen"

    override fun getVersion(): String = plugin.description.version

    override fun persist(): Boolean = true

    override fun canRegister(): Boolean = true

    override fun onPlaceholderRequest(player: Player?, params: String): String? {
        if (player == null) return null

        val flightManager = plugin.flightManager
        val flying = flightManager.hasActiveFlight(player)
        val worldAllowed by lazy { flightManager.isFlightAllowedInWorld(player.world.name) }

        return when (params.lowercase()) {
            "flying" -> flying.toString()
            "flying_status" -> if (flying) "Enabled" else "Disabled"
            "flying_symbol" -> if (flying) "✔" else "✘"
            "speed" -> if (flying) flightManager.speedLevelOf(player).toString() else "0"
            "speed_percent" -> if (flying) "${(player.flySpeed * 100).roundToInt()}%" else "0%"
            "speed_max" -> flightManager.maxSpeedLevelFor(player).toString()
            "world_allowed" -> worldAllowed.toString()
            "world_status" -> if (worldAllowed) "Allowed" else "Disabled"
            "total_flying" -> flightManager.getFlyingPlayerCount().toString()
            "timed" -> plugin.timedFlightManager.hasTimedFlight(player).toString()
            "time_left" -> describeFlightTimeLeft(player, flying)
            "time_left_seconds" -> plugin.timedFlightManager.timedFlightSecondsLeft(player).toString()
            "in_combat" -> plugin.combatManager.isCombatLocked(player).toString()
            "combat_time_left" -> if (plugin.combatManager.isCombatLocked(player)) {
                plugin.combatManager.combatSecondsLeft(player).toString()
            } else {
                "0"
            }
            else -> null
        }
    }

    private fun describeFlightTimeLeft(player: Player, flying: Boolean): String = when {
        plugin.timedFlightManager.hasTimedFlight(player) ->
            DurationUtils.formatDuration(plugin.timedFlightManager.timedFlightSecondsLeft(player))
        flying -> "Unlimited"
        else -> "0s"
    }
}
