package com.talhanation.bannermod.war.registry;

import java.util.Collection;
import java.util.Locale;
import java.util.UUID;

public final class PoliticalRegistryValidation {
    public static final int MIN_NAME_LENGTH = 3;
    public static final int MAX_NAME_LENGTH = 32;

    private PoliticalRegistryValidation() {
    }

    public static Result validateCreate(String name, UUID leaderUuid, Collection<PoliticalEntityRecord> existing) {
        String normalized = normalizeName(name);
        if (normalized.length() < MIN_NAME_LENGTH) {
            return Result.invalid("name_too_short");
        }
        if (normalized.length() > MAX_NAME_LENGTH) {
            return Result.invalid("name_too_long");
        }
        if (leaderUuid == null) {
            return Result.invalid("missing_leader");
        }
        for (PoliticalEntityRecord record : existing) {
            if (record.name().equalsIgnoreCase(normalized)) {
                return Result.invalid("duplicate_name");
            }
        }
        return Result.ok();
    }

    /**
     * Validate a rename of {@code currentEntityId} to {@code newName} against the existing
     * registry. Identical to {@link #validateCreate} but ignores any duplicate match for the
     * entity being renamed (so a leader can re-issue the same name as a no-op safety) and
     * does not require a leader uuid.
     */
    public static Result validateRename(String newName,
                                        UUID currentEntityId,
                                        Collection<PoliticalEntityRecord> existing) {
        String normalized = normalizeName(newName);
        if (normalized.length() < MIN_NAME_LENGTH) {
            return Result.invalid("name_too_short");
        }
        if (normalized.length() > MAX_NAME_LENGTH) {
            return Result.invalid("name_too_long");
        }
        for (PoliticalEntityRecord record : existing) {
            if (currentEntityId != null && currentEntityId.equals(record.id())) {
                continue;
            }
            if (record.name().equalsIgnoreCase(normalized)) {
                return Result.invalid("duplicate_name");
            }
        }
        return Result.ok();
    }

    public static String normalizeName(String name) {
        return name == null ? "" : name.trim().replaceAll("\\s+", " ");
    }

    public static String slug(String name) {
        return normalizeName(name).toLowerCase(Locale.ROOT).replace(' ', '_');
    }

    public record Result(boolean valid, String reason) {
        public static Result ok() {
            return new Result(true, "");
        }

        public static Result invalid(String reason) {
            return new Result(false, reason);
        }
    }
}
