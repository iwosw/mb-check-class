package com.talhanation.recruits.world;

import com.talhanation.recruits.migration.StatePersistenceSeams;

import java.util.Map;
import java.util.function.Consumer;

public class RecruitsSavedDataFacade {

    public void loadTeams(Map<String, RecruitsFaction> target, Map<String, RecruitsFaction> loaded, Consumer<RecruitsFaction> hydrateConfig) {
        target.clear();
        target.putAll(loaded);
        target.values().forEach(hydrateConfig);
    }

    public void saveTeams(
            Map<String, RecruitsFaction> teams,
            Consumer<Map<String, RecruitsFaction>> apply,
            Runnable markDirty,
            Runnable broadcast
    ) {
        StatePersistenceSeams.SavedDataMutation mutation = new StatePersistenceSeams.SavedDataMutation(
                "teams",
                () -> apply.accept(teams),
                markDirty,
                broadcast
        );
        mutation.apply().run();
        mutation.markDirty().run();
        mutation.broadcast().run();
    }
}
