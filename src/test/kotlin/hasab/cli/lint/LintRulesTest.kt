package hasab.cli.lint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

public class LintRulesTest {

    @Test
    public fun `LintSeverity enum has all values`() {
        assertEquals(3, LintSeverity.values().size)
        assertTrue(LintSeverity.values().contains(LintSeverity.ERROR))
        assertTrue(LintSeverity.values().contains(LintSeverity.WARNING))
        assertTrue(LintSeverity.values().contains(LintSeverity.INFO))
    }

    @Test
    public fun `LintIssue data class works`() {
        val issue = LintIssue(
            line = 10,
            column = 5,
            severity = LintSeverity.ERROR,
            rule = "test-rule",
            message = "test message",
        )
        assertEquals(10, issue.line)
        assertEquals(5, issue.column)
        assertEquals(LintSeverity.ERROR, issue.severity)
        assertEquals("test-rule", issue.rule)
        assertEquals("test message", issue.message)
    }

    @Test
    public fun `LintResult hasIssues returns true when issues exist`() {
        val result = LintResult(
            file = "test.has",
            issues = listOf(
                LintIssue(1, 1, LintSeverity.WARNING, "w", "msg"),
            ),
        )
        assertTrue(result.hasIssues)
        assertEquals(1, result.warningCount)
        assertEquals(0, result.errorCount)
    }

    @Test
    public fun `LintResult hasIssues returns false when no issues`() {
        val result = LintResult("test.has", emptyList())
        assertTrue(!result.hasIssues)
    }
}
