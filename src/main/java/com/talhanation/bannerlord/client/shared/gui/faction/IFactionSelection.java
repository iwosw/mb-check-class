package com.talhanation.bannerlord.client.shared.gui.faction;

import com.talhanation.bannerlord.entity.shared.*;

import com.talhanation.bannerlord.client.shared.gui.widgets.ListScreenListBase;
import com.talhanation.bannerlord.persistence.military.RecruitsFaction;

public interface IFactionSelection {
    RecruitsFaction getSelected();
    ListScreenListBase<RecruitsFactionEntry> getFactionList();


}