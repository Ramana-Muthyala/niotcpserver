package ramana.example.niotcpserver.codec.parser.v1;

import ramana.example.niotcpserver.codec.parser.ParseException;
import ramana.example.niotcpserver.types.InternalException;

public abstract class AbstractStateParser {
    public boolean done;
    public AbstractStateParser next;
    public abstract void parse(Buffer buffer) throws ParseException, InternalException;
}
