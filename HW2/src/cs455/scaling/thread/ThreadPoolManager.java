package cs455.scaling.thread;

import cs455.scaling.tasks.ClientTask;

import java.util.*;

public class ThreadPoolManager {
    // list of work to be done - fifo linked list

    //method to retrieve a spare worker

    //worker return thread to pool

    private final LinkedList<ClientTask> tasks = new LinkedList<>();
    private volatile boolean run;
    private final Object mutex = new Object();

    public ThreadPoolManager(int threadCount) {
        WorkerThread[] threads = new WorkerThread[threadCount];

        run = true;

        for (int i = 0; i < threadCount; i++) {
            threads[i] = new WorkerThread();
            threads[i].setName("Worker thread " + i);
            threads[i].start();
        }

    }

    public void execute(ClientTask task) {
        synchronized (mutex) {
            tasks.add(task);
            mutex.notify();
        }
    }

    private class WorkerThread extends Thread {


        public void run() {
            ClientTask task = null;
            while (run) {
                try {
                    synchronized (mutex) {
                        while (tasks.isEmpty()) {
                            mutex.wait();
                        }
                        task = tasks.poll();
                    }
                    if (task != null) {
                        task.runClientTask();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}