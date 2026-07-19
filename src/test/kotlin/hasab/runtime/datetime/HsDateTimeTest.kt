package hasab.runtime.datetime

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.ZoneOffset

public class HsDateTimeTest {

    private val sampleDateTime = "2024-06-15T10:30:00Z"
    private val sampleDate = "2024-06-15"

    // ── now, today, currentTimeMillis ──────────────────────────

    @Test
    public fun `now returns non-empty string`() {
        val result = HsDateTime.now()
        assertTrue(result.isNotEmpty())
    }

    @Test
    public fun `now contains T separator`() {
        val result = HsDateTime.now()
        assertTrue(result.contains("T"))
    }

    @Test
    public fun `now ends with Z for UTC`() {
        val result = HsDateTime.now()
        assertTrue(result.endsWith("Z"))
    }

    @Test
    public fun `today format is YYYY-MM-DD`() {
        val result = HsDateTime.today()
        assertTrue(result.matches(Regex("\\d{4}-\\d{2}-\\d{2}")))
    }

    @Test
    public fun `today matches local date`() {
        val expected = LocalDate.now().toString()
        assertEquals(expected, HsDateTime.today())
    }

    @Test
    public fun `currentTimeMillis returns reasonable value`() {
        val now = HsDateTime.currentTimeMillis()
        assertTrue(now > 1_000_000_000_000L)
    }

    @Test
    public fun `currentTimeMillis increases over time`() {
        val t1 = HsDateTime.currentTimeMillis()
        val t2 = HsDateTime.currentTimeMillis()
        assertTrue(t2 >= t1)
    }

    @Test
    public fun `nanoTime returns positive value`() {
        assertTrue(HsDateTime.nanoTime() > 0)
    }

    // ── parseDate, parseTime ───────────────────────────────────

    @Test
    public fun `parseDate returns normalized form`() {
        assertEquals("2024-06-15", HsDateTime.parseDate("2024-06-15"))
    }

    @Test
    public fun `parseTime returns normalized form`() {
        assertEquals("10:30:00", HsDateTime.parseTime("10:30:00"))
    }

    @Test
    public fun `parseDate rejects invalid`() {
        assertFailsWith<Exception> {
            HsDateTime.parseDate("not-a-date")
        }
    }

    @Test
    public fun `parseTime rejects invalid`() {
        assertFailsWith<Exception> {
            HsDateTime.parseTime("25:00:00")
        }
    }

    // ── year, month, day extraction ────────────────────────────

    @Test
    public fun `year extracts correctly`() {
        assertEquals(2024, HsDateTime.year(sampleDateTime))
    }

    @Test
    public fun `month extracts correctly`() {
        assertEquals(6, HsDateTime.month(sampleDateTime))
    }

    @Test
    public fun `day extracts correctly`() {
        assertEquals(15, HsDateTime.day(sampleDateTime))
    }

    @Test
    public fun `hour extracts correctly`() {
        assertEquals(10, HsDateTime.hour(sampleDateTime))
    }

    @Test
    public fun `minute extracts correctly`() {
        assertEquals(30, HsDateTime.minute(sampleDateTime))
    }

    @Test
    public fun `second extracts correctly`() {
        assertEquals(0, HsDateTime.second(sampleDateTime))
    }

    @Test
    public fun `dayOfWeek extracts correctly`() {
        assertEquals("SATURDAY", HsDateTime.dayOfWeek(sampleDateTime))
    }

    @Test
    public fun `dayOfYear extracts correctly`() {
        assertEquals(167, HsDateTime.dayOfYear(sampleDateTime))
    }

    // ── isLeapYear ─────────────────────────────────────────────

    @Test
    public fun `2024 is leap year`() {
        assertTrue(HsDateTime.isLeapYear(2024))
    }

    @Test
    public fun `2023 is not leap year`() {
        assertEquals(false, HsDateTime.isLeapYear(2023))
    }

    @Test
    public fun `2000 is leap year`() {
        assertTrue(HsDateTime.isLeapYear(2000))
    }

    @Test
    public fun `1900 is not leap year`() {
        assertEquals(false, HsDateTime.isLeapYear(1900))
    }

    @Test
    public fun `2100 is not leap year`() {
        assertEquals(false, HsDateTime.isLeapYear(2100))
    }

    // ── daysInMonth, daysInYear ────────────────────────────────

