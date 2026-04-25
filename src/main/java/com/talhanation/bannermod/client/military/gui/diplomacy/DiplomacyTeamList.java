package com.talhanation.bannermod.client.military.gui.diplomacy;

import com.google.common.collect.Lists;
import com.talhanation.bannermod.client.military.ClientManager;
import com.talhanation.bannermod.client.military.gui.widgets.ListScreenListBase;
import com.talhanation.bannermod.persistence.military.RecruitsDiplomacyManager;
import com.talhanation.bannermod.persistence.military.RecruitsFaction;

import java.util.*;

public class DiplomacyTeamList extends ListScreenListBase<DiplomacyTeamEntry> {

    protected DiplomacyTeamListScreen screen;
    protected final List<DiplomacyTeamEntry> entries;
    protected String filter;
    public DiplomacyFilter diplomacyFilter;
    private int lastFactionsVersion = -1;
    private int lastDiplomacyVersion = -1;
    private DiplomacyFilter lastEntryDiplomacyFilter = null;
    private int entriesVersion = 0;
    private int lastFilteredEntriesVersion = -1;
    private String lastFilteredFilter = null;
    private final List<DiplomacyTeamEntry> cachedFilteredEntries = new ArrayList<>();

    public DiplomacyTeamList(int width, int height, int x, int y, int size, DiplomacyTeamListScreen screen) {
        super(width, height, x, y, size);
        this.screen = screen;
        this.entries = Lists.newArrayList();
        this.filter = "";
        this.diplomacyFilter = DiplomacyFilter.ALL;

        setRenderBackground(false);
        setRenderTopAndBottom(false);
        setRenderSelection(true);
    }

    public void tick() {
        if (ClientManager.factions != null && ClientManager.diplomacyMap != null
                && (lastFactionsVersion != ClientManager.factionsVersion || lastDiplomacyVersion != ClientManager.diplomacyVersion)) {
            updateEntryList();
        }
    }

    public void updateEntryList() {
        if (entriesVersion > 0 && lastFactionsVersion == ClientManager.factionsVersion && lastDiplomacyVersion == ClientManager.diplomacyVersion
                && lastEntryDiplomacyFilter == diplomacyFilter) {
            updateFilter();
            return;
        }

        entries.clear();
        lastFactionsVersion = ClientManager.factionsVersion;
        lastDiplomacyVersion = ClientManager.diplomacyVersion;
        lastEntryDiplomacyFilter = diplomacyFilter;
        entriesVersion++;

        for (RecruitsFaction team : ClientManager.factions) {
            if (ClientManager.ownFaction != null && !team.getStringID().equals(ClientManager.ownFaction.getStringID())) {
                RecruitsDiplomacyManager.DiplomacyStatus status = ClientManager.getRelation(ClientManager.ownFaction.getStringID(), team.getStringID());

                switch (diplomacyFilter) {
                    case ALL -> {
                        entries.add(new DiplomacyTeamEntry(screen, team, status));
                    }
                    case ALLIES -> {
                        if (status == RecruitsDiplomacyManager.DiplomacyStatus.ALLY) {
                            entries.add(new DiplomacyTeamEntry(screen, team, status));
                        }
                    }
                    case NEUTRALS -> {
                        if (status == RecruitsDiplomacyManager.DiplomacyStatus.NEUTRAL) {
                            entries.add(new DiplomacyTeamEntry(screen, team, status));
                        }
                    }
                    case ENEMIES -> {
                        if (status == RecruitsDiplomacyManager.DiplomacyStatus.ENEMY) {
                            entries.add(new DiplomacyTeamEntry(screen, team, status));
                        }
                    }
                }
            }
        }

        updateFilter();
    }

    public void updateFilter() {
        if (lastFilteredEntriesVersion == entriesVersion && filter.equals(lastFilteredFilter)) {
            replaceEntries(cachedFilteredEntries);
            return;
        }

        List<DiplomacyTeamEntry> filteredEntries = new ArrayList<>(entries);
        if (!filter.isEmpty()) {
            filteredEntries.removeIf(teamEntry -> {
                return teamEntry.getTeamInfo() == null || !teamEntry.getTeamInfo().getTeamDisplayName().toLowerCase(Locale.ROOT).contains(filter);
            });
        }

        filteredEntries.sort((e1, e2) -> {
            if (!e1.getClass().equals(e2.getClass())) {
                if (e1 instanceof DiplomacyTeamEntry) {
                    return 1;
                } else {
                    return -1;
                }
            }
            return volumeEntryToString(e1).compareToIgnoreCase(volumeEntryToString(e2));
        });

        cachedFilteredEntries.clear();
        cachedFilteredEntries.addAll(filteredEntries);
        lastFilteredEntriesVersion = entriesVersion;
        lastFilteredFilter = filter;
        replaceEntries(cachedFilteredEntries);
    }

    private String volumeEntryToString(DiplomacyTeamEntry entry) {
        return entry.getTeamInfo() == null ? "" : entry.getTeamInfo().getStringID();
    }

    public void setFilter(String filter) {
        if (this.filter.equals(filter)) return;
        this.filter = filter;
        updateFilter();
    }

    public boolean isEmpty() {
        return children().isEmpty();
    }

    public enum DiplomacyFilter {
        ALL,
        ALLIES,
        NEUTRALS,
        ENEMIES
    }
}
