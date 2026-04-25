package com.talhanation.bannermod.client.military.gui.faction;


import com.google.common.collect.Lists;
import com.talhanation.bannermod.client.military.ClientManager;
import com.talhanation.bannermod.client.military.gui.widgets.ListScreenListBase;
import com.talhanation.bannermod.persistence.military.RecruitsFaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RecruitsFactionList extends ListScreenListBase<RecruitsFactionEntry> {

    protected IFactionSelection screen;
    protected final List<RecruitsFactionEntry> entries;
    protected String filter;
    protected boolean showPlayerCount;
    protected boolean includeOwn;
    private int lastFactionsVersion = -1;
    private int entriesVersion = 0;
    private int lastFilteredEntriesVersion = -1;
    private String lastFilteredFilter = null;
    private final List<RecruitsFactionEntry> cachedFilteredEntries = new ArrayList<>();
    public RecruitsFactionList(int width, int height, int x, int y, int size, IFactionSelection screen, boolean showPlayerCount, boolean includeOwn) {
        super(width, height, x, y, size);
        this.screen = screen;
        this.entries = Lists.newArrayList();
        this.filter = "";
        this.showPlayerCount = showPlayerCount;
        this.includeOwn = includeOwn;
        setRenderBackground(false);
        setRenderTopAndBottom(false);
        setRenderSelection(true);
    }
    public void tick() {
        if(ClientManager.factions != null && lastFactionsVersion != ClientManager.factionsVersion){
            updateEntryList();
        }
    }
    public void updateEntryList() {
        if (entriesVersion > 0 && lastFactionsVersion == ClientManager.factionsVersion) {
            updateFilter();
            return;
        }

        entries.clear();
        lastFactionsVersion = ClientManager.factionsVersion;
        entriesVersion++;

        for (RecruitsFaction team : ClientManager.factions) {
            if(ClientManager.ownFaction == null || includeOwn || !ClientManager.ownFaction.getStringID().equals(team.getStringID()))
                entries.add(new RecruitsFactionEntry(screen, team, showPlayerCount));
        }

        updateFilter();
    }

    public void updateFilter() {
        if (lastFilteredEntriesVersion == entriesVersion && filter.equals(lastFilteredFilter)) {
            replaceEntries(cachedFilteredEntries);
            return;
        }

        List<RecruitsFactionEntry> filteredEntries = new ArrayList<>(entries);
        if (!filter.isEmpty()) {
            filteredEntries.removeIf(teamEntry -> {
                return teamEntry.getTeamInfo() == null || !teamEntry.getTeamInfo().getTeamDisplayName().toLowerCase(Locale.ROOT).contains(filter);
            });
        }

        filteredEntries.sort((e1, e2) -> {
            if (!e1.getClass().equals(e2.getClass())) {
                if (e1 instanceof RecruitsFactionEntry) {
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

    private String volumeEntryToString(RecruitsFactionEntry entry) {
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
}
