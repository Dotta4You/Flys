/*
 * ==========================================
 * Fly's Plugin v1.4.1
 * Made by Dötchen with <3
 * https://github.com/Dotta4You/Flys
 * ==========================================
 */

package de.doetchen.projects.utils

import de.doetchen.projects.Flys
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.entity.Player
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

object EffectUtils {

    private const val WING_PARTICLES = 15
    private const val SPARKLE_PARTICLES = 10
    private const val FALLING_PARTICLES = 12
    private const val RING_POINTS = 8

    private fun spawnParticle(
        location: Location,
        particle: Particle,
        count: Int,
        offsetX: Double,
        offsetY: Double,
        offsetZ: Double,
        speed: Double
    ) {
        val world = location.world ?: return

        try {
            world.spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, speed)
        } catch (_: Exception) {
        }
    }

    private fun offset(origin: Location, x: Double, y: Double, z: Double) =
        Location(origin.world, origin.x + x, origin.y + y, origin.z + z)

    private fun Player.playEffectSound(sound: String, volume: Float, pitch: Float) =
        playSound(location, sound, volume, pitch)

    fun playFlightEnabledEffects(player: Player, plugin: Flys) {
        val location = player.location

        if (plugin.configManager.getBoolean("general.enable-particles")) {
            spawnWingParticles(location)
            spawnSparkleParticles(location)
        }

        if (plugin.configManager.getBoolean("general.enable-sounds")) {
            player.playEffectSound("entity.ender_dragon.flap", 0.5f, 1.2f)
            player.playEffectSound("block.enchantment_table.use", 0.8f, 1.5f)
        }
    }

    fun playFlightDisabledEffects(player: Player, plugin: Flys) {
        val location = player.location

        if (plugin.configManager.getBoolean("general.enable-particles")) {
            spawnFallingParticles(location)
        }

        if (plugin.configManager.getBoolean("general.enable-sounds")) {
            player.playEffectSound("entity.bat.takeoff", 0.6f, 0.8f)
            player.playEffectSound("block.fire.extinguish", 0.4f, 1.0f)
        }
    }

    fun playSpeedChangeEffect(player: Player, plugin: Flys) {
        val location = player.location

        if (plugin.configManager.getBoolean("general.enable-particles")) {
            repeat(RING_POINTS) {
                val angle = it * 2 * PI / RING_POINTS
                val ringPoint = offset(location, cos(angle) * 1.5, 1.0, sin(angle) * 1.5)
                spawnParticle(ringPoint, Particle.ENCHANT, 3, 0.1, 0.1, 0.1, 0.0)
            }
        }

        if (plugin.configManager.getBoolean("general.enable-sounds")) {
            player.playEffectSound("block.note_block.chime", 0.7f, 1.3f)
        }
    }

    private fun spawnWingParticles(location: Location) {
        repeat(WING_PARTICLES) {
            val angle = Random.nextDouble(0.0, 2 * PI)
            val radius = Random.nextDouble(0.5, 2.0)
            val height = Random.nextDouble(-0.5, 1.5)

            val point = offset(location, cos(angle) * radius, height, sin(angle) * radius)

            spawnParticle(point, Particle.CLOUD, 1, 0.1, 0.1, 0.1, 0.02)
            spawnParticle(point, Particle.FIREWORK, 1, 0.0, 0.0, 0.0, 0.0)
        }
    }

    private fun spawnSparkleParticles(location: Location) {
        repeat(SPARKLE_PARTICLES) {
            val point = offset(
                location,
                Random.nextDouble(-1.5, 1.5),
                Random.nextDouble(0.0, 2.0),
                Random.nextDouble(-1.5, 1.5)
            )

            spawnParticle(point, Particle.ENCHANT, 2, 0.2, 0.2, 0.2, 0.0)
            spawnParticle(point, Particle.CRIT, 3, 0.3, 0.3, 0.3, 0.5)
        }
    }

    private fun spawnFallingParticles(location: Location) {
        repeat(FALLING_PARTICLES) {
            val point = offset(
                location,
                Random.nextDouble(-1.0, 1.0),
                Random.nextDouble(1.0, 2.5),
                Random.nextDouble(-1.0, 1.0)
            )

            spawnParticle(point, Particle.SMOKE, 2, 0.1, 0.1, 0.1, 0.02)
            spawnParticle(point, Particle.CLOUD, 1, 0.2, 0.2, 0.2, 0.0)
        }
    }
}
