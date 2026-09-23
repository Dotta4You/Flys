/*
 * ==========================================
 * Fly's Plugin v1.4.1
 * Made by Dötchen with <3
 * https://github.com/Dotta4You/Flys
 * ==========================================
 */

package de.doetchen.projects.managers

import de.doetchen.projects.Flys
import org.bukkit.GameMode
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerGameModeChangeEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.persistence.PersistentDataType
import java.util.UUID

class FlightManager(private val plugin: Flys) : Listener {

    private val flyingPlayers = mutableSetOf<UUID>()
    private val speedKey = NamespacedKey(plugin, "fly_speed")

    fun enableFlight(player: Player): Boolean {
        if (!isFlightAllowedInWorld(player.world.name)) {
            plugin.messageUtils.sendMessage(player, "errors.world-not-allowed")
            return false
        }

        player.allowFlight = true
        player.isFlying = true
        flyingPlayers.add(player.uniqueId)

        player.flySpeed = (savedSpeed(player) ?: defaultSpeed()).coerceIn(0.0f, 1.0f)

        return true
    }

    fun disableFlight(player: Player) {
        if (!hasNativeFlight(player)) {
            player.allowFlight = false
            player.isFlying = false
        }
        flyingPlayers.remove(player.uniqueId)
    }

    fun hasFlightEnabled(player: Player): Boolean = player.uniqueId in flyingPlayers

    fun canAdjustSpeed(player: Player): Boolean = hasFlightEnabled(player) || hasNativeFlight(player)

    private fun hasNativeFlight(player: Player): Boolean =
        player.gameMode == GameMode.CREATIVE || player.gameMode == GameMode.SPECTATOR

    private fun defaultSpeed(): Float =
        plugin.configManager.getDouble("general.flight-speed.default-speed", 0.1).toFloat()

    private fun savedSpeed(player: Player): Float? =
        player.persistentDataContainer.get(speedKey, PersistentDataType.FLOAT)

    fun toggleFlight(player: Player): Boolean {
        if (hasFlightEnabled(player)) {
            disableFlight(player)
            return false
        }
        return enableFlight(player)
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        flyingPlayers.remove(event.player.uniqueId)
    }

    @EventHandler
    fun onGameModeChange(event: PlayerGameModeChangeEvent) {
        val player = event.player

        val mode = event.newGameMode
        if ((mode == GameMode.SURVIVAL || mode == GameMode.ADVENTURE) && hasFlightEnabled(player)) {
            restoreAllowFlightNextTick(player)
        }
    }

    @EventHandler
    fun onWorldChange(event: PlayerChangedWorldEvent) {
        val player = event.player
        if (!hasFlightEnabled(player)) return

        if (isFlightAllowedInWorld(player.world.name)) {
            restoreAllowFlightNextTick(player)
        } else {
            disableFlight(player)
            plugin.messageUtils.sendMessage(player, "errors.world-not-allowed")
        }
    }

    private fun restoreAllowFlightNextTick(player: Player) {
        plugin.server.scheduler.runTaskLater(plugin, Runnable {
            if (player.isOnline && hasFlightEnabled(player)) player.allowFlight = true
        }, 1L)
    }

    internal fun isFlightAllowedInWorld(worldName: String): Boolean {
        val config = plugin.configManager
        if (worldName in config.disabledWorlds) return false
        return config.allowedWorlds.isEmpty() || worldName in config.allowedWorlds
    }

    fun setFlightSpeed(player: Player, speed: Float): Boolean {
        if (!canAdjustSpeed(player)) return false

        val maxSpeed = plugin.configManager.getDouble("general.flight-speed.max-speed", 1.0).toFloat()
        val clampedSpeed = speed.coerceIn(0.0f, maxSpeed)
        player.flySpeed = clampedSpeed
        player.persistentDataContainer.set(speedKey, PersistentDataType.FLOAT, clampedSpeed)
        return true
    }

    fun getFlyingPlayerCount(): Int = flyingPlayers.count { plugin.server.getPlayer(it) != null }
}
