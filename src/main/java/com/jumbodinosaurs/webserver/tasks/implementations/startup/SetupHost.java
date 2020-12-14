package com.jumbodinosaurs.webserver.tasks.implementations.startup;


import com.jumbodinosaurs.devlib.task.StartUpTask;
import com.jumbodinosaurs.webserver.util.ServerUtil;

public class SetupHost extends StartUpTask
{
    public SetupHost()
    {
        super(0);
    }
    
    
    @Override
    public void run()
    {
        ServerUtil.setHost();
    }
}
