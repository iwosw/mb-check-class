package com.talhanation.bannermod.settlement.prefab.validation;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Outcome of running a {@link BuildingValidator} against a player-built structure.
 *
 * <p>Three orthogonal pieces of information:</p>
 * <ul>
 *   <li>{@link #passed()} — the prefab's rule set is satisfied (no BLOCKER issues).</li>
 *   <li>{@link #architectureScore()} — 0..100, independent of rules, measuring how visually
 *       interesting the build is.</li>
 *   <li>{@link #issues()} — structured list of findings, each with a translation key so the
 *       chat/UI can render localised text.</li>
 * </ul>
 */
public record ValidationResult(
        boolean passed,
        int architectureScore,
        List<ValidationIssue> issues
) {
    public ValidationResult {
        architectureScore = Math.max(0, Math.min(100, architectureScore));
        issues = List.copyOf(issues == null ? List.of() : issues);
    }

    public ArchitectureTier architectureTier() {
        return ArchitectureTier.fromScore(this.architectureScore);
    }

    public static ValidationResult blocked(List<ValidationIssue> issues, int architectureScore) {
        return new ValidationResult(false, architectureScore, issues);
    }

    public static ValidationResult ok(List<ValidationIssue> issues, int architectureScore) {
        return new ValidationResult(true, architectureScore, issues);
    }

    public long count(ValidationIssue.Severity severity) {
        Objects.requireNonNull(severity, "severity");
        return issues.stream().filter(i -> i.severity() == severity).count();
    }

    public List<ValidationIssue> issuesOf(ValidationIssue.Severity severity) {
        Objects.requireNonNull(severity, "severity");
        return Collections.unmodifiableList(issues.stream().filter(i -> i.severity() == severity).toList());
    }
}
