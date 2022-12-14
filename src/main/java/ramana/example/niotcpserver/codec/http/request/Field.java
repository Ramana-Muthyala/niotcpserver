package ramana.example.niotcpserver.codec.http.request;

import java.util.ArrayList;

public class Field {
    public final String name;
    public final ArrayList<String> values;

    public Field(String name, ArrayList<String> values) {
        this.name = name;
        this.values = values;
    }
}
