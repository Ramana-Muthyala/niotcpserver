package ramana.example.niotcpserver;

import ramana.example.niotcpserver.conf.Configuration;
import ramana.example.niotcpserver.internal.worker.Acceptor;
import ramana.example.niotcpserver.internal.worker.AsyncPool;
import ramana.example.niotcpserver.internal.worker.Dispatcher;
import ramana.example.niotcpserver.internal.worker.Worker;
import ramana.example.niotcpserver.log.LogFactory;
import ramana.example.niotcpserver.log.LogFormatter;
import ramana.example.niotcpserver.util.CompletionSignal;
import ramana.example.niotcpserver.util.Constants;

import java.nio.channels.spi.SelectorProvider;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class Server {
    private static final Logger logger = LogFactory.getLogger();
    private final Bootstrap bootstrap;
    private Runnable shutDownTrigger;
    private int numWorkers;
    private Acceptor acceptor;
    private Worker[] workers;
    private AsyncPool asyncPool;
    private static final long shutDownTimeout = Constants.SERVER_SHUTDOWN_TIMEOUT;
    private Thread shutDownHook;
    private final Object shutDownLock = new Object();
    private boolean shutDownCompleted;

    public AsyncPool getAsyncPool() {
        return asyncPool;
    }

    public Server(Bootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }

    public void start() throws CompletionSignal.CompletionSignalException, InterruptedException {
        if(bootstrap.getAsyncWorkers() > 0) {
            asyncPool = new AsyncPool(bootstrap.getAsyncWorkers());
        }
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
                    if(asyncPool != null) asyncPool.shutDown();
                    CompletionSignal shutDownSignal = new CompletionSignal(numWorkers + 1);
                    acceptor.shutDown(shutDownSignal);
                    for (Worker worker: workers) {
                        worker.shutDown(shutDownSignal);
                    }
                    shutDownSignal.doWait(shutDownTimeout);
                } catch (InterruptedException | CompletionSignal.CompletionSignalException e) {
                    logger.log(Level.SEVERE, e.getMessage(), e);
                }

                // jdk logger also uses shutdown hooks.
                // if logger shutdown hook is executed before this hook or in parallel,
                // log statements won't be executed as log handlers are reset in jdk logging shutdown hook.
                // Invoking System.out directly to have a log that shutdown has completed.
                LogRecord record = new LogRecord(Level.INFO, "Shutdown complete.");
                Arrays.stream(Thread.currentThread().getStackTrace())
                        .filter(stackTraceElement -> stackTraceElement.getClassName().equals(Server.class.getName()))
                        .findFirst().ifPresent(stackTraceLement -> {
                    record.setSourceClassName(stackTraceLement.getClassName());
                    record.setSourceMethodName(stackTraceLement.getMethodName());
                });
                record.setLoggerName(logger.getName());
                System.out.println(new LogFormatter().format(record));

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
