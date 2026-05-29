package dnt.mcpclient;

import dnt.mcpservice.McpService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.net.Socket;
import java.net.ServerSocket;
import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class McpClientTest {

    private static McpService mcpService;
    private static Thread serverThread;
    private static int serverPort;
    private static String baseUrl;

    @BeforeAll
    static void startServer() throws Exception {
        serverPort = findAvailablePort();
        baseUrl = "http://localhost:" + serverPort;
        mcpService = new McpService(serverPort);

        serverThread = new Thread(() -> {
            try {
                mcpService.start();
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, "mcp-service-test-server");
        serverThread.setDaemon(true);
        serverThread.start();

        waitUntilServerReady();
    }

    @AfterAll
    static void stopServer() throws Exception {
        if (mcpService != null) {
            Method stopMethod = McpService.class.getDeclaredMethod("stopGracefully");
            stopMethod.setAccessible(true);
            stopMethod.invoke(mcpService);
        }
        if (serverThread != null) {
            serverThread.join(2000);
        }
    }

    @Test
    void shouldListCurrentTimeTool() {
        try (var client = new McpClient(baseUrl)) {
            var toolNames = client.listTools().stream().map(t -> t.name()).toList();
            assertThat(toolNames).contains("get_current_time");
        }
    }

    @Test
    void shouldGetCurrentTimeFromTool() {
        try (var client = new McpClient(baseUrl)) {
            String result = client.callTool("get_current_time", Map.of());

            assertThat(result).startsWith("Current time: ");

            String timestamp = result.substring("Current time: ".length());
            Instant parsed = Instant.parse(timestamp);
            assertThat(parsed).isNotNull();
        }
    }

    private static int findAvailablePort() throws Exception {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    private static void waitUntilServerReady() throws Exception {
        for (int i = 0; i < 40; i++) {
            try (Socket ignored = new Socket("localhost", serverPort)) {
                return;
            }
            catch (Exception ignored) {
                Thread.sleep(100);
            }
        }

        throw new IllegalStateException("MCP server did not become ready in time");
    }
}

