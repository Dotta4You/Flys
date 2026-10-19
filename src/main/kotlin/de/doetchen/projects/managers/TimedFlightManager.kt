/*
 * ==========================================
 * Fly's Plugin v1.5
 * Made by Dötchen with <3
 * https://github.com/Dotta4You/Flys
 * ==========================================
 */

package de.doetchen.projects.managers

import de.doetchen.projects.Flys
import de.doetchen.projects.utils.DurationUtils
import de.doetchen.projects.utils.EffectUtils
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.persistence.PersistentDataType
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class TimedFlightManager(private val plugin: Flys) : Listener {

    enum class SavedTimedFlight { NONE, RUNNING, OVERDUE }

    private val expiryByPlayer = ConcurrentHashMap<UUID, Long>()
    private val lastWarnedSecondsByPlayer = HashMap<UUID, Long>()
    private val expiryKey = NamespacedKey(plugin, "fly_timed_until")

    fun startExpiryWatcher() {
        plugin.server.scheduler.runTaskTimer(
            plugin,
            Runnable { processTimedFlights() },
            WATCHER_INTERVAL_TICKS,
            WATCHER_INTERVAL_TICKS
        )
    }

    fun grantTimedFlight(player: Player, durationSeconds: Long) {
        val expiry = System.currentTimeMillis() + durationSeconds * 1000L
        expiryByPlayer[player.uniqueId] = expiry
        lastWarnedSecondsByPlayer[player.uniqueId] = durationSeconds
        player.persistentDataContainer.set(expiryKey, PersistentDataType.LONG, expiry)
    }

    fun endTimedFlight(player: Player) {
        expiryByPlayer.remove(player.uniqueId)
        lastWarnedSecondsByPlayer.remove(player.uniqueId)
        player.persistentDataContainer.remove(expiryKey)
    }

    fun hasTimedFlight(player: Player): Boolean = expiryByPlayer.containsKey(player.uniqueId)

    fun timedFlightSecondsLeft(player: Player): Long {
        val expiry = expiryByPlayer[player.uniqueId] ?: return 0L
        return secondsUntil(expiry, System.currentTimeMillis())
    }

    fun loadSavedTimedFlight(player: Player): SavedTimedFlight {
        val container = player.persistentDataContainer
        val expiry = container.get(expiryKey, PersistentDataType.LONG) ?: return SavedTimedFlight.NONE
        val now = System.currentTimeMillis()

        if (expiry <= now) {
            container.remove(expiryKey)
            expiryByPlayer.remove(player.uniqueId)
            lastWarnedSecondsByPlayer.remove(player.uniqueId)
            return SavedTimedFlight.OVERDUE
        }

        expiryByPlayer[player.uniqueId] = expiry
        lastWarnedSecondsByPlayer[player.uniqueId] = secondsUntil(expiry, now)
        return SavedTimedFlight.RUNNING
    }

    private fun secondsUntil(expiry: Long, now: Long): Long = ((expiry - now + 999L) / 1000L).coerceAtLeast(0L)

    private fun processTimedFlights() {
        if (expiryByPlayer.isEmpty()) return

        val now = System.currentTimeMillis()

        expiryByPlayer.entries.map { it.key to it.value }.forEach { (playerId, expiry) ->
            val player = plugin.server.getPlayer(playerId)
            when {
                player == null -> {
                    expiryByPlayer.remove(playerId)
                    lastWarnedSecondsByPlayer.remove(playerId)
                }
                expiry <= now -> endExpiredFlight(player)
                else -> warnBeforeExpiry(player, secondsUntil(expiry, now))
            }
        }
    }

    private fun endExpiredFlight(player: Player) {
        plugin.flightManager.disableFlight(player)
        plugin.messageUtils.sendActionBar(player, "flight.timed-expired")
        EffectUtils.playFlightDisabledEffects(player, plugin)
    }

    private fun warnBeforeExpiry(player: Player, secondsLeft: Long) {
        val lastWarned = lastWarnedSecondsByPlayer[player.uniqueId]
        if (lastWarned == null) {
            lastWarnedSecondsByPlayer[player.uniqueId] = secondsLeft
            return
        }

        val reached = WARNING_SECONDS.filter { it >= secondsLeft && it < lastWarned }
        if (reached.isEmpty()) return

        lastWarnedSecondsByPlayer[player.uniqueId] = reached.min()
        plugin.messageUtils.sendActionBar(player, "flight.timed-warning", "TIME" to DurationUtils.formatDuration(secondsLeft))
        EffectUtils.playTimedFlightWarningSound(player, plugin)
    }

    @EventHandler
    fun onTimedFlightPlayerQuit(event: PlayerQuitEvent) {
        expiryByPlayer.remove(event.player.uniqueId)
        lastWarnedSecondsByPlayer.remove(event.player.uniqueId)
    }

    private companion object {
        const val WATCHER_INTERVAL_TICKS = 20L
        val WARNING_SECONDS = listOf(30L, 10L, 5L, 4L, 3L, 2L, 1L)
    }
}
