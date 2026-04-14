package com.talhanation.bannerlord.client.shared.gui.group;

import com.talhanation.bannerlord.entity.shared.*;

import com.talhanation.bannerlord.client.shared.gui.widgets.ListScreenListBase;
import com.talhanation.bannerlord.persistence.military.RecruitsGroup;


public interface IGroupSelection {
    RecruitsGroup getSelected();
    ListScreenListBase<RecruitsGroupEntry> getGroupList();
}