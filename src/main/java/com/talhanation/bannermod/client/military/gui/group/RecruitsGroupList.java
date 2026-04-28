package com.talhanation.bannermod.client.military.gui.group;


import com.google.common.collect.Lists;
import com.talhanation.bannermod.client.military.ClientManager;
import com.talhanation.bannermod.client.military.gui.widgets.ListScreenListBase;
import com.talhanation.bannermod.persistence.military.RecruitsGroup;
import org.jetbrains.annotations.Nullable;


import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class RecruitsGroupList extends ListScreenListBase<RecruitsGroupEntry> {

    protected IGroupSelection screen;
    protected final List<RecruitsGroupEntry> entries;
    protected String filter;
    protected List<UUID> blackList = new ArrayList<>();
    private int lastGroupsVersion = -1;
    private int entriesVersion = 0;
    private int lastFilteredEntriesVersion = -1;
    private String lastFilteredFilter = null;
    private final List<RecruitsGroupEntry> cachedFilteredEntries = new ArrayList<>();
    public RecruitsGroupList(int width, int height, int x, int y, int size, IGroupSelection screen, List<UUID> blackList) {
        super(width, height, x, y, size);
        this.screen = screen;
        this.entries = Lists.newArrayList();
        this.filter = "";
        if(blackList != null){
            this.blackList.addAll(blackList);
        }
    }

    public void tick() {
        if(ClientManager.groups != null && lastGroupsVersion != ClientManager.groupsVersion){
            updateEntryList();
        }
    }

    public void updateEntryList() {
        if (entriesVersion > 0 && lastGroupsVersion == ClientManager.groupsVersion) {
            updateFilter();
            return;
        }

        entries.clear();
        lastGroupsVersion = ClientManager.groupsVersion;
        entriesVersion++;

        for (RecruitsGroup group : ClientManager.groups) {
            if(!blackList.contains(group.getUUID()))
                entries.add(new RecruitsGroupEntry(screen, group));
        }

        updateFilter();
    }

    public void updateFilter() {
        if (lastFilteredEntriesVersion == entriesVersion && filter.equals(lastFilteredFilter)) {
            replaceEntries(cachedFilteredEntries);
            return;
        }

        List<RecruitsGroupEntry> filteredEntries = new ArrayList<>(entries);
        if (!filter.isEmpty()) {
            filteredEntries.removeIf(
                groupEntry -> groupEntry.getGroup() == null ||
                              !groupEntry.getGroup().getName().toLowerCase(Locale.ROOT).contains(filter)
            );
        }

        cachedFilteredEntries.clear();
        cachedFilteredEntries.addAll(filteredEntries);
        lastFilteredEntriesVersion = entriesVersion;
        lastFilteredFilter = filter;
        replaceEntries(cachedFilteredEntries);
    }

    public void setFilter(String filter) {
        if (this.filter.equals(filter)) return;
        this.filter = filter;
        updateFilter();
    }
    @Nullable
    public RecruitsGroupEntry getGroupEntryAtPosition(double x, double y){
        return this.getEntryAtPosition(x,y);
    }



    public boolean isEmpty() {
        return children().isEmpty();
    }
}
