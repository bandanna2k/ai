package dnt.mcpservice;

import io.modelcontextprotocol.json.jackson3.JacksonMcpJsonMapper;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import tools.jackson.databind.json.JsonMapper;

public class McpService
{
    public McpService()
    {
    }

    public void start()
    {
        var jsonMapper = new JacksonMcpJsonMapper(new JsonMapper());
        var transportProvider = new StdioServerTransportProvider(jsonMapper);

        McpSchema.ServerCapabilities capabilities = McpSchema.ServerCapabilities.builder()
                .tools(true)
                .build();

        McpServer.sync(transportProvider)
                .serverInfo("dnt software", "0.0.1")
                .capabilities(capabilities)
                .build();
    }
}

//
//    private Tool listTasksTool() {
//        return Tool.builder()
//                .name("list_tasks")
//                .description("Returns all tasks, optionally filtered by priority")
//                .inputSchema("""
//                {
//                  "type": "object",
//                  "properties": {
//                    "priority": { "type": "string", "enum": ["LOW", "MEDIUM", "HIGH"] }
//                  }
//                }
//            """)
//                .handler(args -> {
//                    String priority = args.has("priority") ? args.get("priority").asText() : null;
//
//                    return taskService.listAsync(priority)
//                            .map(tasks -> ToolResult.success(toJson(tasks)));
//                })
//                .build();


//    public void handleRequest()
//    {
//
//    }
