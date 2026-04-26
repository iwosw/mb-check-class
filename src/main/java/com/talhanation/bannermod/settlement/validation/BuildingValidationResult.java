package com.talhanation.bannermod.settlement.validation;

import com.talhanation.bannermod.settlement.building.BuildingType;

import java.util.List;

public record BuildingValidationResult(
        boolean valid,
        BuildingType type,
        int capacity,
        int qualityScore,
        List<ValidationIssue> failures,
        List<ValidationIssue> warnings,
        ValidatedBuildingSnapshot snapshot
) {
    public BuildingValidationResult {
        type = type == null ? BuildingType.HOUSE : type;
        capacity = Math.max(0, capacity);
        qualityScore = Math.max(0, qualityScore);
        failures = List.copyOf(failures == null ? List.of() : failures);
        warnings = List.copyOf(warnings == null ? List.of() : warnings);
    }

    public static BuildingValidationResult blockingFailure(BuildingType type, String code, String message) {
        return new BuildingValidationResult(
                false,
                type,
                0,
                0,
                List.of(new ValidationIssue(code, message, ValidationSeverity.BLOCKING)),
                List.of(),
                null
        );
    }

    public static BuildingValidationResult success(BuildingType type,
                                                   int capacity,
                                                   int qualityScore,
                                                   List<ValidationIssue> warnings,
                                                   ValidatedBuildingSnapshot snapshot) {
        return new BuildingValidationResult(
                true,
                type,
                capacity,
                qualityScore,
                List.of(),
                warnings,
                snapshot
        );
    }
}
