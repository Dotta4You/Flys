/*
 * ==========================================
 * Fly's Plugin v1.0
 * Made by Dötchen with <3
 * https://github.com/Dotta4You/Flys
 * ==========================================
 */
package de.doetchen.projects.managers

import de.doetchen.projects.Flys
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerGameModeChangeEvent
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.GameMode
import java.util.*

class FlightManager(private val plugin: Flys) : Listener {

    private val flyingPlayers = mutableSetOf<UUID>()

    fun enableFlight(player: Player): Boolean {
        if (!isFlightAllowedInWorld(player.world.name)) {
            plugin.messageUtils.sendMessage(player, "errors.world-not-allowed")
            return false
        }

        player.allowFlight = true
        player.isFlying = true
        flyingPlayers.add(player.uniqueId)

        val speed = plugin.configManager.getDouble("general.flight-speed.default-speed", 0.1).toFloat()
        player.flySpeed = speed.coerceIn(0.0f, 1.0f)

        return true
    }

    fun disableFlight(player: Player) {
        player.allowFlight = false
        player.isFlying = false
        flyingPlayers.remove(player.uniqueId)
    }

    fun hasFlightEnabled(player: Player): Boolean {
        return flyingPlayers.contains(player.uniqueId)
    }

    fun toggleFlight(player: Player): Boolean {
        return if (hasFlightEnabled(player)) {
            disableFlight(player)
            false
        } else {
            enableFlight(player)
        }
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        flyingPlayers.remove(event.player.uniqueId)
    }

    @EventHandler
    fun onGameModeChange(event: PlayerGameModeChangeEvent) {
        val player = event.player

        when (event.newGameMode) {
            GameMode.CREATIVE, GameMode.SPECTATOR -> {
                flyingPlayers.remove(player.uniqueId)
            }
            GameMode.SURVIVAL, GameMode.ADVENTURE -> {
                if (hasFlightEnabled(player)) {
                    plugin.server.scheduler.runTaskLater(plugin, Runnable {
                        if (player.isOnline && hasFlightEnabled(player)) {
                            player.allowFlight = true
                        }
                    }, 1L)
                }
            }
        }
    }

    @EventHandler
    fun onWorldChange(event: PlayerChangedWorldEvent) {
        val player = event.player

        if (hasFlightEnabled(player)) {
            if (!isFlightAllowedInWorld(player.world.name)) {
                disableFlight(player)
                plugin.messageUtils.sendMessage(player, "errors.world-not-allowed")
            } else {
                plugin.server.scheduler.runTaskLater(plugin, Runnable {
                    if (player.isOnline && hasFlightEnabled(player)) {
                        player.allowFlight = true
                    }
                }, 1L)
            }
        }
    }

    private fun isFlightAllowedInWorld(worldName: String): Boolean {
        val allowedWorlds = plugin.configManager.getStringList("worlds.allowed-worlds")
        val disabledWorlds = plugin.configManager.getStringList("worlds.disabled-worlds")

        if (disabledWorlds.contains(worldName)) {
            return false
        }

        return allowedWorlds.isEmpty() || allowedWorlds.contains(worldName)
    }

    fun setFlightSpeed(player: Player, speed: Float): Boolean {
        if (!hasFlightEnabled(player)) return false

        val maxSpeed = plugin.configManager.getDouble("general.flight-speed.max-speed", 1.0).toFloat()
        val clampedSpeed = speed.coerceIn(0.0f, maxSpeed)
        player.flySpeed = clampedSpeed
        return true
    }
}
