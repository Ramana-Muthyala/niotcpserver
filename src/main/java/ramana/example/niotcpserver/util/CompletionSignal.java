package ramana.example.niotcpserver.util;

public class CompletionSignal {
    private int counter;
    private boolean failed;

    private static final String FAILED_MESSAGE = "Completion failure";
    private static final String INVALID_COUNTER = "Invalid completion counter (should be > 0): ";

    public CompletionSignal(int counter) throws CompletionSignalException {
        if(counter < 1) throw new CompletionSignalException(INVALID_COUNTER + counter);
        this.counter = counter;
    }

    public synchronized void doWait() throws InterruptedException, CompletionSignalException {
        if(counter > 0) {
            wait();
        }
        if(failed) throw new CompletionSignalException(FAILED_MESSAGE);
    }

    public synchronized void doWait(long timeout) throws InterruptedException, CompletionSignalException {
        if(counter > 0) {
            wait(timeout);
        }
        if(failed) throw new CompletionSignalException(FAILED_MESSAGE);
    }

    public synchronized void complete() {
        counter--;
        if(counter == 0) notifyAll();
    }

    public synchronized void fail() {
        failed = true;
        counter--;
        if(counter == 0) notifyAll();
    }

    public static class CompletionSignalException extends Exception {
        public CompletionSignalException(String message) {
            super(message);
        }
    }
}
