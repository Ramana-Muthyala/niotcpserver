package ramana.example.niotcpserver.example.timeserver;

import org.junit.jupiter.api.Test;
import ramana.example.niotcpserver.util.ProcessTestHarness;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TimeServerTest {
    @Test
    void testTimeServer() throws IOException, InterruptedException {
        ProcessTestHarness timeServer = ProcessTestHarness.spawn(TimeServer.class)
                .readOutputUpTo("Listening on port: 8080");
        ProcessTestHarness timeClient = ProcessTestHarness.spawn(TimeClient.class).readAll();
        assertTrue(timeClient.outputContains("TimeClient$ChannelHandler onRead: Received:"));
        // Ensure line breaks are accounted for in below regex.
        assertTrue(timeClient.outputMatches(".*\\R*.*\\R*.*TimeClient\\$ChannelHandler onRead\\: Received\\: .*\\R*.*\\R*.*\\R*"));
        timeClient.destroy();
        timeServer.destroy();
    }
}
