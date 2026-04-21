package dnt.localagentapp;

import java.util.List;
import java.util.Scanner;

/**
 * Entry point. Runs the agent in an interactive REPL.
 *
 * Usage:
 *   java -jar target/ollama4j-agent.jar
 *
 * Environment variables (optional — defaults shown):
 *   OLLAMA_HOST   http://localhost:11434
 *   OLLAMA_MODEL  llama3.2
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
        System.out.println("Tools: get_time, calculate");
        System.out.println("Type your question and press Enter. (Ctrl+C to quit)\n");

        Agent agent = new Agent(host, model, List.of(
                new ClockTool(),
                new CalculatorTool()
        ));

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("You> ");
            if (!scanner.hasNextLine()) break;
            String question = scanner.nextLine().trim();
            if (question.isEmpty()) continue;

            try {
                String answer = agent.run(question);
                System.out.println("\nAgent> " + answer + "\n");
            } catch (Exception e) {
                System.err.println("[Error] " + e.getMessage());
            }
        }
    }
}