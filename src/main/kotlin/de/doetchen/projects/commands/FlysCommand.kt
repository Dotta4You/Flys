/*
 * ==========================================
 * Fly's Plugin v1.4
 * Made by Dötchen with <3
 * https://github.com/Dotta4You/Flys
 * ==========================================
 */

package de.doetchen.projects.commands

import de.doetchen.projects.Flys
import org.bukkit.command.Command
import org.bukkit.command.CommandSender

class FlysCommand(plugin: Flys) : BaseCommand(plugin) {

    private val subCommands = listOf("addworld", "removeworld", "listworlds")
    private val listTypes = listOf("allowed", "disallowed")

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!checkPermission(sender, "permissions.admin", "flys.admin")) return true

        when (args.firstOrNull()?.lowercase()) {
            "addworld" -> handleAddWorld(sender, args)
            "removeworld" -> handleRemoveWorld(sender, args)
            "listworlds" -> handleListWorlds(sender)
            else -> sendUsageMessage(sender)
        }

        return true
    }

    private fun sendUsageMessage(sender: CommandSender) {
        plugin.messageUtils.sendMessage(sender, "world-management.usage-main")
        plugin.messageUtils.sendMessage(sender, "world-management.usage-main-line1")
        plugin.messageUtils.sendMessage(sender, "world-management.usage-main-line2")
        plugin.messageUtils.sendMessage(sender, "world-management.usage-main-line3")
    }

    private fun saveWorlds(allowed: List<String>, disabled: List<String>) {
        plugin.configManager.setStringList("worlds.allowed-worlds", allowed)
        plugin.configManager.setStringList("worlds.disabled-worlds", disabled)
        plugin.configManager.saveConfig()
    }

    private fun handleAddWorld(sender: CommandSender, args: Array<out String>) {
        val type = args.getOrNull(2)?.lowercase()
        if (type !in listTypes) {
            plugin.messageUtils.sendMessage(sender, "world-management.usage-add")
            return
        }

        val worldName = args[1]
        val world = plugin.server.getWorld(worldName)
        if (world == null) {
            plugin.messageUtils.sendMessage(sender, "world-management.world-not-found", "WORLD" to worldName)
            return
        }

        val allowedWorlds = plugin.configManager.getStringList("worlds.allowed-worlds").toMutableList()
        val disabledWorlds = plugin.configManager.getStringList("worlds.disabled-worlds").toMutableList()
        val allowing = type == "allowed"

        if (allowing && worldName in allowedWorlds) {
            plugin.messageUtils.sendMessage(sender, "world-management.already-in-allowed", "WORLD" to worldName)
            return
        }
        if (!allowing && worldName in disabledWorlds) {
            plugin.messageUtils.sendMessage(sender, "world-management.already-in-disabled", "WORLD" to worldName)
            return
        }

        allowedWorlds.remove(worldName)
        disabledWorlds.remove(worldName)

        if (allowing) {
            allowedWorlds.add(worldName)
            plugin.messageUtils.sendMessage(sender, "world-management.world-added-allowed", "WORLD" to worldName)
        } else {
            disabledWorlds.add(worldName)
            plugin.messageUtils.sendMessage(sender, "world-management.world-added-disallowed", "WORLD" to worldName)
        }

        saveWorlds(allowedWorlds, disabledWorlds)

        if (!allowing) {
            world.players
                .filter { plugin.flightManager.hasFlightEnabled(it) }
                .forEach {
                    plugin.flightManager.disableFlight(it)
                    plugin.messageUtils.sendMessage(it, "errors.world-not-allowed")
                }
        }
    }

    private fun handleRemoveWorld(sender: CommandSender, args: Array<out String>) {
        if (args.size < 2) {
            plugin.messageUtils.sendMessage(sender, "world-management.usage-remove")
            return
        }

        val worldName = args[1]
        val allowedWorlds = plugin.configManager.getStringList("worlds.allowed-worlds").toMutableList()
        val disabledWorlds = plugin.configManager.getStringList("worlds.disabled-worlds").toMutableList()

        if (worldName !in allowedWorlds && worldName !in disabledWorlds) {
            plugin.messageUtils.sendMessage(sender, "world-management.world-not-in-list", "WORLD" to worldName)
            return
        }

        allowedWorlds.remove(worldName)
        disabledWorlds.remove(worldName)
        saveWorlds(allowedWorlds, disabledWorlds)

        plugin.messageUtils.sendMessage(sender, "world-management.world-removed", "WORLD" to worldName)
    }

    private fun handleListWorlds(sender: CommandSender) {
        val allowedWorlds = plugin.configManager.getStringList("worlds.allowed-worlds")
        val disabledWorlds = plugin.configManager.getStringList("worlds.disabled-worlds")

        plugin.messageUtils.sendMessage(sender, "world-management.list-header")

        if (allowedWorlds.isEmpty()) {
            plugin.messageUtils.sendMessage(sender, "world-management.all-worlds-allowed")
        } else {
            sendWorldList(sender, "world-management.allowed-worlds", allowedWorlds)
        }

        if (disabledWorlds.isNotEmpty()) {
            sendWorldList(sender, "world-management.disabled-worlds", disabledWorlds)
        }
    }

    private fun sendWorldList(sender: CommandSender, headerPath: String, worlds: List<String>) {
        plugin.messageUtils.sendMessage(sender, headerPath, "COUNT" to worlds.size.toString())
        worlds.forEach { plugin.messageUtils.sendMessage(sender, "world-management.world-entry", "WORLD" to it) }
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): List<String> {
        if (!hasPermission(sender, "permissions.admin", "flys.admin")) return emptyList()

        return when (args.size) {
            1 -> subCommands.startingWith(args[0]).sorted()
            2 -> if (args[0].lowercase() in listOf("addworld", "removeworld")) {
                plugin.server.worlds.map { it.name }.startingWith(args[1]).sorted()
            } else {
                emptyList()
            }
            3 -> if (args[0].lowercase() == "addworld") listTypes.startingWith(args[2]) else emptyList()
            else -> emptyList()
        }
    }
}
