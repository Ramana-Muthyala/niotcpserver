package ramana.example.niotcpserver;

import ramana.example.niotcpserver.conf.Configuration;
import ramana.example.niotcpserver.log.LogFactory;
import ramana.example.niotcpserver.util.CompletionSignal;
import ramana.example.niotcpserver.util.Constants;
import ramana.example.niotcpserver.worker.Dispatcher;
import ramana.example.niotcpserver.worker.Acceptor;
import ramana.example.niotcpserver.worker.Worker;

import java.nio.channels.spi.SelectorProvider;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {
    private static final Logger logger = LogFactory.getLogger();
    private final Bootstrap bootstrap;
    private Runnable shutDownTrigger;
    private int numWorkers;
    private Acceptor acceptor;
    private Worker[] workers;
    private static final long shutDownTimeout = Constants.SERVER_SHUTDOWN_TIMEOUT;
    private Thread shutDownHook;
    private final Object shutDownLock = new Object();
    private boolean shutDownCompleted;

    public Server(Bootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }

    public void start() throws CompletionSignal.CompletionSignalException, InterruptedException {
        SelectorProvider provider = SelectorProvider.provider();
        Dispatcher dispatcher = new Dispatcher();
        Configuration.ServerConfiguration serverConfiguration = new Configuration.ServerConfiguration(provider, bootstrap.getServerSocketOptions(), bootstrap.getPort(), dispatcher, bootstrap.getBacklog());
        Configuration.WorkerConfiguration workerConfiguration = new Configuration.WorkerConfiguration(provider, bootstrap.getSocketOptions(), bootstrap.getChannelHandlers(), bootstrap.getIdleTimeout(), bootstrap.isDefaultRead(), bootstrap.isLoggingEnabled(), bootstrap.isSslEnabled());
        numWorkers = bootstrap.getNumWorkers();
        workers = new Worker[numWorkers];
        dispatcher.setWorkers(workers);
        CompletionSignal initSignal = new CompletionSignal(numWorkers + 1);
        for (int i = 0; i < workers.length; i++) {
            Worker worker = new Worker(initSignal, workerConfiguration, this);
            workers[i] = worker;
            new Thread(worker, "Worker-" + i).start();
        }
        initShutDownHook();
        Runtime.getRuntime().addShutdownHook(shutDownHook);
        acceptor = new Acceptor(initSignal, serverConfiguration);
        new Thread(acceptor, "Acceptor").start();
        initSignal.doWait();
    }

    private void initShutDownHook() {
        shutDownTrigger = () -> {
            synchronized (shutDownLock) {
                if(shutDownCompleted) return;
                logger.info("Initiating shutdown.");
                try {
                    CompletionSignal shutDownSignal = new CompletionSignal(numWorkers + 1);
                    acceptor.shutDown(shutDownSignal);
                    for (Worker worker: workers) {
                        worker.shutDown(shutDownSignal);
                    }
                    shutDownSignal.doWait(shutDownTimeout);
                } catch (InterruptedException | CompletionSignal.CompletionSignalException e) {
                    logger.log(Level.SEVERE, e.getMessage(), e);
                }
                logger.info("--- Shutdown complete ---");
                shutDownCompleted = true;
            }
        };
        shutDownHook = new Thread(shutDownTrigger);
    }

    public void shutDown(boolean newThread) {
        Runnable runnable = () -> {
            Runtime.getRuntime().removeShutdownHook(shutDownHook);
            shutDownTrigger.run();
        };
        if(newThread) {
            new Thread(runnable).start();
        } else {
            runnable.run();
        }
    }
}
