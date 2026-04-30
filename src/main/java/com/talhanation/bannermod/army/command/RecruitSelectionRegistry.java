package com.talhanation.bannermod.army.command;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-player server-side set of currently-selected recruit UUIDs.
 *
 * <p>Selection is orthogonal to groups: a player can select any subset of
 * their owned recruits (via radius command today, drag-box UI tomorrow), and subsequent
 * commands target that subset instead of the broader group.</p>
 *
 * <p>The registry is in-memory only. Players who log out clear their entry on the server
 * disconnect hook.</p>
 */
public final class RecruitSelectionRegistry {
    private static final RecruitSelectionRegistry INSTANCE = new RecruitSelectionRegistry();

    private final Map<UUID, Set<UUID>> selectionByPlayer = new ConcurrentHashMap<>();

    private RecruitSelectionRegistry() {
    }

    public static RecruitSelectionRegistry instance() {
        return INSTANCE;
    }

    public synchronized void set(UUID playerUuid, Set<UUID> recruits) {
        if (playerUuid == null) return;
        if (recruits == null || recruits.isEmpty()) {
            selectionByPlayer.remove(playerUuid);
            return;
        }
        selectionByPlayer.put(playerUuid, new LinkedHashSet<>(recruits));
    }

    public synchronized void add(UUID playerUuid, UUID recruitUuid) {
        if (playerUuid == null || recruitUuid == null) return;
        selectionByPlayer.computeIfAbsent(playerUuid, ignored -> new LinkedHashSet<>()).add(recruitUuid);
    }

    public synchronized void remove(UUID playerUuid, UUID recruitUuid) {
        if (playerUuid == null || recruitUuid == null) return;
        Set<UUID> set = selectionByPlayer.get(playerUuid);
        if (set != null) {
            set.remove(recruitUuid);
            if (set.isEmpty()) {
                selectionByPlayer.remove(playerUuid);
            }
        }
    }

    public synchronized void clear(UUID playerUuid) {
        if (playerUuid == null) return;
        selectionByPlayer.remove(playerUuid);
    }

    public synchronized Set<UUID> get(UUID playerUuid) {
        if (playerUuid == null) return Collections.emptySet();
        Set<UUID> set = selectionByPlayer.get(playerUuid);
        return set == null ? Collections.emptySet() : Collections.unmodifiableSet(new LinkedHashSet<>(set));
    }

    public synchronized int sizeFor(UUID playerUuid) {
        if (playerUuid == null) return 0;
        Set<UUID> set = selectionByPlayer.get(playerUuid);
        return set == null ? 0 : set.size();
    }

    public synchronized boolean isEmpty(UUID playerUuid) {
        return sizeFor(playerUuid) == 0;
    }

    public synchronized void clearAllForTest() {
        selectionByPlayer.clear();
    }
}
