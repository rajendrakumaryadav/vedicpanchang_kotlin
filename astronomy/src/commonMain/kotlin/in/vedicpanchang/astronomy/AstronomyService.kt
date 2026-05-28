package `in`.vedicpanchang.astronomy

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.*

// Result types for rise/set pairs
data class SunriseSunset(val sunrise: Instant, val sunset: Instant)
data class MoonriseMoonset(val moonrise: Instant, val moonset: Instant)
private data class EclipticPosition(val longitude: Double, val latitude: Double)
private data class EquatorialPosition(val rightAscensionDeg: Double, val declinationDeg: Double)

/**
 * Core astronomical calculation engine for Vedic Panchang.
 * Based on Jean Meeus "Astronomical Algorithms" (2nd Ed.) and Vedic adaptations.
 *
 * Not thread-safe — callers should confine to a single coroutine/thread.
 */
object AstronomyService {

    private const val MAX_RISE_SET_CACHE = 512
    private const val MAX_JD_CACHE = 4096

    private const val DEG2RAD = PI / 180.0
    private const val RAD2DEG = 180.0 / PI
    private const val SUN_RISE_SET_ALT = 0.0
    private const val MOON_RISE_SET_ALT = 0.30

    private val sunriseSunsetCache = LinkedHashMap<String, SunriseSunset>()
    private val moonriseMoonsetCache = LinkedHashMap<String, MoonriseMoonset>()
    private val sunLongitudeCache = LinkedHashMap<String, Double>()
    private val lahiriAyanamshaCache = LinkedHashMap<String, Double>()
    private val sunLongitudeSiderealCache = LinkedHashMap<String, Double>()
    private val moonEclipticPositionCache = LinkedHashMap<String, EclipticPosition>()
    private val moonLongitudeCache = LinkedHashMap<String, Double>()
    private val moonLongitudeSiderealCache = LinkedHashMap<String, Double>()

    // ─── Julian Day ───────────────────────────────────────────────────────────

    fun julianDay(year: Int, month: Int, day: Int, ut: Double): Double {
        var y = year
        var m = month
        if (m <= 2) { y -= 1; m += 12 }
        val a = floor(y.toDouble() / 100.0).toInt()
        val b = 2 - a + floor(a.toDouble() / 4.0).toInt()
        return floor(365.25 * (y + 4716)) +
                floor(30.6001 * (m + 1)) +
                day + ut / 24.0 + b - 1524.5
    }

    fun julianDayFromInstant(instant: Instant): Double {
        val dt = instant.toLocalDateTime(TimeZone.UTC)
        val ut = dt.hour + dt.minute / 60.0 + dt.second / 3600.0
        return julianDay(dt.year, dt.month.ordinal + 1, dt.day, ut)
    }

    fun julianCenturies(jd: Double): Double = (jd - 2451545.0) / 36525.0

    // ─── Sun Position ─────────────────────────────────────────────────────────

    fun sunLongitude(jd: Double): Double {
        val key = jdKey(jd)
        readLru(sunLongitudeCache, key)?.let { return it }

        val t = julianCenturies(jd)
        var l0 = 280.46646 + 36000.76983 * t + 0.0003032 * t * t
        l0 = normalizeAngle(l0)

        var mAnomaly = 357.52911 + 35999.05029 * t - 0.0001537 * t * t
        mAnomaly = normalizeAngle(mAnomaly)
        val mRad = mAnomaly * DEG2RAD

        val centerEq =
            (1.914602 - 0.004817 * t - 0.000014 * t * t) * sin(mRad) +
            (0.019993 - 0.000101 * t) * sin(2 * mRad) +
            0.000289 * sin(3 * mRad)

        val sunTrueLon = l0 + centerEq
        val omega = 125.04 - 1934.136 * t
        val apparent = sunTrueLon - 0.00569 - 0.00478 * sin(omega * DEG2RAD)

        val result = normalizeAngle(apparent)
        writeLru(sunLongitudeCache, key, result, MAX_JD_CACHE)
        return result
    }

