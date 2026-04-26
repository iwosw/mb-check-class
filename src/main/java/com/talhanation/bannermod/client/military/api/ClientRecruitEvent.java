package com.talhanation.bannermod.client.military.api;

import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
public abstract class ClientRecruitEvent extends Event {

    private final AbstractRecruitEntity recruit;

    protected ClientRecruitEvent(AbstractRecruitEntity recruit) {
        this.recruit = recruit;
    }

    public AbstractRecruitEntity getRecruit() { return recruit; }

    public static class LevelUp extends ClientRecruitEvent {
        private final int newLevel;

        public LevelUp(AbstractRecruitEntity recruit, int newLevel) {
            super(recruit);
            this.newLevel = newLevel;
        }

        public int getNewLevel() { return newLevel; }
    }

    public static class Died extends ClientRecruitEvent {
        @Nullable
        private final Player owner;

        public Died(AbstractRecruitEntity recruit, @Nullable Player owner) {
            super(recruit);
            this.owner = owner;
        }

        @Nullable
        public Player getOwner() { return owner; }
    }


    public static class Spawned extends ClientRecruitEvent {
        public Spawned(AbstractRecruitEntity recruit) {
            super(recruit);
        }
    }
}
