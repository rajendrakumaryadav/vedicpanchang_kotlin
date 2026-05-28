package `in`.vedicpanchang.astronomy

import kotlin.math.*

/**
 * Approximate heliocentric ecliptic longitudes for the seven classical Vedic planets
 * (excluding Sun and Moon) and the lunar nodes Rahu/Ketu.
 *
 * Method: Jean Meeus "Astronomical Algorithms" (2nd ed.) Chapter 33 —
 * low-precision planetary positions via mean orbital elements.
 * Accuracy: ~1–2° for outer planets, ~2–5° for inner planets.
 */
object PlanetaryPositions {

    private const val DEG2RAD = PI / 180.0
    private const val RAD2DEG = 180.0 / PI

    // ── Orbital elements at J2000.0 (Meeus Table 33.a) ───────────────────────
    private val MERCURY = OrbitalElements(252.250906, 149474.0722491, 0.20563175, 0.000020407, 0.387098, 7.004980, 48.330893, 77.456119)
    private val VENUS = OrbitalElements(181.979801, 58519.2130302, 0.00677323, -0.000047708, 0.723329, 3.394662, 76.679920, 131.563703)
    private val EARTH = OrbitalElements(100.466449, 36000.7698231, 0.01670862, -0.000042037, 1.000000, 0.0, 0.0, 102.937348)
    private val MARS = OrbitalElements(355.433275, 19141.6964746, 0.09340062, 0.000090441, 1.523679, 1.849726, 49.558093, 336.060234)
    private val JUPITER = OrbitalElements(34.351484, 3036.3027889, 0.04849485, 0.000163244, 5.202603, 1.303270, 100.464441, 14.331309)
    private val SATURN = OrbitalElements(50.077444, 1223.5110686, 0.05550825, -0.000346641, 9.554909, 2.488878, 113.665524, 93.057237)

    /**
     * Tropical geocentric ecliptic longitudes [0, 360) for all classical planets.
     * Returns a map keyed by planet name: Mercury, Venus, Mars, Jupiter, Saturn, Rahu, Ketu.
     */
    fun tropicalLongitudes(jd: Double, sunLongitudeTropical: Double): Map<String, Double> {
        val t = (jd - 2451545.0) / 36525.0
        val earthPos = heliocentric(EARTH, t)
        return mapOf(
            "Mercury" to geocentricLongitude(heliocentric(MERCURY, t), earthPos),
            "Venus" to geocentricLongitude(heliocentric(VENUS, t), earthPos),
            "Mars" to geocentricLongitude(heliocentric(MARS, t), earthPos),
            "Jupiter" to geocentricLongitude(heliocentric(JUPITER, t), earthPos),
            "Saturn" to geocentricLongitude(heliocentric(SATURN, t), earthPos),
            "Rahu" to rahuLongitude(t),
            "Ketu" to normalize(rahuLongitude(t) + 180.0)
        )
    }

    /** Returns true if the planet is retrograde at jd. Rahu/Ketu always return true. */
    fun isRetrograde(planetName: String, jd: Double): Boolean {
        if (planetName == "Rahu" || planetName == "Ketu") return true
        val step = 1.0 / 24.0
        val lonNow = planetTropicalLongitude(planetName, jd)
        val lonNext = planetTropicalLongitude(planetName, jd + step)
        var diff = lonNext - lonNow
        if (diff > 180.0) diff -= 360.0
        if (diff < -180.0) diff += 360.0
        return diff < 0.0
    }

    private fun planetTropicalLongitude(name: String, jd: Double): Double {
        val t = (jd - 2451545.0) / 36525.0
        val earthPos = heliocentric(EARTH, t)
        val el = when (name) {
            "Mercury" -> MERCURY; "Venus" -> VENUS; "Mars" -> MARS
            "Jupiter" -> JUPITER; "Saturn" -> SATURN; else -> return 0.0
        }
        return geocentricLongitude(heliocentric(el, t), earthPos)
    }

    private fun equationOfCenter(mDeg: Double, e: Double): Double {
        val mRad = mDeg * DEG2RAD
        return RAD2DEG * (
            (2.0 * e - e * e * e / 4.0) * sin(mRad) +
            (5.0 * e * e / 4.0) * sin(2.0 * mRad) +
            (13.0 * e * e * e / 12.0) * sin(3.0 * mRad)
        )
    }

    private data class Vec3(val x: Double, val y: Double, val z: Double)

    private fun heliocentric(el: OrbitalElements, t: Double): Vec3 {
        val l = normalize(el.l0 + el.l1 * t)
        val e = el.e0 + el.e1 * t
        val mDeg = normalize(l - el.omega)
        val vDeg = normalize(mDeg + equationOfCenter(mDeg, e))
        val r = el.a * (1.0 - e * e) / (1.0 + e * cos(vDeg * DEG2RAD))
        val argPeri = el.omega - el.bigOmega
        val uRad = (argPeri + vDeg) * DEG2RAD
        val bigOmegaRad = el.bigOmega * DEG2RAD
        val iRad = el.i * DEG2RAD
        return Vec3(
            r * (cos(bigOmegaRad) * cos(uRad) - sin(bigOmegaRad) * sin(uRad) * cos(iRad)),
            r * (sin(bigOmegaRad) * cos(uRad) + cos(bigOmegaRad) * sin(uRad) * cos(iRad)),
            r * (sin(uRad) * sin(iRad))
        )
    }

    private fun geocentricLongitude(planet: Vec3, earth: Vec3): Double =
        normalize(atan2(planet.y - earth.y, planet.x - earth.x) * RAD2DEG)

    private fun rahuLongitude(t: Double): Double =
        normalize(125.04452 - 1934.136261 * t)

    private fun normalize(angle: Double): Double {
        var r = angle % 360.0
        if (r < 0.0) r += 360.0
        return r
    }
}

private data class OrbitalElements(
    val l0: Double, val l1: Double,
    val e0: Double, val e1: Double,
    val a: Double, val i: Double,
    val bigOmega: Double, val omega: Double
)
