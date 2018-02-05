package cs455.scaling.server;

import cs455.scaling.tasks.TestClientTask;
import cs455.scaling.thread.ThreadPoolManager;

import java.nio.channels.Selector;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Server {

    Selector something;
    private static Random rand = new Random();
    private static HashMap<String, Integer> tracker = new HashMap<>();

    public static void main(String args[]){

        ThreadPoolManager pool = new ThreadPoolManager(10);


        for (int i=0;i<1000;i++){
            pool.execute(new TestClientTask());
        }

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        report();
    }

    private static void report() {

        for(Map.Entry e : tracker.entrySet()){
            System.out.println(e.getKey() + ": " + e.getValue());
        }
    }

    public synchronized static void count(String threadName){
        Integer currentCount = tracker.get(threadName);
        if(currentCount != null){
            tracker.put(threadName, currentCount + 1);
        }
        else{
            tracker.put(threadName, 1);
        }
    }

    public static int randInt(int min, int max) {
        int randomNum = rand.nextInt((max - min) + 1) + min;

        return randomNum;
    }
}
