package com.talhanation.bannermod.persistence.military;


import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RecruitsPlayerInfo {
    private UUID uuid;
    private String name;
    private boolean online;

    public RecruitsPlayerInfo(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public UUID getUUID() {
        return uuid;
    }

    public void setUUID(UUID uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    @Override
    public String toString() {
        return "{" +
                ", uuid=" + uuid +
                ", name=" + name +
                ", online=" + online +
                '}';
    }

    public CompoundTag toNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putUUID("UUID", uuid);
        nbt.putString("Name", name);
        nbt.putBoolean("Online", online);
        return nbt;
    }

    public static RecruitsPlayerInfo getFromNBT(CompoundTag nbt) {
        if(nbt == null || nbt.isEmpty()) return null;

        UUID uuid = nbt.getUUID("UUID");
        String name = nbt.getString("Name");
        boolean online = nbt.getBoolean("Online");

        RecruitsPlayerInfo info = new RecruitsPlayerInfo(uuid, name);
        info.setOnline(online);
        return info;
    }

    public static CompoundTag toNBT(List<RecruitsPlayerInfo> list) {
        CompoundTag nbt = new CompoundTag();
        ListTag playerList = new ListTag();

        for (RecruitsPlayerInfo playerInfo : list) {
            CompoundTag playerTag = playerInfo.toNBT();
            playerList.add(playerTag);
        }

        nbt.put("Players", playerList);
        return nbt;
    }

    public static List<RecruitsPlayerInfo> getListFromNBT(CompoundTag nbt) {
        List<RecruitsPlayerInfo> list = new ArrayList<>();
        ListTag playerList = nbt.getList("Players", 10);

        for (int i = 0; i < playerList.size(); i++) {
            CompoundTag playerTag = playerList.getCompound(i);
            RecruitsPlayerInfo playerInfo = RecruitsPlayerInfo.getFromNBT(playerTag);
            list.add(playerInfo);
        }

        return list;
    }
}