    fun lahiriAyanamsha(jd: Double): Double {
        val key = jdKey(jd)
        readLru(lahiriAyanamshaCache, key)?.let { return it }

        // Lahiri ayanamsha value for J2000.0 is approximately 23.85°
        // Annual precession is ~50.27" per year
        val ayan = 23.85 + (50.27 / 3600.0) * (jd - 2451545.0) / 365.25
        
        val result = normalizeAngle(ayan)
        writeLru(lahiriAyanamshaCache, key, result, MAX_JD_CACHE)
        return result
    }

    fun siderealLongitude(tropicalLongitude: Double, jd: Double): Double =
        normalizeAngle(tropicalLongitude - lahiriAyanamsha(jd))

    fun sunLongitudeSidereal(jd: Double): Double {
        val key = jdKey(jd)
        readLru(sunLongitudeSiderealCache, key)?.let { return it }
        val result = siderealLongitude(sunLongitude(jd), jd)
        writeLru(sunLongitudeSiderealCache, key, result, MAX_JD_CACHE)
        return result
    }

    // ─── Moon Position ────────────────────────────────────────────────────────

    fun moonLongitude(jd: Double): Double {
        val key = jdKey(jd)
        readLru(moonLongitudeCache, key)?.let { return it }
        val result = moonEclipticPosition(jd).longitude
        writeLru(moonLongitudeCache, key, result, MAX_JD_CACHE)
        return result
    }

    fun moonLongitudeSidereal(jd: Double): Double {
        val key = jdKey(jd)
        readLru(moonLongitudeSiderealCache, key)?.let { return it }
        val result = siderealLongitude(moonLongitude(jd), jd)
        writeLru(moonLongitudeSiderealCache, key, result, MAX_JD_CACHE)
        return result
    }

    // ─── Sunrise / Sunset ────────────────────────────────────────────────────

    fun sunriseSunset(date: LocalDate, lat: Double, lon: Double): SunriseSunset {
        val cacheKey = riseSetCacheKey(date, lat, lon)
        readLru(sunriseSunsetCache, cacheKey)?.let { return it }

        val altitudeCache = HashMap<String, Double>()
        fun sunAltitudeAt(jd: Double): Double =
            altitudeCache.getOrPut(jdKey(jd)) { sunAltitudeDegrees(jd, lat, lon) }

        val jdStart = julianDay(date.year, date.month.ordinal + 1, date.day, 0.0)
        var riseJd: Double? = null
        var setJd: Double? = null

        var prevJd = jdStart
        var prevAlt = sunAltitudeAt(prevJd)

        for (hour in 1..24) {
            val nextJd = jdStart + hour / 24.0
            val nextAlt = sunAltitudeAt(nextJd)
            val prevF = prevAlt - SUN_RISE_SET_ALT
            val nextF = nextAlt - SUN_RISE_SET_ALT

            if (prevF == 0.0) {
                if (nextAlt > prevAlt) riseJd = riseJd ?: prevJd
                else setJd = setJd ?: prevJd
            } else if (prevF * nextF < 0.0) {
                val eventJd = refineAltitudeCrossing(prevJd, nextJd, SUN_RISE_SET_ALT, ::sunAltitudeAt)
                if (nextAlt > prevAlt) riseJd = riseJd ?: eventJd
                else setJd = setJd ?: eventJd
            }

            prevJd = nextJd
            prevAlt = nextAlt
        }

        if (riseJd == null || setJd == null) {
            val jdNoon = julianDay(date.year, date.month.ordinal + 1, date.day, 12.0)
            val solarNoonMins = solarNoonUtcMinutes(jdNoon, lon)
            val sunriseMins = sunEventUtcMinutes(jdNoon, lat, lon, true, solarNoonMins - 360.0)
            val sunsetMins = sunEventUtcMinutes(jdNoon, lat, lon, false, solarNoonMins + 360.0)

            var fallbackRise = instantFromDayAndMinutes(date, normalizeDayMinutes(sunriseMins))
            var fallbackSet = instantFromDayAndMinutes(date, normalizeDayMinutes(sunsetMins))
            if (!fallbackSet.isAfter(fallbackRise)) {
                fallbackSet = Instant.fromEpochMilliseconds(
                    fallbackSet.toEpochMilliseconds() + 86400_000L
                )
            }
            val result = SunriseSunset(roundToNearestMinute(fallbackRise), roundToNearestMinute(fallbackSet))
            writeLru(sunriseSunsetCache, cacheKey, result, MAX_RISE_SET_CACHE)
            return result
        }

        var sunrise = jdToInstant(riseJd)
        var sunset = jdToInstant(setJd)
        if (!sunset.isAfter(sunrise)) {
            sunset = Instant.fromEpochMilliseconds(sunset.toEpochMilliseconds() + 86400_000L)
        }
        val result = SunriseSunset(roundToNearestMinute(sunrise), roundToNearestMinute(sunset))
        writeLru(sunriseSunsetCache, cacheKey, result, MAX_RISE_SET_CACHE)
        return result
    }

