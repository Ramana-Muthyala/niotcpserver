package ramana.example.niotcpserver.types;

import ramana.example.niotcpserver.log.LogFactory;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ScheduledTask implements Comparable<ScheduledTask> {
    private static final Logger logger = LogFactory.getLogger();
    private final Runnable runnable;
    public final Long scheduledTime;

    public ScheduledTask(Runnable runnable, long scheduledTime) {
        this.runnable = runnable;
        this.scheduledTime = scheduledTime;
    }

    public void execute() {
        try {
            runnable.run();
        } catch (OutOfMemoryError | RuntimeException exception) {
            logger.log(Level.INFO, exception.getMessage(), exception);
        }
    }

    @Override
    public int compareTo(ScheduledTask scheduledTask) {
        return scheduledTime.compareTo(scheduledTask.scheduledTime);
    }
}
