/*
 * ==========================================
 * Fly's Plugin v1.4.1
 * Made by Dötchen with <3
 * https://github.com/Dotta4You/Flys
 * ==========================================
 */

package de.doetchen.projects.commands

import de.doetchen.projects.Flys
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

                val target = plugin.server.getPlayer(args[0])
                if (target == null) {
                    plugin.messageUtils.sendMessage(sender, "errors.player-not-found", "PLAYER" to args[0])
                    return true
                }

                toggleFlight(sender, target)
            }
            else -> plugin.messageUtils.sendMessage(sender, "errors.usage")
        }

        return true
    }

    private fun toggleFlight(sender: Player, target: Player) {
        val wasFlying = plugin.flightManager.hasFlightEnabled(target)
        val enabled = plugin.flightManager.toggleFlight(target)
        if (!wasFlying && !enabled) return

        val state = if (enabled) "enabled" else "disabled"
        if (sender == target) {
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

        if (args.size == 1 && hasPermission(sender, "permissions.fly-others", "flys.fly.others")) {
            return onlinePlayerNames().startingWith(args[0]).sorted()
        }
        return emptyList()
    }
}