    // ─── Moonrise / Moonset ──────────────────────────────────────────────────

    fun moonriseMoonset(date: LocalDate, lat: Double, lon: Double): MoonriseMoonset {
        val cacheKey = riseSetCacheKey(date, lat, lon)
        readLru(moonriseMoonsetCache, cacheKey)?.let { return it }

        val altitudeCache = HashMap<String, Double>()
        fun moonAltitudeAt(jd: Double): Double =
            altitudeCache.getOrPut(jdKey(jd)) { moonAltitudeDegrees(jd, lat, lon) }

        val jdStart = julianDay(date.year, date.monthNumber, date.dayOfMonth, 0.0)
        var riseJd: Double? = null
        var setJd: Double? = null

        var prevJd = jdStart
        var prevAlt = moonAltitudeAt(prevJd)

        for (hour in 1..24) {
            val nextJd = jdStart + hour / 24.0
            val nextAlt = moonAltitudeAt(nextJd)
            val prevF = prevAlt - MOON_RISE_SET_ALT
            val nextF = nextAlt - MOON_RISE_SET_ALT

            if (prevF == 0.0) {
                if (nextAlt > prevAlt) riseJd = riseJd ?: prevJd
                else setJd = setJd ?: prevJd
            } else if (prevF * nextF < 0.0) {
                val eventJd = refineAltitudeCrossing(prevJd, nextJd, MOON_RISE_SET_ALT, ::moonAltitudeAt)
                if (nextAlt > prevAlt) riseJd = riseJd ?: eventJd
                else setJd = setJd ?: eventJd
            }

            prevJd = nextJd
            prevAlt = nextAlt
        }

        // Fallback for polar/no-event days
        val resolvedRise = riseJd ?: (jdStart + 0.25)
        val resolvedSet = setJd ?: (jdStart + 0.75)

        val result = MoonriseMoonset(
            roundToNearestMinute(jdToInstant(resolvedRise)),
            roundToNearestMinute(jdToInstant(resolvedSet))
        )
        writeLru(moonriseMoonsetCache, cacheKey, result, MAX_RISE_SET_CACHE)
        return result
    }

    // ─── Moon Ecliptic Position (Meeus Table 47.A) ───────────────────────────

