package cs455.scaling.tasks;

import java.io.IOException;

/**
 * Interface that defines method which can be executed by the thread pool.
 */
public interface IClientTask {
    void runClientTask() throws IOException;
}
