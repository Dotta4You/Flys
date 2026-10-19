/*
 * ==========================================
 * Fly's Plugin v1.5
 * Made by Dötchen with <3
 * https://github.com/Dotta4You/Flys
 * ==========================================
 */

package de.doetchen.projects.commands

import de.doetchen.projects.Flys
import de.doetchen.projects.managers.FlightManager.FlightStartResult
import de.doetchen.projects.utils.DurationUtils
import de.doetchen.projects.utils.EffectUtils
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class FlyCommand(plugin: Flys) : BaseCommand(plugin) {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            plugin.messageUtils.sendMessage(sender, "errors.player-only")
            return true
        }

        if (!checkPermission(sender, "permissions.fly", "flys.fly")) return true

        when (args.size) {
            0 -> toggleFlight(sender, sender)
            1 -> {
                if (!checkPermission(sender, "permissions.fly-others", "flys.fly.others")) return true

                val target = findOnlineTarget(sender, args[0]) ?: return true
                toggleFlight(sender, target)
            }
            2 -> giveTimedFlight(sender, args[0], args[1])
            else -> plugin.messageUtils.sendMessage(sender, "errors.usage")
        }

        return true
    }

    private fun findOnlineTarget(sender: Player, name: String): Player? {
        val target = plugin.server.getPlayer(name)
        if (target == null) {
            plugin.messageUtils.sendMessage(sender, "errors.player-not-found", "PLAYER" to name)
        }
        return target
    }

    private fun toggleFlight(sender: Player, target: Player) {
        val flightManager = plugin.flightManager

        if (flightManager.hasNativeFlight(target)) {
            plugin.messageUtils.sendMessage(sender, "errors.native-flight")
            return
        }

        if (flightManager.hasActiveFlight(target)) {
            flightManager.disableFlight(target)
            announceToggle(sender, target, enabled = false)
            return
        }

        if (!startFlightOrReport(sender, target)) return
        announceToggle(sender, target, enabled = true)
    }

    private fun giveTimedFlight(sender: Player, targetName: String, timeInput: String) {
        if (!checkPermission(sender, "permissions.fly-temp", "flys.fly.temp")) return

        val target = findOnlineTarget(sender, targetName) ?: return
        val targetsSelf = target.uniqueId == sender.uniqueId
        if (!targetsSelf && !checkPermission(sender, "permissions.fly-others", "flys.fly.others")) return

        val seconds = DurationUtils.parseDurationSeconds(timeInput)
        if (seconds == null) {
            plugin.messageUtils.sendMessage(sender, "errors.invalid-time")
            return
        }

        val longestAllowed = longestTimedFlightSeconds()
        if (seconds > longestAllowed) {
            plugin.messageUtils.sendMessage(sender, "errors.time-too-long", "MAX" to DurationUtils.formatDuration(longestAllowed))
            return
        }

        if (plugin.flightManager.hasNativeFlight(target)) {
            plugin.messageUtils.sendMessage(sender, "errors.native-flight")
            return
        }

        if (!startFlightOrReport(sender, target)) return
        plugin.timedFlightManager.grantTimedFlight(target, seconds)

        val time = DurationUtils.formatDuration(seconds)
        if (targetsSelf) {
            plugin.messageUtils.sendActionBar(target, "flight.timed-enabled-self", "TIME" to time)
        } else {
            plugin.messageUtils.sendMessage(sender, "flight.timed-enabled-other", "PLAYER" to target.name, "TIME" to time)
            plugin.messageUtils.sendActionBar(target, "flight.timed-enabled-by-other", "SENDER" to sender.name, "TIME" to time)
        }
        EffectUtils.playFlightEnabledEffects(target, plugin)
    }

    private fun longestTimedFlightSeconds(): Long =
        plugin.configManager.getLong("extras.timed-flight-max-seconds").coerceAtLeast(1L)

    private fun startFlightOrReport(sender: Player, target: Player): Boolean {
        val targetsSelf = target.uniqueId == sender.uniqueId

        return when (plugin.flightManager.startFlight(target)) {
            FlightStartResult.STARTED -> true
            FlightStartResult.WORLD_BLOCKED -> {
                if (targetsSelf) {
                    plugin.messageUtils.sendMessage(sender, "errors.world-not-allowed")
                } else {
                    plugin.messageUtils.sendMessage(sender, "errors.target-world-not-allowed", "PLAYER" to target.name)
                }
                false
            }
            FlightStartResult.IN_COMBAT -> {
                if (targetsSelf) {
                    plugin.messageUtils.sendMessage(sender, "errors.in-combat", "TIME" to combatTimeLeft(target))
                } else {
                    plugin.messageUtils.sendMessage(sender, "errors.target-in-combat", "PLAYER" to target.name, "TIME" to combatTimeLeft(target))
                }
                false
            }
        }
    }

    private fun combatTimeLeft(target: Player): String =
        DurationUtils.formatDuration(plugin.combatManager.combatSecondsLeft(target))

    private fun announceToggle(sender: Player, target: Player, enabled: Boolean) {
        val state = if (enabled) "enabled" else "disabled"
        if (sender.uniqueId == target.uniqueId) {
            plugin.messageUtils.sendActionBar(target, "flight.$state-self")
        } else {
            plugin.messageUtils.sendMessage(sender, "flight.$state-other", "PLAYER" to target.name)
            plugin.messageUtils.sendActionBar(target, "flight.$state-by-other", "SENDER" to sender.name)
        }

        if (enabled) {
            EffectUtils.playFlightEnabledEffects(target, plugin)
        } else {
            EffectUtils.playFlightDisabledEffects(target, plugin)
        }
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): List<String> {
        if (!hasPermission(sender, "permissions.fly", "flys.fly")) return emptyList()

        if (args.size != 1 || !hasPermission(sender, "permissions.fly-others", "flys.fly.others")) return emptyList()
        return onlinePlayerNames().startingWith(args[0]).sorted()
    }
}
