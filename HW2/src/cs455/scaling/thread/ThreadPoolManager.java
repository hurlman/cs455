package cs455.scaling.thread;

import cs455.scaling.tasks.ClientTask;

import java.util.*;

public class ThreadPoolManager {

    private final LinkedList<ClientTask> tasks = new LinkedList<>();
    private volatile boolean run;
    private final Object mutex = new Object();
    private int threadCount;
    private WorkerThread[] threads;

    public ThreadPoolManager(int threadCount) {
        this.threadCount = threadCount;
        run = true;
        threads = new WorkerThread[threadCount];

    }

    public void initialize(){

        for (int i = 0; i < threadCount; i++) {
            threads[i] = new WorkerThread();
            threads[i].setName("Worker thread " + i);
            threads[i].start();
        }
    }

    public void stop(){
        run = false;
    }

    public void execute(ClientTask task) {
        synchronized (mutex) {
            tasks.add(task);
            mutex.notify();
        }
    }

    private class WorkerThread extends Thread {


        public void run() {
            ClientTask task;
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