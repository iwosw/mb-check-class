package com.talhanation.bannermod.client.military.gui.faction;

import com.talhanation.bannermod.client.military.gui.widgets.ListScreenListBase;
import com.talhanation.bannermod.persistence.military.RecruitsFaction;

public interface IFactionSelection {
    RecruitsFaction getSelected();
    ListScreenListBase<RecruitsFactionEntry> getFactionList();


}