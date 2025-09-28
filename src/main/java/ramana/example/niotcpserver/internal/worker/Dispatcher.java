package ramana.example.niotcpserver.internal.worker;

import java.nio.channels.SocketChannel;

public class Dispatcher {
    private Worker[] workers;
    private int index = 0;

    public void dispatch(SocketChannel channel) {
        Worker worker = workers[index];
        worker.enqueue(channel);
        index++;
        if(index == workers.length) index = 0;
    }

    public void setWorkers(Worker[] workers) {
        this.workers = workers;
    }
}
