package com.talhanation.bannermod.settlement.prefab.validation;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidationResultTest {

    @Test
    void scoreClampsToZeroAtMinimum() {
        ValidationResult result = new ValidationResult(true, -50, List.of());
        assertEquals(0, result.architectureScore());
    }

    @Test
    void scoreClampsToHundredAtMaximum() {
        ValidationResult result = new ValidationResult(true, 250, List.of());
        assertEquals(100, result.architectureScore());
    }

    @Test
    void issuesAreGroupedBySeverity() {
        ValidationResult result = new ValidationResult(true, 50, List.of(
                ValidationIssue.info("ok"),
                ValidationIssue.minor("small"),
                ValidationIssue.major("big"),
                ValidationIssue.blocker("fatal")
        ));
        assertEquals(1, result.count(ValidationIssue.Severity.INFO));
        assertEquals(1, result.count(ValidationIssue.Severity.MINOR));
        assertEquals(1, result.count(ValidationIssue.Severity.MAJOR));
        assertEquals(1, result.count(ValidationIssue.Severity.BLOCKER));
    }

    @Test
    void blockedBuildReportsNotPassed() {
        ValidationResult blocked = new ValidationResult(false, 20, List.of(ValidationIssue.blocker("x")));
        assertFalse(blocked.passed());
    }

    @Test
    void passingBuildReportsPassed() {
        ValidationResult ok = new ValidationResult(true, 80, List.of(ValidationIssue.info("x")));
        assertTrue(ok.passed());
        assertEquals(ArchitectureTier.GREAT, ok.architectureTier());
    }
}
