package ramana.example.niotcpserver.log;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class LogFactory {
    private static final Logger logger;

    static {
        try {
            InputStream in = LogFactory.class.getResourceAsStream("/logging.properties");
            LogManager.getLogManager().readConfiguration(in);
            in.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        logger = Logger.getLogger("ramana.example.niotcpserver");
    }

    public static Logger getLogger() {
        return logger;
    }
}
