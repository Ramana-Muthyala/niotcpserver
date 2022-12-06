package ramana.example.niotcpserver.log;

import ramana.example.niotcpserver.util.Util;

import java.util.logging.Logger;

public class LogFactory {
    private static final Logger logger;

    static {
        System.setProperty("java.util.logging.config.file", Util.classPathToNormalizedPath("/logging.properties"));
        logger = Logger.getLogger("ramana.example.niotcpserver");
    }

    public static Logger getLogger() {
        return logger;
    }
}
