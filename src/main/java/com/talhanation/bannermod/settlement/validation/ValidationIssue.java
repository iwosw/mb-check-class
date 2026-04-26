package com.talhanation.bannermod.settlement.validation;

public record ValidationIssue(
        String code,
        String message,
        ValidationSeverity severity
) {
    public ValidationIssue {
        code = code == null ? "unknown" : code;
        message = message == null ? "" : message;
        severity = severity == null ? ValidationSeverity.INFO : severity;
    }
}
