package hasab.runtime.datetime

import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.ZoneOffset

/**
 * Date/time utilities wrapping java.time API for common date and time operations.
 */
public object HsDateTime {

    private val isoDateTime: DateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME
    private val isoDate: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val isoTime: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_TIME

    public fun now(): String = ZonedDateTime.now(ZoneOffset.UTC).format(isoDateTime)

    public fun today(): String = LocalDate.now().format(isoDate)

    public fun currentTimeMillis(): Long = System.currentTimeMillis()

    public fun nanoTime(): Long = System.nanoTime()

    public fun parseDateTime(dateTimeStr: String): Long =
        ZonedDateTime.parse(dateTimeStr).toInstant().toEpochMilli()

    public fun parseDate(dateStr: String): String = LocalDate.parse(dateStr).format(isoDate)

    public fun parseTime(timeStr: String): String = LocalTime.parse(timeStr).format(isoTime)

    public fun formatDateTime(millis: Long, pattern: String): String {
        val instant = Instant.ofEpochMilli(millis)
        val zdt = ZonedDateTime.ofInstant(instant, ZoneOffset.UTC)
        return zdt.format(DateTimeFormatter.ofPattern(pattern))
    }

    public fun formatDate(millis: Long, pattern: String): String {
        val instant = Instant.ofEpochMilli(millis)
        val date = instant.atZone(ZoneOffset.UTC).toLocalDate()
        return date.format(DateTimeFormatter.ofPattern(pattern))
    }

    public fun formatTime(millis: Long, pattern: String): String {
        val instant = Instant.ofEpochMilli(millis)
        val time = instant.atZone(ZoneOffset.UTC).toLocalTime()
        return time.format(DateTimeFormatter.ofPattern(pattern))
    }

    public fun year(dateTimeStr: String): Int = ZonedDateTime.parse(dateTimeStr).year

    public fun month(dateTimeStr: String): Int = ZonedDateTime.parse(dateTimeStr).monthValue

    public fun day(dateTimeStr: String): Int = ZonedDateTime.parse(dateTimeStr).dayOfMonth

    public fun hour(dateTimeStr: String): Int = ZonedDateTime.parse(dateTimeStr).hour

    public fun minute(dateTimeStr: String): Int = ZonedDateTime.parse(dateTimeStr).minute

    public fun second(dateTimeStr: String): Int = ZonedDateTime.parse(dateTimeStr).second

    public fun dayOfWeek(dateTimeStr: String): String = ZonedDateTime.parse(dateTimeStr).dayOfWeek.toString()

    public fun dayOfYear(dateTimeStr: String): Int = ZonedDateTime.parse(dateTimeStr).dayOfYear

    public fun isLeapYear(year: Int): Boolean = Year.isLeap(year.toLong())

    public fun daysInMonth(year: Int, month: Int): Int = YearMonth.of(year, month).lengthOfMonth()

    public fun daysInYear(year: Int): Int = if (isLeapYear(year)) 366 else 365

    public fun addDays(dateTimeStr: String, days: Int): String {
        val zdt = ZonedDateTime.parse(dateTimeStr)
        return zdt.plusDays(days.toLong()).format(isoDateTime)
    }

    public fun addHours(dateTimeStr: String, hours: Int): String {
        val zdt = ZonedDateTime.parse(dateTimeStr)
        return zdt.plusHours(hours.toLong()).format(isoDateTime)
    }

    public fun addMinutes(dateTimeStr: String, minutes: Int): String {
        val zdt = ZonedDateTime.parse(dateTimeStr)
        return zdt.plusMinutes(minutes.toLong()).format(isoDateTime)
    }

    public fun addSeconds(dateTimeStr: String, seconds: Int): String {
        val zdt = ZonedDateTime.parse(dateTimeStr)
        return zdt.plusSeconds(seconds.toLong()).format(isoDateTime)
    }

    public fun diffDays(from: String, to: String): Long {
        val fromInstant = ZonedDateTime.parse(from).toInstant()
        val toInstant = ZonedDateTime.parse(to).toInstant()
        return ChronoUnit.DAYS.between(fromInstant, toInstant)
    }

    public fun diffHours(from: String, to: String): Long {
        val fromInstant = ZonedDateTime.parse(from).toInstant()
        val toInstant = ZonedDateTime.parse(to).toInstant()
        return ChronoUnit.HOURS.between(fromInstant, toInstant)
    }

    public fun diffMinutes(from: String, to: String): Long {
        val fromInstant = ZonedDateTime.parse(from).toInstant()
        val toInstant = ZonedDateTime.parse(to).toInstant()
        return ChronoUnit.MINUTES.between(fromInstant, toInstant)
    }

    public fun diffSeconds(from: String, to: String): Long {
        val fromInstant = ZonedDateTime.parse(from).toInstant()
        val toInstant = ZonedDateTime.parse(to).toInstant()
        return ChronoUnit.SECONDS.between(fromInstant, toInstant)
    }

    public fun toEpochMillis(dateTimeStr: String): Long =
        ZonedDateTime.parse(dateTimeStr).toInstant().toEpochMilli()

    public fun fromEpochMillis(millis: Long): String =
        Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).format(isoDateTime)

    public fun toUtc(dateTimeStr: String): String {
        val zdt = ZonedDateTime.parse(dateTimeStr)
        return zdt.withZoneSameInstant(ZoneOffset.UTC).format(isoDateTime)
    }

    public fun toLocal(dateTimeStr: String): String {
        val zdt = ZonedDateTime.parse(dateTimeStr)
        return zdt.toLocalDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    }

    public fun timeZoneIds(): List<String> = ZoneId.getAvailableZoneIds().sorted()

    public fun withTimeZone(dateTimeStr: String, zoneId: String): String {
        val zdt = ZonedDateTime.parse(dateTimeStr)
        return zdt.withZoneSameInstant(ZoneId.of(zoneId)).format(isoDateTime)
    }

    public fun age(birthDate: String): Int {
        val birth = LocalDate.parse(birthDate)
        val today = LocalDate.now()
        var age = today.year - birth.year
        if (today.dayOfYear < birth.dayOfYear) {
            age--
        }
        return age
    }
}
