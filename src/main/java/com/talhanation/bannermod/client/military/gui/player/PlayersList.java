package com.talhanation.bannermod.client.military.gui.player;

import com.google.common.collect.Lists;
import com.talhanation.bannermod.client.military.ClientManager;
import com.talhanation.bannermod.client.military.gui.widgets.ListScreenListBase;
import com.talhanation.bannermod.persistence.military.RecruitsPlayerInfo;
import com.talhanation.bannermod.persistence.military.RecruitsFaction;
import net.minecraft.world.entity.player.Player;

import java.util.*;


public class PlayersList extends ListScreenListBase<RecruitsPlayerEntry> {

    protected IPlayerSelection screen;
    protected final List<RecruitsPlayerEntry> entries;
    protected String filter;
    protected final PlayersList.FilterType filterType;
    public final Player player;
    protected final boolean includeSelf;
    private int lastOnlinePlayersVersion = -1;
    private int lastFactionsVersion = -1;
    private int entriesVersion = 0;
    private int lastFilteredEntriesVersion = -1;
    private String lastFilteredFilter = null;
    private final List<RecruitsPlayerEntry> cachedFilteredEntries = new ArrayList<>();

    public PlayersList(int width, int height, int x, int y, int size, IPlayerSelection screen, PlayersList.FilterType filterType, Player player, boolean includeSelf) {
        super(width, height, x, y, size);
        this.screen = screen;
        this.entries = Lists.newArrayList();
        this.filter = "";
        this.filterType = filterType;
        this.player = player;
        this.includeSelf = includeSelf;
        setRenderBackground(false);
        setRenderTopAndBottom(false);
        setRenderSelection(true);
    }

    public void tick() {
        if(ClientManager.onlinePlayers != null && (lastOnlinePlayersVersion != ClientManager.onlinePlayersVersion || lastFactionsVersion != ClientManager.factionsVersion)){
            updateEntryList();
        }
    }

    public void updateEntryList() {
        if (entriesVersion > 0 && lastOnlinePlayersVersion == ClientManager.onlinePlayersVersion && lastFactionsVersion == ClientManager.factionsVersion) {
            updateFilter();
            return;
        }

        entries.clear();
        lastOnlinePlayersVersion = ClientManager.onlinePlayersVersion;
        lastFactionsVersion = ClientManager.factionsVersion;
        entriesVersion++;

        switch (filterType) {
            case SAME_TEAM -> {
                if (ClientManager.ownFaction != null) {
                    Set<UUID> onlineUUIDs = new HashSet<>();
                    for (RecruitsPlayerInfo online : ClientManager.onlinePlayers) {
                        onlineUUIDs.add(online.getUUID());
                    }

                    for (RecruitsPlayerInfo member : ClientManager.ownFaction.getMembers()) {
                        if (includeSelf || !member.getUUID().equals(this.player.getUUID())) {
                            member.setOnline(onlineUUIDs.contains(member.getUUID()));
                            entries.add(new RecruitsPlayerEntry(screen, member));
                        }
                    }
                }
            }
            default -> {
                for (RecruitsPlayerInfo playerInfo : ClientManager.onlinePlayers) {
                    if (includeSelf || !playerInfo.getUUID().equals(this.player.getUUID())) {
                        switch (filterType) {
                            default -> entries.add(new RecruitsPlayerEntry(screen, playerInfo));

                            case TEAM_JOIN_REQUEST -> {
                                if (ClientManager.ownFaction != null && ClientManager.ownFaction.getJoinRequests().contains(playerInfo.getName())) {
                                    entries.add(new RecruitsPlayerEntry(screen, playerInfo));
                                }
                            }

                            case ANY_TEAM -> {
                                if (playerInfo.getFaction() != null) {
                                    entries.add(new RecruitsPlayerEntry(screen, playerInfo));
                                }
                            }
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

        List<RecruitsPlayerEntry> filteredEntries = new ArrayList<>(entries);
        if (!filter.isEmpty()) {
            filteredEntries.removeIf(playerEntry ->
                    playerEntry.getPlayerInfo() == null || !playerEntry.getPlayerInfo().getName().toLowerCase(Locale.ROOT).contains(filter));
        }

        filteredEntries.sort((e1, e2) -> {
            if (!e1.getClass().equals(e2.getClass())) {
                if (e1 instanceof RecruitsPlayerEntry) {
                    return 1;
                } else {
                    return -1;
                }
            }
            boolean o1online = e1.getPlayerInfo() != null && e1.getPlayerInfo().isOnline();
            boolean o2online = e2.getPlayerInfo() != null && e2.getPlayerInfo().isOnline();
            if (o1online != o2online) return o1online ? -1 : 1;
            return volumeEntryToString(e1).compareToIgnoreCase(volumeEntryToString(e2));
        });

        cachedFilteredEntries.clear();
        cachedFilteredEntries.addAll(filteredEntries);
        lastFilteredEntriesVersion = entriesVersion;
        lastFilteredFilter = filter;
        replaceEntries(cachedFilteredEntries);
    }

    private String volumeEntryToString(RecruitsPlayerEntry entry) {
        return entry.getPlayerInfo() == null ? "" : entry.getPlayerInfo().getName();
    }

    public void setFilter(String filter) {
        if (this.filter.equals(filter)) return;
        this.filter = filter;
        updateFilter();
    }

    public boolean isEmpty() {
        return children().isEmpty();
    }

    public int size(){
        return children().size();
    }

    public enum FilterType{
        NONE,
        SAME_TEAM,
        TEAM_JOIN_REQUEST,
        ANY_TEAM
    }
}
