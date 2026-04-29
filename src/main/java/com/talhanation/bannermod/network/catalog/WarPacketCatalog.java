package com.talhanation.bannermod.network.catalog;

import com.talhanation.bannermod.network.messages.war.MessageCancelAllyInvite;
import com.talhanation.bannermod.network.messages.war.MessageCreatePoliticalEntity;
import com.talhanation.bannermod.network.messages.war.MessageDeclareWar;
import com.talhanation.bannermod.network.messages.war.MessageInviteAlly;
import com.talhanation.bannermod.network.messages.war.MessagePlaceSiegeStandardHere;
import com.talhanation.bannermod.network.messages.war.MessageRenamePoliticalEntity;
import com.talhanation.bannermod.network.messages.war.MessageResolveWarOutcome;
import com.talhanation.bannermod.network.messages.war.MessageRespondAllyInvite;
import com.talhanation.bannermod.network.messages.war.MessageSetGovernmentForm;
import com.talhanation.bannermod.network.messages.war.MessageSetPoliticalEntityCapital;
import com.talhanation.bannermod.network.messages.war.MessageSetPoliticalEntityCharter;
import com.talhanation.bannermod.network.messages.war.MessageSetPoliticalEntityColor;
import com.talhanation.bannermod.network.messages.war.MessageSetPoliticalEntityStatus;
import com.talhanation.bannermod.network.messages.war.MessageToClientUpdateWarState;
import com.talhanation.bannermod.network.messages.war.MessageUpdateCoLeader;

/** War and political runtime packet catalog, registered after civilian packets. */
public final class WarPacketCatalog {
    public static final String NAME = "war";

    public static final Class<?>[] MESSAGES = {
        MessageToClientUpdateWarState.class,
        MessageCreatePoliticalEntity.class,
        MessageRenamePoliticalEntity.class,
        MessageSetPoliticalEntityCapital.class,
        MessagePlaceSiegeStandardHere.class,
        MessageSetGovernmentForm.class,
        MessageInviteAlly.class,
        MessageRespondAllyInvite.class,
        MessageCancelAllyInvite.class,
        MessageSetPoliticalEntityColor.class,
        MessageSetPoliticalEntityCharter.class,
        MessageDeclareWar.class,
        MessageResolveWarOutcome.class,
        MessageUpdateCoLeader.class,
        MessageSetPoliticalEntityStatus.class,
    };

    public static final PacketCatalog CATALOG = new PacketCatalog(NAME, MESSAGES);

    private WarPacketCatalog() {
    }
}
