/*
 * ==========================================
 * Fly's Plugin v1.0
 * Made by Dötchen with <3
 * https://github.com/Dotta4You/Flys
 * ==========================================
 */
package de.doetchen.projects.utils

import de.doetchen.projects.Flys
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Player
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

object EffectUtils {

    private const val WING_PARTICLES = 15
    private const val SPARKLE_PARTICLES = 10
    private const val FALLING_PARTICLES = 12

    private const val DRAGON_FLAP_VOLUME = 0.5f
    private const val DRAGON_FLAP_PITCH = 1.2f
    private const val ENCHANT_VOLUME = 0.8f
    private const val ENCHANT_PITCH = 1.5f
    private const val BAT_VOLUME = 0.6f
    private const val BAT_PITCH = 0.8f
    private const val FIRE_VOLUME = 0.4f
    private const val FIRE_PITCH = 1.0f

    private fun spawnParticle(location: Location, particle: Particle, count: Int,
                              offsetX: Double, offsetY: Double, offsetZ: Double, speed: Double) {
        val world = location.world ?: return

        try {
            world.spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, speed)
        } catch (_: Exception) {
        }
    }

    fun playFlightEnabledEffects(player: Player, plugin: Flys) {
        val location = player.location

        if (plugin.configManager.getBoolean("general.enable-particles")) {
            spawnWingParticles(location)
            spawnSparkleParticles(location)
        }

        if (plugin.configManager.getBoolean("general.enable-sounds")) {
            player.playSound(location, Sound.ENTITY_ENDER_DRAGON_FLAP, DRAGON_FLAP_VOLUME, DRAGON_FLAP_PITCH)
            player.playSound(location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, ENCHANT_VOLUME, ENCHANT_PITCH)
        }
    }

    fun playFlightDisabledEffects(player: Player, plugin: Flys) {
        val location = player.location

        if (plugin.configManager.getBoolean("general.enable-particles")) {
            spawnFallingParticles(location)
        }

        if (plugin.configManager.getBoolean("general.enable-sounds")) {
            player.playSound(location, Sound.ENTITY_BAT_TAKEOFF, BAT_VOLUME, BAT_PITCH)
            player.playSound(location, Sound.BLOCK_FIRE_EXTINGUISH, FIRE_VOLUME, FIRE_PITCH)
        }
    }

    fun playSpeedChangeEffect(player: Player, plugin: Flys) {
        val location = player.location

        if (plugin.configManager.getBoolean("general.enable-particles")) {
            repeat(8) {
                val angle = (it * 45.0) * Math.PI / 180.0
                val radius = 1.5
                val x = location.x + cos(angle) * radius
                val y = location.y + 1.0
                val z = location.z + sin(angle) * radius
                val particleLocation = Location(location.world, x, y, z)
                spawnParticle(particleLocation, Particle.ENCHANT, 3, 0.1, 0.1, 0.1, 0.0)
            }
        }

        if (plugin.configManager.getBoolean("general.enable-sounds")) {
            player.playSound(location, Sound.BLOCK_NOTE_BLOCK_CHIME, 0.7f, 1.3f)
        }
    }

    private fun spawnWingParticles(location: Location) {
        repeat(WING_PARTICLES) {
            val angle = Random.nextDouble(0.0, 2 * Math.PI)
            val radius = Random.nextDouble(0.5, 2.0)
            val height = Random.nextDouble(-0.5, 1.5)

            val x = location.x + cos(angle) * radius
            val y = location.y + height
            val z = location.z + sin(angle) * radius

            val particleLocation = Location(location.world, x, y, z)

            spawnParticle(particleLocation, Particle.CLOUD, 1, 0.1, 0.1, 0.1, 0.02)
            spawnParticle(particleLocation, Particle.FIREWORK, 1, 0.0, 0.0, 0.0, 0.0)
        }
    }

    private fun spawnSparkleParticles(location: Location) {
        repeat(SPARKLE_PARTICLES) {
            val x = location.x + Random.nextDouble(-1.5, 1.5)
            val y = location.y + Random.nextDouble(0.0, 2.0)
            val z = location.z + Random.nextDouble(-1.5, 1.5)

            val particleLocation = Location(location.world, x, y, z)

            spawnParticle(particleLocation, Particle.ENCHANT, 2, 0.2, 0.2, 0.2, 0.0)
            spawnParticle(particleLocation, Particle.CRIT, 3, 0.3, 0.3, 0.3, 0.5)
        }
    }

    private fun spawnFallingParticles(location: Location) {
        repeat(FALLING_PARTICLES) {
            val x = location.x + Random.nextDouble(-1.0, 1.0)
            val y = location.y + Random.nextDouble(1.0, 2.5)
            val z = location.z + Random.nextDouble(-1.0, 1.0)

            val particleLocation = Location(location.world, x, y, z)

            spawnParticle(particleLocation, Particle.SMOKE, 2, 0.1, 0.1, 0.1, 0.02)
            spawnParticle(particleLocation, Particle.CLOUD, 1, 0.2, 0.2, 0.2, 0.0)
        }
    }
}