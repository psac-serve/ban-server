package com.github.psacserve.task;

import com.github.psacserve.BanServer;

import java.util.ArrayList;
import java.util.Random;

public class Worker
{
    private volatile ArrayList<Task> taskList;

    private volatile boolean isRunning;

    public Worker()
    {
        this.taskList = new ArrayList<>();
        this.isRunning = false;
    }

    public void addTask(Task task)
    {
        this.taskList.add(task);
    }

    public void start()
    {
        this.isRunning = true;
        new Thread(() -> {
            while (isRunning)
            {
                if (taskList.size() == 0)
                    continue;
                Random random = new Random();
                int index = random.nextInt(taskList.size());
                try
                {
                    taskList.get(index).run();
                }
                catch (Exception e)
                {
                    BanServer.printStackTrace(e);
                }

                taskList.remove(index);
            }
        }).start();
    }

    public void stop()
    {
        this.isRunning = false;
    }

    public void clear()
    {
        this.taskList = new ArrayList<>();
    }

    public boolean isRunning()
    {
        return isRunning;
    }
}
