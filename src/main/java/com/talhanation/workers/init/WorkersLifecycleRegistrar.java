package com.talhanation.workers.init;

import com.talhanation.workers.WorkersSubsystem;

@Deprecated(forRemoval = false)
public class WorkersLifecycleRegistrar extends com.talhanation.bannerlord.registry.civilian.WorkersLifecycleRegistrar {

    public WorkersLifecycleRegistrar(WorkersSubsystem workersSubsystem) {
        super(workersSubsystem);
    }
}
