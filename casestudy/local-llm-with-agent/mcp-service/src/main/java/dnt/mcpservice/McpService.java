package dnt.mcpservice;

import io.modelcontextprotocol.json.jackson3.JacksonMcpJsonMapper;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.server.transport.HttpServletSseServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public class McpService {

    private static final int PORT = 8080;
    private final Map<String, McpTool> toolRegistry = new HashMap<>();
    private final Vertx vertx;

    public McpService() {
        this.vertx = Vertx.vertx();
        registerTool(new CurrentTimeTool());
    }

    public void registerTool(McpTool tool) {
        toolRegistry.put(tool.getName(), tool);
    }

    public void start() throws Exception {
        var jsonMapper = new JacksonMcpJsonMapper(new JsonMapper());

        // Create SSE transport
        var transport = HttpServletSseServerTransportProvider.builder()
                .jsonMapper(jsonMapper)
                .sseEndpoint("/sse")
                .messageEndpoint("/message")
                .build();

        // Build tool specifications
        List<McpServerFeatures.SyncToolSpecification> toolSpecs = new ArrayList<>();
        for (McpTool mcpTool : toolRegistry.values()) {
            McpSchema.JsonSchema inputSchema = new McpSchema.JsonSchema(
                    "object", new HashMap<>(), null, null, null, null);

            McpSchema.Tool toolSchema = new McpSchema.Tool(
                    mcpTool.getName(), null, mcpTool.getDescription(),
                    inputSchema, null, null, null);

            BiFunction<McpSyncServerExchange,
                    McpSchema.CallToolRequest,
                    McpSchema.CallToolResult> handler = (exchange, request) -> {
                var args = request.arguments();
                String parameter = args == null || args.isEmpty() ? ""
                        : args.values().iterator().next().toString();
                String result = mcpTool.execute(parameter);
                List<McpSchema.Content> content = new ArrayList<>();
                content.add(new McpSchema.TextContent(result));
                return new McpSchema.CallToolResult(content, false, null, null);
            };

            toolSpecs.add(McpServerFeatures.SyncToolSpecification.builder()
                    .tool(toolSchema)
                    .callHandler(handler)
                    .build());
        }

        // Build the MCP server with SSE transport
        McpServer.sync(transport)
                .serverInfo("dnt software", "0.0.1")
                .capabilities(McpSchema.ServerCapabilities.builder().tools(true).build())
                .tools(toolSpecs)
                .build();

        // Expose via Vert.x - wrap servlet in a Vert.x handler
        HttpServer vertxServer = vertx.createHttpServer();
        vertxServer.requestHandler(request -> {
            // Note: The HttpServletSseServerTransportProvider requires a servlet container
            // For now, returning a placeholder - full integration would require adapting
            // the servlet transport to work with Vert.x directly
            request.response()
                    .putHeader("Content-Type", "text/plain")
                    .end("MCP Service is running. SSE transport requires Jetty integration.");
        });

        vertxServer.listen(PORT, "0.0.0.0")
                .onSuccess(server -> System.err.println("MCP Service listening on http://localhost:" + PORT))
                .onFailure(err -> System.err.println("Failed to start: " + err.getMessage()));
    }
}