    private fun moonEclipticPosition(jd: Double): EclipticPosition {
        val key = jdKey(jd)
        readLru(moonEclipticPositionCache, key)?.let { return it }

        val t = julianCenturies(jd)
        val t2 = t * t
        val t3 = t2 * t
        val t4 = t3 * t

        val lPrime = normalizeAngle(218.3164477 + 481267.88123421 * t - 0.0015786 * t2 + t3 / 538841.0 - t4 / 65194000.0)
        val d = normalizeAngle(297.8501921 + 445267.1114034 * t - 0.0018819 * t2 + t3 / 545868.0 - t4 / 113065000.0)
        val m = normalizeAngle(357.5291092 + 35999.0502909 * t - 0.0001536 * t2 + t3 / 24490000.0)
        val mPrime = normalizeAngle(134.9633964 + 477198.8675055 * t + 0.0087414 * t2 + t3 / 69699.0 - t4 / 14712000.0)
        val f = normalizeAngle(93.2720950 + 483202.0175233 * t - 0.0036539 * t2 - t3 / 3526000.0 + t4 / 863310000.0)

        val a1 = normalizeAngle(119.75 + 131.849 * t)
        val a2 = normalizeAngle(53.09 + 479264.290 * t)
        val a3 = normalizeAngle(313.45 + 481266.484 * t)

        val dR = d * DEG2RAD; val mR = m * DEG2RAD; val mpR = mPrime * DEG2RAD
        val fR = f * DEG2RAD; val a1R = a1 * DEG2RAD; val a2R = a2 * DEG2RAD; val a3R = a3 * DEG2RAD

        val e = 1.0 - 0.002516 * t - 0.0000074 * t2
        val e2 = e * e

        var sigmaL = 0.0
        sigmaL += 6288774 * sin(mpR)
        sigmaL += 1274027 * sin(2 * dR - mpR)
        sigmaL += 658314 * sin(2 * dR)
        sigmaL += 213618 * sin(2 * mpR)
        sigmaL += -185116 * e * sin(mR)
        sigmaL += -114332 * sin(2 * fR)
        sigmaL += 58793 * sin(2 * dR - 2 * mpR)
        sigmaL += 57066 * e * sin(2 * dR - mR - mpR)
        sigmaL += 53322 * sin(2 * dR + mpR)
        sigmaL += 45758 * e * sin(2 * dR - mR)
        sigmaL += -40923 * e * sin(mR - mpR)
        sigmaL += -34720 * sin(dR)
        sigmaL += -30383 * e * sin(mR + mpR)
        sigmaL += 15327 * sin(2 * dR - 2 * fR)
        sigmaL += -12528 * sin(mpR + 2 * fR)
        sigmaL += 10980 * sin(mpR - 2 * fR)
        sigmaL += 10675 * sin(4 * dR - mpR)
        sigmaL += 10034 * sin(3 * mpR)
        sigmaL += 8548 * sin(4 * dR - 2 * mpR)
        sigmaL += -7888 * e * sin(2 * dR + mR - mpR)
        sigmaL += -6766 * e * sin(2 * dR + mR)
        sigmaL += -5163 * sin(dR - mpR)
        sigmaL += 4987 * e * sin(dR + mR)
        sigmaL += 4036 * e * sin(2 * dR - mR + mpR)
        sigmaL += 3994 * sin(2 * dR + 2 * mpR)
        sigmaL += 3861 * sin(4 * dR)
        sigmaL += 3665 * sin(2 * dR - 3 * mpR)
        sigmaL += -2689 * e * sin(mR - 2 * mpR)
        sigmaL += -2602 * sin(2 * dR - mpR + 2 * fR)
        sigmaL += 2390 * e * sin(2 * dR - mR - 2 * mpR)
        sigmaL += -2348 * sin(dR + mpR)
        sigmaL += 2236 * e2 * sin(2 * dR - 2 * mR)
        sigmaL += -2120 * e * sin(mR + 2 * mpR)
        sigmaL += -2069 * e2 * sin(2 * mR)
        sigmaL += 2048 * e2 * sin(2 * dR - 2 * mR - mpR)
        sigmaL += -1773 * sin(2 * dR + mpR - 2 * fR)
        sigmaL += -1595 * sin(2 * dR + 2 * fR)
        sigmaL += 1215 * e * sin(4 * dR - mR - mpR)
        sigmaL += -1110 * sin(2 * mpR + 2 * fR)
        sigmaL += -892 * sin(3 * dR - mpR)
        sigmaL += -810 * e * sin(2 * dR + mR + mpR)
        sigmaL += 759 * e * sin(4 * dR - mR - 2 * mpR)
        sigmaL += -713 * e2 * sin(2 * mR - mpR)
        sigmaL += -700 * e2 * sin(2 * dR + 2 * mR - mpR)
        sigmaL += 691 * e * sin(2 * dR + mR - 2 * mpR)
        sigmaL += 596 * e * sin(2 * dR - mR - 2 * fR)
        sigmaL += 549 * sin(4 * dR + mpR)
        sigmaL += 537 * sin(4 * mpR)
        sigmaL += 520 * e * sin(4 * dR - mR)
        sigmaL += -487 * sin(dR - 2 * mpR)
        sigmaL += -399 * e * sin(2 * dR + mR - 2 * fR)
        sigmaL += -381 * sin(2 * mpR - 2 * fR)
        sigmaL += 351 * e * sin(dR + mR + mpR)
        sigmaL += -340 * sin(3 * dR - 2 * mpR)
        sigmaL += 330 * sin(4 * dR - 3 * mpR)
        sigmaL += 327 * e * sin(2 * dR - mR + 2 * mpR)
        sigmaL += -323 * e2 * sin(2 * mR + mpR)
        sigmaL += 299 * e * sin(dR + mR - mpR)
        sigmaL += 294 * sin(2 * dR + 3 * mpR)
        // Meeus additional corrections (eq. 47.b)
        sigmaL += 3958 * sin(a1R) + 1962 * sin(lPrime * DEG2RAD - fR) + 318 * sin(a2R)

        val lon = lPrime + sigmaL / 1_000_000.0

        var sigmaB = 0.0
        sigmaB += 5128122 * sin(fR)
        sigmaB += 280602 * sin(mpR + fR)
        sigmaB += 277693 * sin(mpR - fR)
        sigmaB += 173237 * sin(2 * dR - fR)
        sigmaB += 55413 * sin(2 * dR - mpR + fR)
        sigmaB += 46271 * sin(2 * dR - mpR - fR)
        sigmaB += 32573 * sin(2 * dR + fR)
        sigmaB += 17198 * sin(2 * mpR + fR)
        sigmaB += 9266 * sin(2 * dR + mpR - fR)
        sigmaB += 8822 * sin(2 * mpR - fR)
        sigmaB += -8216 * e * sin(2 * dR - mR - fR)
        sigmaB += 4324 * sin(2 * dR - 2 * mpR - fR)
        sigmaB += 4200 * sin(2 * dR + mpR + fR)
        sigmaB += -3359 * e * sin(2 * dR + mR - fR)
        sigmaB += 2463 * e * sin(2 * dR - mR - mpR + fR)
        sigmaB += 2211 * e * sin(2 * dR - mR + fR)
        sigmaB += 2065 * e * sin(2 * dR - mR - mpR - fR)
        sigmaB += -1870 * e * sin(mR - mpR - fR)
        sigmaB += 1828 * sin(4 * dR - mpR - fR)
        sigmaB += -1794 * e * sin(mR + fR)
        sigmaB += -1749 * sin(3 * fR)
        sigmaB += -1565 * e * sin(mR - mpR + fR)
        sigmaB += -1491 * sin(dR + fR)
        sigmaB += -1475 * e * sin(mR + mpR + fR)
        sigmaB += -1410 * e * sin(mR + mpR - fR)
        sigmaB += -1344 * e * sin(mR - fR)
        sigmaB += -1335 * sin(dR - fR)
        sigmaB += 1107 * sin(3 * mpR + fR)
        sigmaB += 1021 * sin(4 * dR - fR)
        sigmaB += 833 * sin(4 * dR - mpR + fR)
        // Meeus additional latitude corrections
        sigmaB += -2235 * sin(lPrime * DEG2RAD) + 382 * sin(a3R) +
                175 * sin(a1R - fR) + 175 * sin(a1R + fR) +
                127 * sin(lPrime * DEG2RAD - mpR) - 115 * sin(lPrime * DEG2RAD + mpR)

        val lat = sigmaB / 1_000_000.0

        val result = EclipticPosition(normalizeAngle(lon), lat)
        writeLru(moonEclipticPositionCache, key, result, MAX_JD_CACHE)
        return result
    }

