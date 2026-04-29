package dnt.mcpservice;

import io.modelcontextprotocol.json.jackson3.JacksonMcpJsonMapper;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import tools.jackson.databind.json.JsonMapper;

public class McpService
{
    private TaskService taskService;

    public McpService(TaskService taskService)
    {
        this.taskService = taskService;
    }

    public void start()
    {
        taskService = new TaskService();

        var transportProvider = new StdioServerTransportProvider(new JacksonMcpJsonMapper(new JsonMapper()));

        McpSchema.ServerCapabilities capabilities = McpSchema.ServerCapabilities.builder().tools(true).build();
        McpServer.sync(transportProvider)
                .serverInfo("trading-desk", "1.0.0")
                .capabilities(capabilities)
//                .tool(createTaskTool())
//                .tool(listTasksTool())
                .build();
    }
}
//    private Tool createTaskTool() {
//        return Tool.builder()
//                .name("create_task")
//                .description("Creates a new task with a title and priority")
//                .inputSchema("""
//                {
//                  "type": "object",
//                  "properties": {
//                    "title":    { "type": "string" },
//                    "priority": { "type": "string", "enum": ["LOW", "MEDIUM", "HIGH"] }
//                  },
//                  "required": ["title", "priority"]
//                }
//            """)
//                .handler(args -> {
//                    String title    = args.get("title").asText();
//                    String priority = args.get("priority").asText();
//
//                    // Vert.x favors async — return a Future instead of blocking
//                    return taskService.createAsync(title, Priority.valueOf(priority))
//                            .map(task -> ToolResult.success("""
//                        { "id": "%s", "title": "%s", "priority": "%s", "status": "OPEN" }
//                        """.formatted(task.getId(), task.getTitle(), task.getPriority())));
//                })
//                .build();
//    }
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
