package ramana.example.niotcpserver.log;

import ramana.example.niotcpserver.util.Util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class LogFormatter extends Formatter {
    private static final String format = "%1$tb %1$td, %1$tY %1$tl:%1$tM:%1$tS %1$Tp %4$s %7$s: %2$s: %5$s%6$s%n";

    @Override
    public String format(LogRecord record) {
        Date dat = new Date(record.getMillis());
        String source;
        if (record.getSourceClassName() != null) {
            source = record.getSourceClassName();
            source = Util.normalizeClassName(source);
            if (record.getSourceMethodName() != null) {
                source += " " + record.getSourceMethodName();
            }
        } else {
            source = record.getLoggerName();
        }
        String message = record.getMessage() == null ? null : formatMessage(record);
        String throwable = "";
        if (record.getThrown() != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            pw.println();
            record.getThrown().printStackTrace(pw);
            pw.close();
            throwable = sw.toString();
        }
        String threadName = Thread.currentThread().getName();
        return String.format(format,
                dat,
                source,
                record.getLoggerName(),
                record.getLevel().getName(),
                message,
                throwable,
                threadName);
    }
}