    // ─── NOAA-style helpers ───────────────────────────────────────────────────

    private fun equationOfTime(t: Double): Double {
        val epsilon = obliquityCorrection(t)
        val l0 = geomMeanLongSun(t)
        val ecc = eccentricityEarthOrbit(t)
        val mAnom = geomMeanAnomalySun(t)
        val y = tan((epsilon * DEG2RAD) / 2.0)
        val y2 = y * y
        val sin2l0 = sin(2.0 * l0 * DEG2RAD); val sinm = sin(mAnom * DEG2RAD)
        val cos2l0 = cos(2.0 * l0 * DEG2RAD); val sin4l0 = sin(4.0 * l0 * DEG2RAD)
        val sin2m = sin(2.0 * mAnom * DEG2RAD)
        val eTime = y2 * sin2l0 - 2.0 * ecc * sinm + 4.0 * ecc * y2 * sinm * cos2l0 -
                0.5 * y2 * y2 * sin4l0 - 1.25 * ecc * ecc * sin2m
        return RAD2DEG * eTime * 4.0
    }

    private fun geomMeanLongSun(t: Double) = normalizeAngle(280.46646 + t * (36000.76983 + t * 0.0003032))
    private fun geomMeanAnomalySun(t: Double) = 357.52911 + t * (35999.05029 - 0.0001537 * t)
    private fun eccentricityEarthOrbit(t: Double) = 0.016708634 - t * (0.000042037 + 0.0000001267 * t)
    private fun sunEquationOfCenter(t: Double): Double {
        val mRad = geomMeanAnomalySun(t) * DEG2RAD
        return sin(mRad) * (1.914602 - t * (0.004817 + 0.000014 * t)) +
                sin(2.0 * mRad) * (0.019993 - 0.000101 * t) +
                sin(3.0 * mRad) * 0.000289
    }
    private fun sunApparentLongitude(t: Double): Double {
        val trueLon = geomMeanLongSun(t) + sunEquationOfCenter(t)
        return trueLon - 0.00569 - 0.00478 * sin((125.04 - 1934.136 * t) * DEG2RAD)
    }
    private fun meanObliquityOfEcliptic(t: Double): Double {
        val sec = 21.448 - t * (46.815 + t * (0.00059 - t * 0.001813))
        return 23.0 + (26.0 + sec / 60.0) / 60.0
    }
    private fun obliquityCorrection(t: Double) =
        meanObliquityOfEcliptic(t) + 0.00256 * cos((125.04 - 1934.136 * t) * DEG2RAD)
    private fun sunDeclination(t: Double): Double {
        val sinT = sin(obliquityCorrection(t) * DEG2RAD) * sin(sunApparentLongitude(t) * DEG2RAD)
        return asin(sinT) * RAD2DEG
    }
    private fun solarNoonUtcMinutes(jd: Double, lon: Double): Double {
        val tNoon = julianCenturies(jd - lon / 360.0)
        return (720.0 - 4.0 * lon - equationOfTime(tNoon)).coerceIn(0.0, 1440.0)
    }
    private fun sunEventUtcMinutes(jd: Double, lat: Double, lon: Double, isSunrise: Boolean, approxMins: Double): Double {
        var approx = approxMins
        var t = julianCenturies(jd + approx / 1440.0)
        repeat(2) {
            val eqTime = equationOfTime(t)
            val solarDec = sunDeclination(t)
            val hourAngle = hourAngleSunrise(lat, solarDec)
            val delta = lon + if (isSunrise) hourAngle else -hourAngle
            val timeUtc = 720.0 - 4.0 * delta - eqTime
            t = julianCenturies(jd + timeUtc / 1440.0)
            approx = timeUtc
        }
        return approx
    }
    private fun hourAngleSunrise(lat: Double, solarDec: Double): Double {
        val haArg = (cos(90.833 * DEG2RAD) - sin(lat * DEG2RAD) * sin(solarDec * DEG2RAD)) /
                (cos(lat * DEG2RAD) * cos(solarDec * DEG2RAD))
        return acos(haArg.coerceIn(-1.0, 1.0)) * RAD2DEG
    }
    private fun normalizeDayMinutes(minutes: Double): Double {
        var n = minutes % 1440.0
        if (n < 0) n += 1440.0
        return n
    }