    @Test
    public fun `daysInMonth january`() {
        assertEquals(31, HsDateTime.daysInMonth(2024, 1))
    }

    @Test
    public fun `daysInMonth february leap year`() {
        assertEquals(29, HsDateTime.daysInMonth(2024, 2))
    }

    @Test
    public fun `daysInMonth february non leap year`() {
        assertEquals(28, HsDateTime.daysInMonth(2023, 2))
    }

    @Test
    public fun `daysInMonth april has 30`() {
        assertEquals(30, HsDateTime.daysInMonth(2024, 4))
    }

    @Test
    public fun `daysInYear leap year`() {
        assertEquals(366, HsDateTime.daysInYear(2024))
    }

    @Test
    public fun `daysInYear non leap year`() {
        assertEquals(365, HsDateTime.daysInYear(2023))
    }

    // ── addDays, addHours, addMinutes, addSeconds ──────────────

    @Test
    public fun `addDays advances date`() {
        val result = HsDateTime.addDays(sampleDateTime, 5)
        assertTrue(result.contains("2024-06-20"))
    }

    @Test
    public fun `addDays can go backwards`() {
        val result = HsDateTime.addDays(sampleDateTime, -5)
        assertTrue(result.contains("2024-06-10"))
    }

    @Test
    public fun `addDays wraps across months`() {
        val result = HsDateTime.addDays("2024-01-30T00:00:00Z", 2)
        assertTrue(result.contains("2024-02-01"))
    }

    @Test
    public fun `addHours advances hours`() {
        val result = HsDateTime.addHours(sampleDateTime, 3)
        assertTrue(result.contains("T13:30:00"))
    }

    @Test
    public fun `addHours wraps across days`() {
        val result = HsDateTime.addHours(sampleDateTime, 15)
        assertTrue(result.contains("2024-06-16"))
    }

    @Test
    public fun `addHours negative`() {
        val result = HsDateTime.addHours(sampleDateTime, -3)
        assertTrue(result.contains("T07:30:00"))
    }

    @Test
    public fun `addMinutes advances`() {
        val result = HsDateTime.addMinutes(sampleDateTime, 45)
        assertTrue(result.contains("T11:15:00"))
    }

    @Test
    public fun `addMinutes wraps across hours`() {
        val result = HsDateTime.addMinutes(sampleDateTime, 90)
        assertTrue(result.contains("T12:00:00"))
    }

    @Test
    public fun `addSeconds advances`() {
        val result = HsDateTime.addSeconds(sampleDateTime, 30)
        assertTrue(result.contains("T10:30:30"))
    }

    // ── diffDays, diffHours, diffMinutes, diffSeconds ──────────

    @Test
    public fun `diffDays same day`() {
        assertEquals(0L, HsDateTime.diffDays(sampleDateTime, sampleDateTime))
    }

    @Test
    public fun `diffDays across dates`() {
        val later = "2024-06-20T10:30:00Z"
        assertEquals(5L, HsDateTime.diffDays(sampleDateTime, later))
    }

    @Test
    public fun `diffDays negative when reversed`() {
        val later = "2024-06-20T10:30:00Z"
        assertEquals(-5L, HsDateTime.diffDays(later, sampleDateTime))
    }

    @Test
    public fun `diffHours same day`() {
        assertEquals(0L, HsDateTime.diffHours(sampleDateTime, sampleDateTime))
    }

    @Test
    public fun `diffHours across days`() {
        val later = "2024-06-16T10:30:00Z"
        assertEquals(24L, HsDateTime.diffHours(sampleDateTime, later))
    }

    @Test
    public fun `diffMinutes same instant`() {
        assertEquals(0L, HsDateTime.diffMinutes(sampleDateTime, sampleDateTime))
    }

    @Test
    public fun `diffMinutes across hours`() {
        val later = "2024-06-15T11:30:00Z"
        assertEquals(60L, HsDateTime.diffMinutes(sampleDateTime, later))
    }

    @Test
    public fun `diffSeconds same instant`() {
        assertEquals(0L, HsDateTime.diffSeconds(sampleDateTime, sampleDateTime))
    }

    @Test
    public fun `diffSeconds across minutes`() {
        val later = "2024-06-15T10:31:00Z"
        assertEquals(60L, HsDateTime.diffSeconds(sampleDateTime, later))
    }

