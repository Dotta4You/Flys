/*
 * ==========================================
 * Fly's Plugin v1.5
 * Made by Dötchen with <3
 * https://github.com/Dotta4You/Flys
 * ==========================================
 */

package de.doetchen.projects.managers

import de.doetchen.projects.Flys
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class FallProtectionManager(private val plugin: Flys) : Listener {

    private val protectedPlayers: MutableSet<UUID> = ConcurrentHashMap.newKeySet()

    fun startLandingWatcher() {
        plugin.server.scheduler.runTaskTimer(
            plugin,
            Runnable { releaseLandedPlayers() },
            WATCHER_INTERVAL_TICKS,
            WATCHER_INTERVAL_TICKS
        )
    }

    fun grantLandingProtection(player: Player) {
        if (!plugin.configManager.getBoolean("extras.fall-protection")) return
        protectedPlayers.add(player.uniqueId)
    }

    fun revokeLandingProtection(playerId: UUID) {
        protectedPlayers.remove(playerId)
    }

    private fun releaseLandedPlayers() {
        if (protectedPlayers.isEmpty()) return

        protectedPlayers.removeIf { playerId ->
            val player = plugin.server.getPlayer(playerId)
            player == null || plugin.flightManager.isStandingOnGround(player)
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onProtectedFall(event: EntityDamageEvent) {
        if (event.cause != EntityDamageEvent.DamageCause.FALL) return

        val player = event.entity as? Player ?: return
        if (protectedPlayers.remove(player.uniqueId)) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onProtectedPlayerQuit(event: PlayerQuitEvent) {
        revokeLandingProtection(event.player.uniqueId)
    }

    @EventHandler
    fun onProtectedPlayerDeath(event: PlayerDeathEvent) {
        revokeLandingProtection(event.entity.uniqueId)
    }

    private companion object {
        const val WATCHER_INTERVAL_TICKS = 5L
    }
}
