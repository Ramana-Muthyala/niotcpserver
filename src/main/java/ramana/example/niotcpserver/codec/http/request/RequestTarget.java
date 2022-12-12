package ramana.example.niotcpserver.codec.http.request;

import ramana.example.niotcpserver.codec.parser.ParseException;

public class RequestTarget {
    public String path;
    public String queryString;

    /*
    * Complete lenient parsing. Can be changed later on.
    * Works faster for valid requests.
    * */
    public RequestTarget(String input) throws ParseException {
        if(input.length() == 0) throw new ParseException();
        if(input.charAt(0) == '/') {
            // originForm
            if(input.length() == 1) path = "";
            else setPathAndQueryString(input);
        } else if(input.startsWith("http")) {
            // absoluteForm
            int index = input.indexOf("://");
            if((index == -1)  ||  ((index + 3)  ==  input.length())) throw new ParseException();
            index = input.indexOf('/', index + 3);
            if(index == -1) path = "";
            else setPathAndQueryString(input.substring(index));
        } else if(input.equals("*")) {
            // asteriskForm = "*";
            path = input;
        } else {
            // expected: authorityForm
            int index = input.indexOf(':');
            if(index == -1) throw new ParseException();
            path = "";
        }
    }

    private void setPathAndQueryString(String input) {
        int index = input.indexOf('?');
        if(index != -1) {
            path = input.substring(1, index);
            queryString = ((index + 1) < input.length()) ? input.substring(index + 1) : null;
        } else {
            path = input.substring(1);
        }
    }
}
