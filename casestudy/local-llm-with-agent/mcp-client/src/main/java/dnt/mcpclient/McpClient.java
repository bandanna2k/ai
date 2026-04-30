package dnt.mcpclient;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;

/**
 * MCP Client for testing and querying the MCP Service.
 * Uses Vert.x WebClient to communicate with the MCP server.
 */
public class McpClient {
    
    private final Vertx vertx;
    private final WebClient webClient;
    private static final String MCP_HOST = "localhost";
    private static final int MCP_PORT = 8080;
    
    public McpClient() {
        this.vertx = Vertx.vertx();
        WebClientOptions options = new WebClientOptions()
                .setDefaultHost(MCP_HOST)
                .setDefaultPort(MCP_PORT);
        this.webClient = WebClient.create(vertx, options);
    }
    
    /**
     * List all available tools from the MCP service
     */
    public void listTools() {
        JsonObject request = new JsonObject()
                .put("jsonrpc", "2.0")
                .put("id", 1)
                .put("method", "tools/list")
                .put("params", new JsonObject());
        
        sendRequest(request, "List Tools");
    }
    
    /**
     * Call a tool on the MCP service
     * @param toolName the name of the tool to call
     * @param arguments the arguments for the tool
     */
    public void callTool(String toolName, JsonObject arguments) {
        JsonObject request = new JsonObject()
                .put("jsonrpc", "2.0")
                .put("id", 2)
                .put("method", "tools/call")
                .put("params", new JsonObject()
                        .put("name", toolName)
                        .put("arguments", arguments));
        
        sendRequest(request, "Call Tool: " + toolName);
    }
    
    /**
     * Get server information from the MCP service
     */
    public void getServerInfo() {
        JsonObject request = new JsonObject()
                .put("jsonrpc", "2.0")
                .put("id", 3)
                .put("method", "initialize")
                .put("params", new JsonObject()
                        .put("protocolVersion", "2024-11-05")
                        .put("capabilities", new JsonObject())
                        .put("clientInfo", new JsonObject()
                                .put("name", "mcp-test-client")
                                .put("version", "1.0.0")));
        
        sendRequest(request, "Server Info");
    }
    
    /**
     * Send a request to the MCP service
     */
    private void sendRequest(JsonObject request, String description) {
        System.out.println("\n[" + description + "]");
        System.out.println("Request: " + request.encodePrettily());
        
        webClient.post("/")
                .putHeader("Content-Type", "application/json")
                .sendBuffer(Buffer.buffer(request.encode()))
                .onSuccess(response -> {
                    System.out.println("Response: " + response.bodyAsString());
                })
                .onFailure(err -> {
                    System.err.println("Error: " + err.getMessage());
                });
    }
    
    /**
     * Close the client and cleanup resources
     */
    public void close() {
        webClient.close();
        vertx.close();
    }
    
    /**
     * Main method to test the MCP client
     */
    public static void main(String[] args) {
        McpClient client = new McpClient();
        
        // Give the client a moment to connect, then test
        client.vertx.setTimer(500, id -> {
            System.out.println("=== MCP Client Test ===");
            
            // Get server info
            client.getServerInfo();
            
            // List tools
            client.vertx.setTimer(1000, id2 -> {
                client.listTools();
                
                // Call a tool
                client.vertx.setTimer(1000, id3 -> {
                    client.callTool("get_current_time", new JsonObject());
                    
                    // Close after a final delay
                    client.vertx.setTimer(1000, id4 -> {
                        System.out.println("\n=== Test Complete ===");
                        client.close();
                    });
                });
            });
        });
    }
}

