package com.talhanation.bannerlord.client.shared.gui.player;

import com.talhanation.bannerlord.entity.shared.*;

import com.talhanation.bannerlord.client.shared.gui.widgets.ListScreenListBase;
import com.talhanation.bannerlord.persistence.military.RecruitsPlayerInfo;

public interface IPlayerSelection {
    RecruitsPlayerInfo getSelected();
    ListScreenListBase<RecruitsPlayerEntry> getPlayerList();


}
