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
