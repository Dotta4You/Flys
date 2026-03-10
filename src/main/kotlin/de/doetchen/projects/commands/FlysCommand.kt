/*
 * ==========================================
 * Fly's Plugin v1.3
 * Made by Dötchen with <3
 * https://github.com/Dotta4You/Flys
 * ==========================================
 */

package de.doetchen.projects.commands

import de.doetchen.projects.Flys
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class FlysCommand(private val plugin: Flys) : CommandExecutor, TabCompleter {

    private fun getPermission(key: String, default: String): String {
        return plugin.configManager.getString(key).takeIf { it.isNotEmpty() } ?: default
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission(getPermission("permissions.admin", "flys.admin"))) {
            plugin.messageUtils.sendMessage(sender, "errors.no-permission")
            if (sender is Player && plugin.configManager.getBoolean("general.enable-sounds")) {
                sender.playSound(sender.location, "block.note_block.bass", 1.0f, 1.0f)
            }
            return true
        }

        if (args.isEmpty()) {
            sendUsageMessage(sender)
            return true
        }

        when (args[0].lowercase()) {
            "addworld" -> return handleAddWorld(sender, args)
            "removeworld" -> return handleRemoveWorld(sender, args)
            "listworlds" -> return handleListWorlds(sender)
            else -> {
                sendUsageMessage(sender)
                return true
            }
        }
    }

    private fun sendUsageMessage(sender: CommandSender) {
        plugin.messageUtils.sendMessage(sender, "world-management.usage-main")
        plugin.messageUtils.sendMessage(sender, "world-management.usage-main-line1")
        plugin.messageUtils.sendMessage(sender, "world-management.usage-main-line2")
        plugin.messageUtils.sendMessage(sender, "world-management.usage-main-line3")
    }

    private fun handleAddWorld(sender: CommandSender, args: Array<out String>): Boolean {
        if (args.size < 3) {
            plugin.messageUtils.sendMessage(sender, "world-management.usage-add")
            return true
        }

        val worldName = args[1]
        val type = args[2].lowercase()

        if (type != "allowed" && type != "disallowed") {
            plugin.messageUtils.sendMessage(sender, "world-management.usage-add")
            return true
        }

        val world = Bukkit.getWorld(worldName)
        if (world == null) {
            plugin.messageUtils.sendMessage(sender, "world-management.world-not-found", "WORLD" to worldName)
            return true
        }

        val allowedWorlds = plugin.configManager.getStringList("worlds.allowed-worlds").toMutableList()
        val disabledWorlds = plugin.configManager.getStringList("worlds.disabled-worlds").toMutableList()

        if (type == "allowed" && allowedWorlds.contains(worldName)) {
            plugin.messageUtils.sendMessage(sender, "world-management.already-in-allowed", "WORLD" to worldName)
            return true
        }
        if (type == "disallowed" && disabledWorlds.contains(worldName)) {
            plugin.messageUtils.sendMessage(sender, "world-management.already-in-disabled", "WORLD" to worldName)
            return true
        }

        allowedWorlds.remove(worldName)
        disabledWorlds.remove(worldName)

        if (type == "allowed") {
            allowedWorlds.add(worldName)
            plugin.messageUtils.sendMessage(sender, "world-management.world-added-allowed", "WORLD" to worldName)
        } else {
            disabledWorlds.add(worldName)
            plugin.messageUtils.sendMessage(sender, "world-management.world-added-disallowed", "WORLD" to worldName)

            world.players.forEach { player ->
                if (plugin.flightManager.hasFlightEnabled(player)) {
                    plugin.flightManager.disableFlight(player)
                    plugin.messageUtils.sendMessage(player, "errors.world-not-allowed")
                }
            }
        }

        plugin.configManager.setStringList("worlds.allowed-worlds", allowedWorlds)
        plugin.configManager.setStringList("worlds.disabled-worlds", disabledWorlds)
        plugin.configManager.saveConfig()

        return true
    }

    private fun handleRemoveWorld(sender: CommandSender, args: Array<out String>): Boolean {
        if (args.size < 2) {
            plugin.messageUtils.sendMessage(sender, "world-management.usage-remove")
            return true
        }

        val worldName = args[1]
        val allowedWorlds = plugin.configManager.getStringList("worlds.allowed-worlds").toMutableList()
        val disabledWorlds = plugin.configManager.getStringList("worlds.disabled-worlds").toMutableList()

        if (!allowedWorlds.contains(worldName) && !disabledWorlds.contains(worldName)) {
            plugin.messageUtils.sendMessage(sender, "world-management.world-not-in-list", "WORLD" to worldName)
            return true
        }

        allowedWorlds.remove(worldName)
        disabledWorlds.remove(worldName)

        plugin.configManager.setStringList("worlds.allowed-worlds", allowedWorlds)
        plugin.configManager.setStringList("worlds.disabled-worlds", disabledWorlds)
        plugin.configManager.saveConfig()

        plugin.messageUtils.sendMessage(sender, "world-management.world-removed", "WORLD" to worldName)

        return true
    }

    private fun handleListWorlds(sender: CommandSender): Boolean {
        val allowedWorlds = plugin.configManager.getStringList("worlds.allowed-worlds")
        val disabledWorlds = plugin.configManager.getStringList("worlds.disabled-worlds")

        plugin.messageUtils.sendMessage(sender, "world-management.list-header")

        if (allowedWorlds.isEmpty()) {
            plugin.messageUtils.sendMessage(sender, "world-management.all-worlds-allowed")
        } else {
            plugin.messageUtils.sendMessage(sender, "world-management.allowed-worlds", "COUNT" to allowedWorlds.size.toString())
            allowedWorlds.forEach { world ->
                plugin.messageUtils.sendMessage(sender, "world-management.world-entry", "WORLD" to world)
            }
        }

        if (disabledWorlds.isNotEmpty()) {
            plugin.messageUtils.sendMessage(sender, "world-management.disabled-worlds", "COUNT" to disabledWorlds.size.toString())
            disabledWorlds.forEach { world ->
                plugin.messageUtils.sendMessage(sender, "world-management.world-entry", "WORLD" to world)
            }
        }

        return true
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): List<String> {
        if (!sender.hasPermission(getPermission("permissions.admin", "flys.admin"))) {
            return emptyList()
        }

        return when (args.size) {
            1 -> {
                listOf("addworld", "removeworld", "listworlds")
                    .filter { it.lowercase().startsWith(args[0].lowercase()) }
                    .sorted()
            }
            2 -> {
                if (args[0].lowercase() in listOf("addworld", "removeworld")) {
                    Bukkit.getWorlds()
                        .map { it.name }
                        .filter { it.lowercase().startsWith(args[1].lowercase()) }
                        .sorted()
                } else {
                    emptyList()
                }
            }
            3 -> {
                if (args[0].lowercase() == "addworld") {
                    listOf("allowed", "disallowed")
                        .filter { it.lowercase().startsWith(args[2].lowercase()) }
                } else {
                    emptyList()
                }
            }
            else -> emptyList()
        }
    }
}

