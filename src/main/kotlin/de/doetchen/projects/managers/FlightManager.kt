package de.doetchen.projects.managers

import de.doetchen.projects.Flys
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerGameModeChangeEvent
import org.bukkit.GameMode
import java.util.*

class FlightManager(private val plugin: Flys) : Listener {

    private val flyingPlayers = mutableSetOf<UUID>()

    fun enableFlight(player: Player) {
        player.allowFlight = true
        player.isFlying = true
        flyingPlayers.add(player.uniqueId)
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
            true
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
}
