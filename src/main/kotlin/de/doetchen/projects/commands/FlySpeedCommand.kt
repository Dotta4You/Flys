/*
 * ==========================================
 * Fly's Plugin v1.4
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

class FlySpeedCommand(plugin: Flys) : BaseCommand(plugin) {

    private val speedOptions = (1..10).map { it.toString() }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            plugin.messageUtils.sendMessage(sender, "errors.player-only")
            return true
        }

        if (!checkPermission(sender, "permissions.flyspeed", "flys.flyspeed")) return true

        when (args.size) {
            0 -> showSpeed(sender)
            1 -> setOwnSpeed(sender, args[0])
            2 -> setOtherSpeed(sender, args[0], args[1])
            else -> plugin.messageUtils.sendMessage(sender, "flyspeed.usage")
        }

        return true
    }

    private fun parseSpeed(input: String): Int? = input.toIntOrNull()?.takeIf { it in 1..10 }

    private fun showSpeed(player: Player) {
        val currentSpeed = (player.flySpeed * 10).toInt()
        plugin.messageUtils.sendMessage(player, "flyspeed.current", "SPEED" to currentSpeed.toString())
        playSound(player, "block.note_block.harp", 0.5f, 1.2f)
    }

    private fun setOwnSpeed(player: Player, input: String) {
        val speed = parseSpeed(input)
        if (speed == null) {
            plugin.messageUtils.sendMessage(player, "flyspeed.invalid-range")
            return
        }

        if (!plugin.flightManager.hasFlightEnabled(player)) {
            plugin.messageUtils.sendMessage(player, "flyspeed.flight-not-enabled")
            playSound(player, "block.note_block.bass", 1.0f, 0.5f)
            return
        }

        plugin.flightManager.setFlightSpeed(player, speed / 10.0f)
        plugin.messageUtils.sendMessage(player, "flyspeed.set", "SPEED" to speed.toString())
        EffectUtils.playSpeedChangeEffect(player, plugin)
    }

    private fun setOtherSpeed(sender: Player, targetName: String, input: String) {
        val speed = parseSpeed(input)
        if (speed == null) {
            plugin.messageUtils.sendMessage(sender, "flyspeed.invalid-range")
            return
        }

        if (!checkPermission(sender, "permissions.flyspeed-others", "flys.flyspeed.others")) return

        val target = plugin.server.getPlayer(targetName)
        if (target == null) {
            plugin.messageUtils.sendMessage(sender, "errors.player-not-found", "PLAYER" to targetName)
            return
        }

        if (!plugin.flightManager.hasFlightEnabled(target)) {
            plugin.messageUtils.sendMessage(sender, "flyspeed.target-flight-not-enabled", "PLAYER" to target.name)
            return
        }

        plugin.flightManager.setFlightSpeed(target, speed / 10.0f)
        plugin.messageUtils.sendMessage(sender, "flyspeed.set-other", "PLAYER" to target.name, "SPEED" to speed.toString())
        plugin.messageUtils.sendMessage(target, "flyspeed.set-by-other", "SENDER" to sender.name, "SPEED" to speed.toString())
        EffectUtils.playSpeedChangeEffect(target, plugin)
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): List<String> {
        if (!hasPermission(sender, "permissions.flyspeed", "flys.flyspeed")) return emptyList()

        val canTargetOthers = hasPermission(sender, "permissions.flyspeed-others", "flys.flyspeed.others")

        return when (args.size) {
            1 -> if (canTargetOthers) {
                (speedOptions + onlinePlayerNames()).startingWith(args[0]).sorted()
            } else {
                speedOptions.startingWith(args[0])
            }
            2 -> if (canTargetOthers) speedOptions.startingWith(args[1]) else emptyList()
            else -> emptyList()
        }
    }
}
