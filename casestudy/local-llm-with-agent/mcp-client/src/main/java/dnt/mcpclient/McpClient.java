package dnt.mcpclient;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.json.jackson3.JacksonMcpJsonMapper;
import io.modelcontextprotocol.spec.McpSchema;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Map;

/**
 * MCP Client that connects to an MCP server over SSE transport.
 */
public class McpClient implements AutoCloseable {

    private final McpSyncClient client;

    public McpClient(String baseUrl) {
        var transport = HttpClientSseClientTransport.builder(baseUrl)
                .jsonMapper(new JacksonMcpJsonMapper(new JsonMapper()))
                .build();

        client = io.modelcontextprotocol.client.McpClient.sync(transport)
                .clientInfo(new McpSchema.Implementation("mcp-client", "1.0.0"))
                .build();

        client.initialize();
    }

    /**
     * List all tools available on the server
     */
    public List<McpSchema.Tool> listTools() {
        return client.listTools().tools();
    }

    /**
     * Call a tool by name with named string arguments
     */
    public String callTool(String toolName, Map<String, Object> arguments) {
        var request = new McpSchema.CallToolRequest(toolName, arguments);
        var result = client.callTool(request);
        return result.content().stream()
                .filter(c -> c instanceof McpSchema.TextContent)
                .map(c -> ((McpSchema.TextContent) c).text())
                .findFirst()
                .orElse("");
    }

    @Override
    public void close() {
        client.close();
    }
}
