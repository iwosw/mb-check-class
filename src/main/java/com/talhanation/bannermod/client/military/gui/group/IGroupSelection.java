package com.talhanation.bannermod.client.military.gui.group;

import com.talhanation.bannermod.client.military.gui.widgets.ListScreenListBase;
import com.talhanation.bannermod.persistence.military.RecruitsGroup;


public interface IGroupSelection {
    RecruitsGroup getSelected();
    ListScreenListBase<RecruitsGroupEntry> getGroupList();
}