    // ─── Altitude calculations ────────────────────────────────────────────────

    private fun eclipticToEquatorial(lon: Double, lat: Double, jd: Double): EquatorialPosition {
        val epsilon = obliquityCorrection(julianCenturies(jd)) * DEG2RAD
        val lonR = lon * DEG2RAD; val latR = lat * DEG2RAD
        val ra = atan2(sin(lonR) * cos(epsilon) - tan(latR) * sin(epsilon), cos(lonR))
        val dec = asin(sin(latR) * cos(epsilon) + cos(latR) * sin(epsilon) * sin(lonR))
        return EquatorialPosition(normalizeAngle(ra * RAD2DEG), dec * RAD2DEG)
    }

    private fun greenwichMeanSiderealTime(jd: Double): Double {
        val t = (jd - 2451545.0) / 36525.0
        val theta = 280.46061837 + 360.98564736629 * (jd - 2451545.0) +
                0.000387933 * t * t - (t * t * t) / 38710000.0
        return normalizeAngle(theta)
    }

    private fun sunAltitudeDegrees(jd: Double, lat: Double, lon: Double): Double {
        val sunEq = eclipticToEquatorial(sunLongitude(jd), 0.0, jd)
        val lst = normalizeAngle(greenwichMeanSiderealTime(jd) + lon)
        val ha = normalizeSignedAngle(lst - sunEq.rightAscensionDeg) * DEG2RAD
        val alt = asin(sin(lat * DEG2RAD) * sin(sunEq.declinationDeg * DEG2RAD) +
                cos(lat * DEG2RAD) * cos(sunEq.declinationDeg * DEG2RAD) * cos(ha))
        return alt * RAD2DEG
    }

