package com.talhanation.bannermod.client.military.gui.player;

import com.talhanation.bannermod.client.military.gui.widgets.ListScreenListBase;
import com.talhanation.bannermod.persistence.military.RecruitsPlayerInfo;

public interface IPlayerSelection {
    RecruitsPlayerInfo getSelected();
    ListScreenListBase<RecruitsPlayerEntry> getPlayerList();


}
