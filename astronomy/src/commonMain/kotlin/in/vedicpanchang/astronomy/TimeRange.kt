package `in`.vedicpanchang.astronomy

import kotlin.time.Instant

/** Equivalent of Flutter's DateTimeRange — a closed interval between two instants. */
data class TimeRange(val start: Instant, val end: Instant)

/** Auspicious/inauspicious period with an id key and time window. */
data class MuhurtaPeriod(val id: String, val range: TimeRange)
