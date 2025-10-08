package ramana.example.niotcpserver.internal.worker;

import ramana.example.niotcpserver.handler.Context;
import ramana.example.niotcpserver.internal.handler.DefaultContext;
import ramana.example.niotcpserver.log.LogFactory;
import ramana.example.niotcpserver.types.ScheduledTask;
import ramana.example.niotcpserver.types.TriConsumer;
import ramana.example.niotcpserver.util.Constants;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.logging.Logger;

public class AsyncPool {
    private static final Logger logger = LogFactory.getLogger();
    private final ExecutorService executorService;
    public AsyncPool(int asyncWorkers) {
        executorService = Executors.newFixedThreadPool(asyncWorkers);
    }

    public void shutDown() throws InterruptedException {
        executorService.shutdown();
        boolean gracefulShutDown = executorService.awaitTermination(Constants.SERVER_SHUTDOWN_TIMEOUT, TimeUnit.MILLISECONDS);
        logger.info("-- Async pool shutdown completed. Graceful shutdown: " + gracefulShutDown + " --");
    }

    public <T, R> void execute(Task<T, R> task) {
        // Processing intensive tasks are offloaded to async pool.
        // Callbacks have to be triggered from Worker to preserve sanctity.
        // i.e. to start where we left off when async was invoked.
        // Callbacks usually do not have processing intensive needs.
        // They are usually used to write response post-processing.

        Runnable command = () -> {
            Runnable callback;
            try {
                R result = task.task.apply(task.input);
                callback = () -> {
                    task.context.resetAsyncInvoked();
                    task.callback.accept(result, task.context);
                };
            } catch (RuntimeException exception) {
                callback = () -> {
                    task.context.resetAsyncInvoked();
                    task.errorCallback.accept(task.input, task.context, exception);
                };
            }

            ScheduledTask scheduledTask = new ScheduledTask(callback);
            scheduledTask.setScheduledTime(0L); // for immediate processing
            task.worker.add(scheduledTask);
        };
        executorService.execute(command);
    }

    public static class Task<T, R> {
        private final T input;
        private final DefaultContext context;
        private final Function<T, R> task;
        private final BiConsumer<R, Context.OnWrite> callback;
        private final TriConsumer<T, Context.OnWrite, Exception> errorCallback;
        private final Worker worker;

        public Task(T input, Function<T, R> task, DefaultContext context, BiConsumer<R, Context.OnWrite> callback, TriConsumer<T, Context.OnWrite, Exception> errorCallback, Worker worker) {
            this.input = input;
            this.task = task;
            this.context = context;
            this.callback = callback;
            this.errorCallback = errorCallback;
            this.worker = worker;
        }
    }
}
