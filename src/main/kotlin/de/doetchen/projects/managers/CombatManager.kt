/*
 * ==========================================
 * Fly's Plugin v1.5
 * Made by Dötchen with <3
 * https://github.com/Dotta4You/Flys
 * ==========================================
 */

package de.doetchen.projects.managers

import de.doetchen.projects.Flys
import de.doetchen.projects.utils.EffectUtils
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.PlayerDeathEvent
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class CombatManager(private val plugin: Flys) : Listener {

    private val combatLockedUntil = ConcurrentHashMap<UUID, Long>()

    fun startExpiryCleanup() {
        plugin.server.scheduler.runTaskTimer(
            plugin,
            Runnable { dropExpiredCombatLocks() },
            CLEANUP_INTERVAL_TICKS,
            CLEANUP_INTERVAL_TICKS
        )
    }

    fun isCombatLocked(player: Player): Boolean {
        if (!isCombatFlightRuleEnabled() || canBypassCombatLock(player)) return false

        val lockedUntil = combatLockedUntil[player.uniqueId] ?: return false
        if (lockedUntil > System.currentTimeMillis()) return true

        combatLockedUntil.remove(player.uniqueId, lockedUntil)
        return false
    }

    fun combatSecondsLeft(player: Player): Long {
        val lockedUntil = combatLockedUntil[player.uniqueId] ?: return 0L
        return ((lockedUntil - System.currentTimeMillis() + 999L) / 1000L).coerceAtLeast(0L)
    }

    private fun isCombatFlightRuleEnabled(): Boolean =
        plugin.configManager.getBoolean("extras.combat.disable-flight")

    private fun combatCooldownMillis(): Long =
        plugin.configManager.getInt("extras.combat.cooldown-seconds").coerceIn(1, MAX_COOLDOWN_SECONDS) * 1000L

    private fun canBypassCombatLock(player: Player): Boolean =
        player.hasPermission(plugin.configManager.getString("permissions.bypass-combat").ifEmpty { "flys.bypass.combat" })

    private fun lockForCombat(player: Player) {
        if (canBypassCombatLock(player)) return

        combatLockedUntil[player.uniqueId] = System.currentTimeMillis() + combatCooldownMillis()

        val flightManager = plugin.flightManager
        if (flightManager.hasNativeFlight(player) || !flightManager.hasActiveFlight(player)) return

        flightManager.disableFlight(player)
        plugin.messageUtils.sendActionBar(player, "flight.combat-disabled")
        EffectUtils.playFlightDisabledEffects(player, plugin)
    }

    private fun findAttackingPlayer(damager: Entity): Player? = when (damager) {
        is Player -> damager
        is Projectile -> damager.shooter as? Player
        else -> null
    }

    private fun isScriptedNpc(player: Player): Boolean = player.hasMetadata("NPC")

    private fun dropExpiredCombatLocks() {
        if (combatLockedUntil.isEmpty()) return

        val now = System.currentTimeMillis()
        combatLockedUntil.entries.removeIf { it.value <= now }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerHitPlayer(event: EntityDamageByEntityEvent) {
        if (!isCombatFlightRuleEnabled() || event.damage <= 0.0) return

        val victim = event.entity as? Player ?: return
        val attacker = findAttackingPlayer(event.damager) ?: return
        if (attacker.uniqueId == victim.uniqueId) return
        if (isScriptedNpc(victim) || isScriptedNpc(attacker)) return

        lockForCombat(victim)
        lockForCombat(attacker)
    }

    @EventHandler
    fun onCombatantDeath(event: PlayerDeathEvent) {
        combatLockedUntil.remove(event.entity.uniqueId)
    }

    private companion object {
        const val CLEANUP_INTERVAL_TICKS = 100L
        const val MAX_COOLDOWN_SECONDS = 3600
    }
}
