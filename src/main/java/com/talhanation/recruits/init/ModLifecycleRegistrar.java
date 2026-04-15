package com.talhanation.recruits.init;

import com.talhanation.bannerlord.bootstrap.BannerlordLifecycleRegistrar;
import com.talhanation.bannerlord.bootstrap.BannerlordMain;
import com.talhanation.workers.WorkersSubsystem;

@Deprecated(forRemoval = false)
public class ModLifecycleRegistrar extends BannerlordLifecycleRegistrar {

    public ModLifecycleRegistrar(BannerlordMain main, WorkersSubsystem workersSubsystem) {
        super(main, workersSubsystem);
    }
}
