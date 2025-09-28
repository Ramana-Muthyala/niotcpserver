package ramana.example.niotcpserver.types;

import ramana.example.niotcpserver.log.LogFactory;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ScheduledTask implements Comparable<ScheduledTask> {
    private static final Logger logger = LogFactory.getLogger();
    private final Runnable task;
    public Long scheduledTime;
    public void setScheduledTime(Long scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public ScheduledTask(Runnable task) {
        this.task = task;
    }

    public void execute() {
        try {
            task.run();
        } catch (OutOfMemoryError | RuntimeException exception) {
            logger.log(Level.INFO, exception.getMessage(), exception);
        }
    }

    @Override
    public int compareTo(ScheduledTask scheduledTask) {
        int result = Long.compare(scheduledTime, scheduledTask.scheduledTime);
        return result == 0 ? Integer.compare(hashCode(), scheduledTask.hashCode()) : result;
    }
}
