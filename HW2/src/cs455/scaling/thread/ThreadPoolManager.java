package cs455.scaling.thread;

import cs455.scaling.tasks.IClientTask;

import java.io.IOException;
import java.util.*;

/**
 * Thread pool class.  Holds queue of tasks to complete, and array of worker
 * threads that poll the queue and complete tasks as they are available.
 */
public class ThreadPoolManager {

    private final LinkedList<IClientTask> tasks = new LinkedList<>();

    // Locking object for task queue.  Wait/notify is performed on this mutex.
    private final Object mutex = new Object();

    private int threadCount;
    private WorkerThread[] threads;

    /**
     * Constructor initializes number of worker threads with argument.
     */
    public ThreadPoolManager(int threadCount) {
        this.threadCount = threadCount;
        threads = new WorkerThread[threadCount];

    }

    /**
     * Public method to name and start all worker threads.  To be called after construction.
     */
    public void initialize() {

        for (int i = 0; i < threadCount; i++) {
            threads[i] = new WorkerThread();
            threads[i].setName("Worker thread " + i);
            threads[i].start();
        }
    }

    /**
     * Public method to execute a task of type IClientTask.
     * Adds task to the queue and notifies workers.
     */
    public void execute(IClientTask task) {
        synchronized (mutex) {
            tasks.add(task);
            mutex.notify();
        }
    }

    /**
     * Inner class of worker threads.  Threads poll task queue.  Synchronized and waiting on mutex
     * if queue is empty.
     */
    private class WorkerThread extends Thread {


        public void run() {
            IClientTask task;
            //noinspection InfiniteLoopStatement
            while (true) {
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
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}