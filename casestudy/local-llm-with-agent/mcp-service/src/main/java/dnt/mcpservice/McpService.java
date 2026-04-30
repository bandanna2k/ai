package dnt.mcpservice;

import io.modelcontextprotocol.json.jackson3.JacksonMcpJsonMapper;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public class McpService
{
    private final Map<String, McpTool> toolRegistry = new HashMap<>();

    public McpService()
    {
        // Register tools
        registerTool(new CurrentTimeTool());
    }

    public void registerTool(McpTool tool) {
        toolRegistry.put(tool.getName(), tool);
    }

    public void start()
    {
        var jsonMapper = new JacksonMcpJsonMapper(new JsonMapper());
        var transportProvider = new StdioServerTransportProvider(jsonMapper);

        McpSchema.ServerCapabilities capabilities = McpSchema.ServerCapabilities.builder()
                .tools(true)
                .build();

        // Build list of tool specifications
        List<McpServerFeatures.SyncToolSpecification> toolSpecs = new ArrayList<>();
        
        for (McpTool mcpTool : toolRegistry.values()) {
            // Create a simple text input schema (object type)
            McpSchema.JsonSchema inputSchema = new McpSchema.JsonSchema(
                    "object",       // type
                    new HashMap<>(),// properties
                    null,           // required
                    null,           // additionalProperties
                    null,           // defs
                    null            // definitions
            );

            // Create the tool schema
            McpSchema.Tool toolSchema = new McpSchema.Tool(
                    mcpTool.getName(),      // name
                    null,                   // title
                    mcpTool.getDescription(),// description
                    inputSchema,            // inputSchema
                    null,                   // outputSchema
                    null,                   // annotations
                    null                    // meta
            );

            // Create handler that uses CallToolRequest
            var callToolHandler = 
                (BiFunction<
                    io.modelcontextprotocol.server.McpSyncServerExchange,
                    McpSchema.CallToolRequest,
                    McpSchema.CallToolResult>) 
                ((exchange, request) -> {
                    var args = request.arguments();
                    String parameter = args == null || args.isEmpty() ? "" : args.values().iterator().next().toString();
                    String result = mcpTool.execute(parameter);
                    
                    List<McpSchema.Content> content = new ArrayList<>();
                    content.add(new McpSchema.TextContent(result));
                    
                    return new McpSchema.CallToolResult(content, false, null, null);
                });

            // Build the specification using the builder
            var spec = McpServerFeatures.SyncToolSpecification.builder()
                    .tool(toolSchema)
                    .callHandler(callToolHandler)
                    .build();
            
            toolSpecs.add(spec);
        }

        // Build the server with all tools
        McpServer.sync(transportProvider)
                .serverInfo("dnt software", "0.0.1")
                .capabilities(capabilities)
                .tools(toolSpecs)
                .build();
    }
}

