/*
 * ==========================================
 * Fly's Plugin v1.5
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
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerGameModeChangeEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.persistence.PersistentDataType
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.floor
import kotlin.math.roundToInt

class FlightManager(private val plugin: Flys) : Listener {

    enum class FlightStartResult { STARTED, WORLD_BLOCKED, IN_COMBAT }

    private val flyingPlayers: MutableSet<UUID> = ConcurrentHashMap.newKeySet()
    private val speedKey = NamespacedKey(plugin, "fly_speed")
    private val flightStateKey = NamespacedKey(plugin, "fly_enabled")

    fun startFlight(player: Player): FlightStartResult {
        if (!isFlightAllowedInWorld(player.world.name)) return FlightStartResult.WORLD_BLOCKED
        if (plugin.combatManager.isCombatLocked(player)) return FlightStartResult.IN_COMBAT

        player.allowFlight = true
        player.isFlying = true
        flyingPlayers.add(player.uniqueId)
        plugin.fallProtectionManager.revokeLandingProtection(player.uniqueId)
        applyStoredSpeed(player)
        markFlightSaved(player)

        return FlightStartResult.STARTED
    }

    fun disableFlight(player: Player) {
        val wasAirborne = player.isFlying

        if (!hasNativeFlight(player)) {
            player.allowFlight = false
            player.isFlying = false
            if (wasAirborne) plugin.fallProtectionManager.grantLandingProtection(player)
        }

        flyingPlayers.remove(player.uniqueId)
        plugin.timedFlightManager.endTimedFlight(player)
        player.persistentDataContainer.remove(flightStateKey)
    }

    fun hasFlightEnabled(player: Player): Boolean = player.uniqueId in flyingPlayers

    fun hasActiveFlight(player: Player): Boolean =
        hasFlightEnabled(player) || (!hasNativeFlight(player) && player.allowFlight)

    fun canAdjustSpeed(player: Player): Boolean = hasActiveFlight(player) || hasNativeFlight(player)

    fun hasNativeFlight(player: Player): Boolean =
        player.gameMode == GameMode.CREATIVE || player.gameMode == GameMode.SPECTATOR

    @Suppress("DEPRECATION")
    fun isStandingOnGround(player: Player): Boolean = player.isOnGround

    fun getFlyingPlayerCount(): Int = flyingPlayers.count { plugin.server.getPlayer(it) != null }

    fun setFlightSpeed(player: Player, speed: Float): Boolean {
        if (!canAdjustSpeed(player)) return false

        val clampedSpeed = speed.coerceIn(0.0f, maxSpeedFor(player))
        player.flySpeed = clampedSpeed
        player.persistentDataContainer.set(speedKey, PersistentDataType.FLOAT, clampedSpeed)
        return true
    }

    fun speedLevelOf(player: Player): Int = (player.flySpeed * SPEED_LEVEL_FACTOR).roundToInt()

    fun maxSpeedFor(player: Player): Float {
        val configuredMax = configuredMaxSpeed()
        val rankLimit = rankSpeedLimit(player) ?: return configuredMax
        return minOf(configuredMax, rankLimit)
    }

    fun maxSpeedLevelFor(player: Player): Int =
        floor(maxSpeedFor(player) * SPEED_LEVEL_FACTOR + SPEED_LEVEL_TOLERANCE).toInt()
            .coerceIn(MIN_SPEED_LEVEL, MAX_SPEED_LEVEL)

    fun restoreSavedFlight(player: Player) {
        enforceRankSpeedLimit(player)

        val timedState = plugin.timedFlightManager.loadSavedTimedFlight(player)
        if (timedState == TimedFlightManager.SavedTimedFlight.OVERDUE) {
            dropOverdueTimedFlight(player)
            return
        }

        val timedFlightRunning = timedState == TimedFlightManager.SavedTimedFlight.RUNNING
        val restorePermanent = !timedFlightRunning && shouldRestorePermanentFlight(player)

        if (!timedFlightRunning && !restorePermanent) {
            player.persistentDataContainer.remove(flightStateKey)
            return
        }

        if (!isFlightAllowedInWorld(player.world.name) || plugin.combatManager.isCombatLocked(player)) {
            plugin.timedFlightManager.endTimedFlight(player)
            player.persistentDataContainer.remove(flightStateKey)
            return
        }

        flyingPlayers.add(player.uniqueId)
        markFlightSaved(player)

        if (!hasNativeFlight(player)) {
            player.allowFlight = true
            if (!isStandingOnGround(player)) player.isFlying = true
            applyStoredSpeed(player)
        }
    }

    internal fun isFlightAllowedInWorld(worldName: String): Boolean {
        val config = plugin.configManager
        if (worldName in config.disabledWorlds) return false
        return config.allowedWorlds.isEmpty() || worldName in config.allowedWorlds
    }

    private fun configuredMaxSpeed(): Float {
        val configured = plugin.configManager.getDouble("general.flight-speed.max-speed", 1.0).toFloat()
        return if (configured.isNaN()) 1.0f else configured.coerceIn(0.0f, 1.0f)
    }

    private fun defaultSpeed(): Float {
        val configured = plugin.configManager.getDouble("general.flight-speed.default-speed", 0.1).toFloat()
        return if (configured.isNaN()) 0.1f else configured
    }

    private fun storedSpeed(player: Player): Float? =
        player.persistentDataContainer.get(speedKey, PersistentDataType.FLOAT)

    private fun applyStoredSpeed(player: Player) {
        player.flySpeed = (storedSpeed(player) ?: defaultSpeed()).coerceIn(0.0f, maxSpeedFor(player))
    }

    private fun rankSpeedLimit(player: Player): Float? {
        val limitPermission = plugin.configManager.getString("permissions.flyspeed-limit").ifEmpty { "flys.maxspeed" }
        val highestLevel = (MAX_SPEED_LEVEL downTo MIN_SPEED_LEVEL)
            .firstOrNull { player.hasPermission("$limitPermission.$it") } ?: return null
        return highestLevel / SPEED_LEVEL_FACTOR
    }

    private fun enforceRankSpeedLimit(player: Player) {
        val rankLimit = rankSpeedLimit(player) ?: return
        if (player.flySpeed > rankLimit) player.flySpeed = rankLimit
    }

    private fun shouldRestorePermanentFlight(player: Player): Boolean {
        if (!plugin.configManager.getBoolean("extras.restore-flight-on-join")) return false
        if (!player.persistentDataContainer.has(flightStateKey, PersistentDataType.BYTE)) return false

        val flyPermission = plugin.configManager.getString("permissions.fly").ifEmpty { "flys.fly" }
        return player.hasPermission(flyPermission)
    }

    private fun dropOverdueTimedFlight(player: Player) {
        player.persistentDataContainer.remove(flightStateKey)
        flyingPlayers.remove(player.uniqueId)
        plugin.messageUtils.sendActionBar(player, "flight.timed-expired")
    }

    private fun markFlightSaved(player: Player) {
        player.persistentDataContainer.set(flightStateKey, PersistentDataType.BYTE, SAVED_FLAG)
    }

    private fun reapplyFlightNextTick(player: Player) {
        plugin.server.scheduler.runTaskLater(plugin, Runnable {
            if (!player.isOnline || !hasFlightEnabled(player)) return@Runnable

            if (!isFlightAllowedInWorld(player.world.name)) {
                disableFlight(player)
                plugin.messageUtils.sendMessage(player, "errors.world-not-allowed")
                return@Runnable
            }

            if (!hasNativeFlight(player)) {
                player.allowFlight = true
                applyStoredSpeed(player)
            }
        }, 1L)
    }

    @EventHandler
    fun onFlightPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        plugin.server.scheduler.runTaskLater(plugin, Runnable {
            if (player.isOnline) restoreSavedFlight(player)
        }, JOIN_RESTORE_DELAY_TICKS)
    }

    @EventHandler
    fun onFlightPlayerQuit(event: PlayerQuitEvent) {
        val player = event.player
        flyingPlayers.remove(player.uniqueId)

        if (!plugin.configManager.getBoolean("extras.restore-flight-on-join")) {
            player.persistentDataContainer.remove(flightStateKey)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onFlightGameModeChange(event: PlayerGameModeChangeEvent) {
        val player = event.player

        val mode = event.newGameMode
        if ((mode == GameMode.SURVIVAL || mode == GameMode.ADVENTURE) && hasFlightEnabled(player)) {
            reapplyFlightNextTick(player)
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onFlightPlayerRespawn(event: PlayerRespawnEvent) {
        if (hasFlightEnabled(event.player)) reapplyFlightNextTick(event.player)
    }

    @EventHandler
    fun onFlightWorldChange(event: PlayerChangedWorldEvent) {
        val player = event.player
        if (!hasFlightEnabled(player)) return

        if (isFlightAllowedInWorld(player.world.name)) {
            reapplyFlightNextTick(player)
        } else {
            disableFlight(player)
            plugin.messageUtils.sendMessage(player, "errors.world-not-allowed")
        }
    }

    private companion object {
        const val JOIN_RESTORE_DELAY_TICKS = 2L
        const val MIN_SPEED_LEVEL = 1
        const val MAX_SPEED_LEVEL = 10
        const val SPEED_LEVEL_FACTOR = 10.0f
        const val SPEED_LEVEL_TOLERANCE = 0.0001f
        const val SAVED_FLAG: Byte = 1
    }
}
