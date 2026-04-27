package com.talhanation.bannermod.combat;

import java.util.UUID;

/**
 * Pure descriptor of a commander's aura source: their political entity (so the policy can
 * reject buffing enemies / neutral squads) and their world position. The radius is a property
 * of the policy, not the commander, since the aura strength is centrally configured.
 *
 * <p>{@code politicalEntityId} may be {@code null} for unaffiliated commanders (e.g. early-
 * game leaders without a state). In that case the policy treats the aura as inert: an
 * unaffiliated commander cannot project morale to a squad with a known political entity, and
 * the same goes the other way.
 */
public record CommanderAura(UUID politicalEntityId, double x, double y, double z) {
    public static CommanderAura at(UUID politicalEntityId, double x, double y, double z) {
        return new CommanderAura(politicalEntityId, x, y, z);
    }
}
