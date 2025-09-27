/*
 * ==========================================
 * Fly's Plugin v1.0
 * Made by Dötchen with <3
 * https://github.com/Dotta4You/Flys
 * ==========================================
 */
package de.doetchen.projects.commands

import de.doetchen.projects.Flys
import de.doetchen.projects.utils.EffectUtils
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class FlySpeedCommand(private val plugin: Flys) : CommandExecutor, TabCompleter {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            plugin.messageUtils.sendMessage(sender, "errors.player-only")
            return true
        }

        val permission = plugin.configManager.getString("permissions.flyspeed").takeIf { it.isNotEmpty() } ?: "flys.flyspeed"
        if (!sender.hasPermission(permission)) {
            plugin.messageUtils.sendMessage(sender, "errors.no-permission")
            if (plugin.configManager.getBoolean("general.enable-sounds")) {
                sender.playSound(sender.location, "block.note_block.bass", 1.0f, 1.0f)
            }
            return true
        }

        when (args.size) {
            0 -> {
                val currentSpeed = (sender.flySpeed * 10).toInt()
                plugin.messageUtils.sendMessage(sender, "flyspeed.current", "SPEED" to currentSpeed.toString())
                if (plugin.configManager.getBoolean("general.enable-sounds")) {
                    sender.playSound(sender.location, "block.note_block.harp", 0.5f, 1.2f)
                }
            }
            1 -> {
                val speedInput = args[0].toIntOrNull()
                if (speedInput == null || speedInput !in 1..10) {
                    plugin.messageUtils.sendMessage(sender, "flyspeed.invalid-range")
                    return true
                }

                if (!plugin.flightManager.hasFlightEnabled(sender)) {
                    plugin.messageUtils.sendMessage(sender, "flyspeed.flight-not-enabled")
                    if (plugin.configManager.getBoolean("general.enable-sounds")) {
                        sender.playSound(sender.location, "block.note_block.bass", 1.0f, 0.5f)
                    }
                    return true
                }

                val normalizedSpeed = speedInput / 10.0f
                plugin.flightManager.setFlightSpeed(sender, normalizedSpeed)
                plugin.messageUtils.sendMessage(sender, "flyspeed.set", "SPEED" to speedInput.toString())
                EffectUtils.playSpeedChangeEffect(sender, plugin)
            }
            2 -> {
                val speedInput = args[1].toIntOrNull()
                if (speedInput == null || speedInput !in 1..10) {
                    plugin.messageUtils.sendMessage(sender, "flyspeed.invalid-range")
                    return true
                }

                val othersPermission = plugin.configManager.getString("permissions.flyspeed-others").takeIf { it.isNotEmpty() } ?: "flys.flyspeed.others"
                if (!sender.hasPermission(othersPermission)) {
                    plugin.messageUtils.sendMessage(sender, "errors.no-permission")
                    if (plugin.configManager.getBoolean("general.enable-sounds")) {
                        sender.playSound(sender.location, "block.note_block.bass", 1.0f, 1.0f)
                    }
                    return true
                }

                val targetPlayer = Bukkit.getPlayer(args[0])
                if (targetPlayer == null) {
                    plugin.messageUtils.sendMessage(sender, "errors.player-not-found", "PLAYER" to args[0])
                    return true
                }

                if (!plugin.flightManager.hasFlightEnabled(targetPlayer)) {
                    plugin.messageUtils.sendMessage(sender, "flyspeed.target-flight-not-enabled", "PLAYER" to targetPlayer.name)
                    return true
                }

                val normalizedSpeed = speedInput / 10.0f
                plugin.flightManager.setFlightSpeed(targetPlayer, normalizedSpeed)
                plugin.messageUtils.sendMessage(sender, "flyspeed.set-other", "PLAYER" to targetPlayer.name, "SPEED" to speedInput.toString())
                plugin.messageUtils.sendMessage(targetPlayer, "flyspeed.set-by-other", "SENDER" to sender.name, "SPEED" to speedInput.toString())
                EffectUtils.playSpeedChangeEffect(targetPlayer, plugin)
            }
            else -> {
                plugin.messageUtils.sendMessage(sender, "flyspeed.usage")
            }
        }

        return true
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): List<String> {
        val permission = plugin.configManager.getString("permissions.flyspeed").takeIf { it.isNotEmpty() } ?: "flys.flyspeed"
        if (!sender.hasPermission(permission)) {
            return emptyList()
        }

        return when (args.size) {
            1 -> {
                val othersPermission = plugin.configManager.getString("permissions.flyspeed-others").takeIf { it.isNotEmpty() } ?: "flys.flyspeed.others"
                val speedOptions = (1..10).map { it.toString() }

                if (sender.hasPermission(othersPermission)) {
                    val playerNames = Bukkit.getOnlinePlayers()
                        .map { it.name }
                        .filter { it.lowercase().startsWith(args[0].lowercase()) }

                    (speedOptions + playerNames).filter { it.lowercase().startsWith(args[0].lowercase()) }.sorted()
                } else {
                    speedOptions.filter { it.startsWith(args[0]) }
                }
            }
            2 -> {
                val othersPermission = plugin.configManager.getString("permissions.flyspeed-others").takeIf { it.isNotEmpty() } ?: "flys.flyspeed.others"
                if (sender.hasPermission(othersPermission)) {
                    (1..10).map { it.toString() }.filter { it.startsWith(args[1]) }
                } else {
                    emptyList()
                }
            }
            else -> emptyList()
        }
    }
}
