package dnt.mcpservice;

import io.vertx.core.Vertx;

public class Main
{
    public static void main(String[] args)
    {
        new Main().go();
    }

    private void go()
    {
        Vertx vertx = Vertx.vertx();
        Runtime.getRuntime().addShutdownHook(new Thread(vertx::close));

        TaskService taskService = new TaskService();
        McpService mcpService = new McpService(taskService);

        vertx.createHttpServer()
                .requestHandler(event -> {
                })
                .listen(8080)
                .onSuccess(s -> System.out.println("MCP server running on port 8080"))
                .onFailure(e -> System.err.println("Failed to start: " + e.getMessage()));

    }
}
