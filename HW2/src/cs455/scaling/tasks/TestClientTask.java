package cs455.scaling.tasks;



import static cs455.scaling.server.Server.countThreadUsage;
import static cs455.scaling.util.Util.randInt;

public class TestClientTask implements ClientTask {


    @Override
    public void runClientTask() throws InterruptedException {
        String threadName = Thread.currentThread().getName();
        countThreadUsage(threadName);

        Thread.sleep(randInt(1,10));
    }
}