    private fun moonAltitudeDegrees(jd: Double, lat: Double, lon: Double): Double {
        val moonPos = moonEclipticPosition(jd)
        val moonEq = eclipticToEquatorial(moonPos.longitude, moonPos.latitude, jd)
        val lst = normalizeAngle(greenwichMeanSiderealTime(jd) + lon)
        val ha = normalizeSignedAngle(lst - moonEq.rightAscensionDeg) * DEG2RAD
        val alt = asin(sin(lat * DEG2RAD) * sin(moonEq.declinationDeg * DEG2RAD) +
                cos(lat * DEG2RAD) * cos(moonEq.declinationDeg * DEG2RAD) * cos(ha))
        return alt * RAD2DEG
    }

    // ─── Bisection refinement ─────────────────────────────────────────────────

    private fun refineAltitudeCrossing(
        jdLow: Double, jdHigh: Double, targetAlt: Double,
        altitudeAtJd: (Double) -> Double
    ): Double {
        var low = jdLow; var high = jdHigh
        var lowDiff = altitudeAtJd(low) - targetAlt
        repeat(24) {
            val mid = (low + high) / 2.0
            val midDiff = altitudeAtJd(mid) - targetAlt
            if (lowDiff == 0.0) return low
            if (lowDiff * midDiff <= 0.0) high = mid
            else { low = mid; lowDiff = midDiff }
        }
        return (low + high) / 2.0
    }

    // ─── Utility ──────────────────────────────────────────────────────────────

    fun normalizeAngle(angle: Double): Double {
        var r = angle % 360.0
        if (r < 0) r += 360.0
        return r
    }

    private fun normalizeSignedAngle(angle: Double): Double {
        var r = angle % 360.0
        if (r <= -180.0) r += 360.0
        if (r > 180.0) r -= 360.0
        return r
    }

    fun jdToInstant(jd: Double): Instant {
        val millis = ((jd - 2440587.5) * 86400000.0).roundToLong()
        return Instant.fromEpochMilliseconds(millis)
    }

    private fun roundToNearestMinute(instant: Instant): Instant {
        val ms = instant.toEpochMilliseconds()
        val dt = instant.toLocalDateTime(TimeZone.UTC)
        return Instant.fromEpochMilliseconds(ms - dt.second * 1000L - (ms % 1000L))
    }

    private fun instantFromDayAndMinutes(date: LocalDate, minutes: Double): Instant {
        val startMs = julianDay(date.year, date.month.ordinal + 1, date.day, 0.0)
            .let { jdToInstant(it).toEpochMilliseconds() }
        return Instant.fromEpochMilliseconds(startMs + (minutes * 60_000.0).toLong())
    }

    private fun jdKey(jd: Double): String = jd.toString()

    private fun riseSetCacheKey(date: LocalDate, lat: Double, lon: Double): String =
        "${date.year}-${date.month.ordinal + 1}-${date.day}|${"%.8f".format(lat)}|${"%.8f".format(lon)}"

    private fun <K, V> readLru(cache: LinkedHashMap<K, V>, key: K): V? {
        val value = cache.remove(key) ?: return null
        cache[key] = value
        return value
    }

    private fun <K, V> writeLru(cache: LinkedHashMap<K, V>, key: K, value: V, maxEntries: Int) {
        cache[key] = value
        if (cache.size > maxEntries) cache.remove(cache.keys.first())
    }
}

private fun Instant.isAfter(other: Instant): Boolean = this > other
