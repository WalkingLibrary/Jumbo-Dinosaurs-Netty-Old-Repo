package com.jumbodinosaurs.tasks.implementations.startup;

import com.jumbodinosaurs.devlib.task.Phase;
import com.jumbodinosaurs.devlib.task.StartUpTask;
import com.jumbodinosaurs.util.ServerUtil;

public class SetupHost extends StartUpTask
{
    public SetupHost()
    {
        super(Phase.PreInitialization);
    }
    
    
    @Override
    public void run()
    {
        ServerUtil.setHost();
    }
}