    // ── fromEpochMillis, toEpochMillis round-trip ──────────────

    @Test
    public fun `epochMillis roundtrip`() {
        val millis = HsDateTime.toEpochMillis(sampleDateTime)
        val restored = HsDateTime.fromEpochMillis(millis)
        assertEquals(sampleDateTime, restored)
    }

    @Test
    public fun `fromEpochMillis known epoch`() {
        val result = HsDateTime.fromEpochMillis(0L)
        assertEquals("1970-01-01T00:00:00Z", result)
    }

    @Test
    public fun `toEpochMillis is positive for modern date`() {
        val millis = HsDateTime.toEpochMillis(sampleDateTime)
        assertTrue(millis > 0)
    }

    // ── formatDateTime, formatDate, formatTime ─────────────────

    @Test
    public fun `formatDate produces date part`() {
        val millis = HsDateTime.toEpochMillis(sampleDateTime)
        val formatted = HsDateTime.formatDate(millis, "yyyy-MM-dd")
        assertEquals("2024-06-15", formatted)
    }

    @Test
    public fun `formatTime produces time part`() {
        val millis = HsDateTime.toEpochMillis(sampleDateTime)
        val formatted = HsDateTime.formatTime(millis, "HH:mm:ss")
        assertEquals("10:30:00", formatted)
    }

    @Test
    public fun `formatDateTime produces full datetime`() {
        val millis = HsDateTime.toEpochMillis(sampleDateTime)
        val formatted = HsDateTime.formatDateTime(millis, "yyyy-MM-dd HH:mm:ss")
        assertEquals("2024-06-15 10:30:00", formatted)
    }

    // ── toUtc, toLocal ─────────────────────────────────────────

    @Test
    public fun `toUtc returns UTC timezone`() {
        val result = HsDateTime.toUtc(sampleDateTime)
        assertTrue(result.endsWith("Z"))
    }

    @Test
    public fun `toLocal strips timezone`() {
        val result = HsDateTime.toLocal(sampleDateTime)
        assertFalse(result.endsWith("Z"))
        assertTrue(result.startsWith("2024-06-15T"))
    }

    // ── withTimeZone ───────────────────────────────────────────

    @Test
    public fun `withTimeZone converts zone`() {
        val result = HsDateTime.withTimeZone(sampleDateTime, "America/New_York")
        assertTrue(result.isNotEmpty())
    }

    // ── timeZoneIds ────────────────────────────────────────────

    @Test
    public fun `timeZoneIds returns non-empty list`() {
        val ids = HsDateTime.timeZoneIds()
        assertTrue(ids.isNotEmpty())
    }

    @Test
    public fun `timeZoneIds contains UTC`() {
        val ids = HsDateTime.timeZoneIds()
        assertTrue(ids.contains("UTC"))
    }

    // ── age ────────────────────────────────────────────────────

    @Test
    public fun `age calculation recent birth date`() {
        val today = LocalDate.now()
        val birthDate = today.minusYears(25).toString()
        assertEquals(25, HsDateTime.age(birthDate))
    }

    @Test
    public fun `age accounts for birthday not yet occurred this year`() {
        val today = LocalDate.now()
        val nextBirthday = today.withDayOfYear(today.dayOfYear + 30)
        val birthDate = nextBirthday.minusYears(20).toString()
        assertEquals(19, HsDateTime.age(birthDate))
    }

    @Test
    public fun `age of someone born today is zero`() {
        val today = LocalDate.now().toString()
        assertEquals(0, HsDateTime.age(today))
    }

    // ── parseDateTime ──────────────────────────────────────────

    @Test
    public fun `parseDateTime returns epoch millis`() {
        val millis = HsDateTime.parseDateTime(sampleDateTime)
        assertTrue(millis > 0)
    }

    @Test
    public fun `parseDateTime round-trips with fromEpochMillis`() {
        val millis = HsDateTime.parseDateTime(sampleDateTime)
        val result = HsDateTime.fromEpochMillis(millis)
        assertEquals(sampleDateTime, result)
    }

    @Test
    public fun `parseDateTime rejects invalid`() {
        assertFailsWith<Exception> {
            HsDateTime.parseDateTime("not-a-datetime")
        }
    }

    private fun assertFalse(value: Boolean) {
        assertEquals(false, value)
    }
}
