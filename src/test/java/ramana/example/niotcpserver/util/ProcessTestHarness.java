package ramana.example.niotcpserver.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ProcessTestHarness {
    private static final String classPathValue = System.getProperty("java.class.path");
    Process process;
    StringBuilder standardOut = new StringBuilder();
    StringBuilder errorOut = new StringBuilder();
    public static ProcessTestHarness spawn(Class<?> clazz) throws IOException {
        ProcessTestHarness harness = new ProcessTestHarness();
        harness.process = new ProcessBuilder("java", "-classpath", classPathValue, clazz.getName()).start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            harness.process.destroy();
            System.out.println("ShutdownHook: Process: " + harness.process + ", exitValue: " + harness.process.exitValue());
        }));
        return harness;
    }

    public String processToString() {
        return process.toString();
    }

    public ProcessTestHarness readOutputUpTo(String token) throws IOException {
        read(token, standardOut, process.getInputStream());
        return this;
    }

    public ProcessTestHarness readErrorUpTo(String token) throws IOException {
        read(token, errorOut, process.getErrorStream());
        return this;
    }

    private void read(String token, StringBuilder builder, InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String input;
        while((input = reader.readLine()) != null) {
            builder.append(input).append("\n");
            if(input.contains(token)) {
                break;
            }
        }
    }

    public void readAllError() throws IOException {
        read(errorOut, process.getErrorStream());
    }

    public void readAllOutput() throws IOException {
        read(standardOut, process.getInputStream());
    }

    public boolean outputContains(String token) {
        return standardOut.toString().contains(token);
    }

    public boolean errorContains(String token) {
        return errorOut.toString().contains(token);
    }

    public boolean outputMatches(String regex) {
        return standardOut.toString().matches(regex);
    }

    public boolean errorMatches(String regex) {
        return errorOut.toString().matches(regex);
    }

    private void read(StringBuilder builder, InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String input;
        while((input = reader.readLine()) != null) {
            builder.append(input).append("\n");
        }
    }

    public void destroy() throws InterruptedException {
        process.destroy();
        process.waitFor();
    }

    public ProcessTestHarness readAll() throws IOException {
        readAllOutput();
        readAllError();
        return this;
    }
}
