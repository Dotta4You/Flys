package de.doetchen.projects.commands

import de.doetchen.projects.Flys
import de.doetchen.projects.utils.EffectUtils
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Entity
import org.bukkit.entity.Player

class FlyCommand(private val plugin: Flys) : CommandExecutor, TabCompleter {

    companion object {
        const val FLY_PERMS = "flys.fly"
        const val FLY_PERMS_OTHERS = "flys.fly.others"
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            plugin.messageUtils.sendMessage(sender, "errors.player-only")
            return true
        }

        if (!sender.hasPermission(FLY_PERMS)) {
            plugin.messageUtils.sendMessage(sender, "errors.no-permission")
            sender.playSound(sender.location, "block.note_block.bass", 1.0f, 1.0f)
            return true
        }

        when (args.size) {
            0 -> toggleFlight(sender, sender)
            1 -> {
                if (!sender.hasPermission(FLY_PERMS_OTHERS)) {
                    plugin.messageUtils.sendMessage(sender, "errors.no-permission")
                    sender.playSound(sender.location, "block.note_block.bass", 1.0f, 1.0f)
                    return true
                }

                val targetPlayer = Bukkit.getPlayer(args[0])
                if (targetPlayer == null) {
                    plugin.messageUtils.sendMessage(sender, "errors.player-not-found", "PLAYER" to args[0])
                    return true
                }

                toggleFlight(sender, targetPlayer)
            }
            else -> {
                plugin.messageUtils.sendMessage(sender, "errors.usage")
                return true
            }
        }

        return true
    }

    private fun toggleFlight(sender: CommandSender, target: Player) {
        val flightEnabled = plugin.flightManager.toggleFlight(target)

        if (flightEnabled) {
            // Flight enabled
            if (sender == target) {
                plugin.messageUtils.sendActionBar(target, "flight.enabled-self")
                EffectUtils.playFlightEnabledEffects(target, plugin)
            } else {
                plugin.messageUtils.sendMessage(sender, "flight.enabled-other", "PLAYER" to target.name)
                plugin.messageUtils.sendActionBar(target, "flight.enabled-by-other", "SENDER" to sender.name)
                EffectUtils.playFlightEnabledEffects(target, plugin)
            }
        } else {
            // Flight disabled
            if (sender == target) {
                plugin.messageUtils.sendActionBar(target, "flight.disabled-self")
                EffectUtils.playFlightDisabledEffects(target, plugin)
            } else {
                plugin.messageUtils.sendMessage(sender, "flight.disabled-other", "PLAYER" to target.name)
                plugin.messageUtils.sendActionBar(target, "flight.disabled-by-other", "SENDER" to sender.name)
                EffectUtils.playFlightDisabledEffects(target, plugin)
            }
        }
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): List<String> {
        if (!sender.hasPermission(FLY_PERMS)) {
            return emptyList()
        }

        return when (args.size) {
            1 -> {
                if (sender.hasPermission(FLY_PERMS_OTHERS)) {
                    Bukkit.getOnlinePlayers()
                        .map { it.name }
                        .filter { it.lowercase().startsWith(args[0].lowercase()) }
                        .sorted()
                } else {
                    emptyList()
                }
            }
            else -> emptyList()
        }
    }
}