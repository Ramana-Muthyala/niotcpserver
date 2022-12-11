package ramana.example.niotcpserver.codec.http.request;

import java.util.regex.Pattern;

public class RequestTarget {
    private static final Pattern originForm = Pattern.compile("/([a-zA-Z\\d/])+([?])?([a-zA-Z\\d%&=/#])*");
    private static final Pattern absoluteForm = Pattern.compile("http(s)?://([a-zA-Z./])+([a-zA-Z\\d/])+([?])?([a-zA-Z\\d%&=/#])*");
    private static final Pattern authorityForm = Pattern.compile("([a-zA-Z.])+:(\\d)+");

    public String path;
    public String queryString;
    public RequestTarget(String input) {
        if(originForm.matcher(input).matches()) {
            setPathAndQueryString(input);
        } else if(absoluteForm.matcher(input).matches()) {
            int index = input.indexOf("://");
            setPathAndQueryString(input.substring(index + 3));
        } else if (authorityForm.matcher(input).matches()) {
            path = input.substring(0, input.indexOf(':'));
        } else {
            // asteriskForm = "*";
            path = input;
        }
    }

    private void setPathAndQueryString(String input) {
        int index = input.indexOf('?');
        if(index != -1) {
            path = input.substring(1, index);
            queryString = (index + 1 < input.length()) ? input.substring(index + 1) : null;
        } else {
            path = input.substring(1);
        }
    }
}
