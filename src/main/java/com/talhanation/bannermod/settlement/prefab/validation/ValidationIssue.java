package com.talhanation.bannermod.settlement.prefab.validation;

import java.util.Objects;

/**
 * One reason a candidate build passed, failed, or scored the way it did. Each issue has
 * a severity so the UI can colour or filter them; {@link Severity#BLOCKER} entries fail
 * the build regardless of score.
 */
public record ValidationIssue(Severity severity, String translationKey, Object[] args) {
    public ValidationIssue {
        Objects.requireNonNull(severity, "severity");
        Objects.requireNonNull(translationKey, "translationKey");
        args = args == null ? new Object[0] : args.clone();
    }

    public enum Severity {
        INFO,
        MINOR,
        MAJOR,
        BLOCKER
    }

    public static ValidationIssue info(String key, Object... args) {
        return new ValidationIssue(Severity.INFO, key, args);
    }

    public static ValidationIssue minor(String key, Object... args) {
        return new ValidationIssue(Severity.MINOR, key, args);
    }

    public static ValidationIssue major(String key, Object... args) {
        return new ValidationIssue(Severity.MAJOR, key, args);
    }

    public static ValidationIssue blocker(String key, Object... args) {
        return new ValidationIssue(Severity.BLOCKER, key, args);
    }
}
