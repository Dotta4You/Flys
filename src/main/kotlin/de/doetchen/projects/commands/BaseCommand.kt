/*
 * ==========================================
 * Fly's Plugin v1.4.1
 * Made by Dötchen with <3
 * https://github.com/Dotta4You/Flys
 * ==========================================
 */

package de.doetchen.projects.commands

import de.doetchen.projects.Flys
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

abstract class BaseCommand(protected val plugin: Flys) : CommandExecutor, TabCompleter {

    protected fun hasPermission(sender: CommandSender, key: String, default: String): Boolean =
        sender.hasPermission(plugin.configManager.getString(key).ifEmpty { default })

    protected fun checkPermission(sender: CommandSender, key: String, default: String): Boolean {
        if (hasPermission(sender, key, default)) return true

        plugin.messageUtils.sendMessage(sender, "errors.no-permission")
        if (sender is Player) playSound(sender, "block.note_block.bass")
        return false
    }

    protected fun playSound(player: Player, sound: String, volume: Float = 1.0f, pitch: Float = 1.0f) {
        if (plugin.configManager.getBoolean("general.enable-sounds")) {
            player.playSound(player.location, sound, volume, pitch)
        }
    }

    protected fun Collection<String>.startingWith(input: String): List<String> =
        filter { it.startsWith(input, ignoreCase = true) }

    protected fun onlinePlayerNames(): List<String> = plugin.server.onlinePlayers.map { it.name }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): List<String> = emptyList()
}
