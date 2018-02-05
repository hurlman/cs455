package cs455.scaling.tasks;



import static cs455.scaling.server.Server.count;
import static cs455.scaling.server.Server.randInt;

public class TestClientTask implements ClientTask {


    @Override
    public void runClientTask() throws InterruptedException {
        String threadName = Thread.currentThread().getName();
        count(threadName);

        Thread.sleep(randInt(1,100));
    }
}
