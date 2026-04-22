package dnt.localagentapp;

import io.vertx.core.Vertx;

import java.util.List;

/**
 * Entry point. Starts an HTTP server with a chat UI.
 *
 * Environment variables (optional — defaults shown):
 *   OLLAMA_HOST   http://localhost:11434
 *   OLLAMA_MODEL  llama3.2
 *   SERVER_PORT   8080
 */
public class AgentMain {

    public static void main(String[] args) throws Exception {

        String host  = System.getenv().getOrDefault("OLLAMA_HOST",  "http://localhost:11434");
        String model = System.getenv().getOrDefault("OLLAMA_MODEL", "llama3.2");

        System.out.println("===========================================");
        System.out.println("  ollama4j agent");
        System.out.println("  Host : " + host);
        System.out.println("  Model: " + model);
        System.out.println("===========================================");

        Agent agent = new Agent(host, model, List.of(
                new ClockTool(),
                new CalculatorTool()
        ));

        Vertx vertx = Vertx.vertx();

        Runtime.getRuntime().addShutdownHook(new Thread(vertx::close));

        vertx.deployVerticle(new AgentVerticle(agent))
                .onFailure(t -> {
                    System.err.println("Failed to start: " + t.getMessage());
                    System.exit(1);
                });

        // Keep main thread alive
        Thread.currentThread().join();
    }
}