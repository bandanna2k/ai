package dnt.localagentapp;

import io.github.ollama4j.Ollama;
import io.github.ollama4j.models.chat.OllamaChatMessage;
import io.github.ollama4j.models.chat.OllamaChatMessageRole;
import io.github.ollama4j.models.chat.OllamaChatRequest;
import io.github.ollama4j.models.chat.OllamaChatResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A simple ReAct-style agent built on ollama4j.
 *
 * Protocol the model must follow (enforced via system prompt):
 *
 *   THOUGHT: <reasoning about what to do next>
 *   ACTION: <tool_name>(<argument>)
 *
 * or, when ready to answer:
 *
 *   FINAL ANSWER: <answer text>
 *
 * The agent parses each reply, runs the requested tool, appends the
 * OBSERVATION, and loops until it sees FINAL ANSWER or hits the step limit.
 */
public class Agent {

    private static final int MAX_STEPS = 8;

    // Matches:  ACTION: tool_name(argument)
    private static final Pattern ACTION_PATTERN =
            Pattern.compile("ACTION:\\s*(\\w+)\\(([^)]*)\\)", Pattern.CASE_INSENSITIVE);

    // Matches:  FINAL ANSWER: ...  (rest of line / block)
    private static final Pattern FINAL_PATTERN =
            Pattern.compile("FINAL ANSWER:\\s*(.+)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private final Ollama api;
    private final String model;
    private final Map<String, Tool> tools = new HashMap<>();

    public Agent(String ollamaHost, String model, List<Tool> toolList) {
        this.api = new Ollama(ollamaHost);
        this.api.setRequestTimeoutSeconds(120);
        this.model = model;
        toolList.forEach(t -> tools.put(t.name(), t));
    }

    /** Run the agent on a user question and return a structured response. */
    public AgentResponse run(String userQuestion) throws Exception {

        String systemPrompt = buildSystemPrompt();

        List<OllamaChatMessage> history = new ArrayList<>();
        List<AgentStep> steps = new ArrayList<>();

        history.add(new OllamaChatMessage(OllamaChatMessageRole.SYSTEM, systemPrompt));
        history.add(new OllamaChatMessage(OllamaChatMessageRole.USER, userQuestion));

        System.out.println("\n[Agent] Goal: " + userQuestion);
        System.out.println("[Agent] Available tools: " + tools.keySet() + "\n");

        for (int step = 1; step <= MAX_STEPS; step++) {
            System.out.println("--- Step " + step + " ---");

            OllamaChatRequest request = OllamaChatRequest.builder()
                    .withModel(model)
                    .withMessages(history)
                    .build();

            OllamaChatResult result = api.chat(request, null);
            String reply = result.getChatHistory()
                    .get(result.getChatHistory().size() - 1).toString().trim();

            System.out.println("[LLM]\n" + reply + "\n");

            history = result.getChatHistory();

            // Parse THOUGHT lines
            for (String line : reply.split("\n")) {
                if (line.trim().toUpperCase().startsWith("THOUGHT:")) {
                    steps.add(new AgentStep("THOUGHT", line.trim().substring("THOUGHT:".length()).trim()));
                }
            }

            // Check for FINAL ANSWER
            Matcher finalMatcher = FINAL_PATTERN.matcher(reply);
            if (finalMatcher.find()) {
                String answer = finalMatcher.group(1).trim();
                System.out.println("[Agent] Final answer reached after " + step + " step(s).");
                steps.add(new AgentStep("FINAL", answer));
                return new AgentResponse(steps, answer);
            }

            // Check for ACTION
            Matcher actionMatcher = ACTION_PATTERN.matcher(reply);
            if (actionMatcher.find()) {
                String toolName = actionMatcher.group(1).trim();
                String argument = actionMatcher.group(2).trim();

                System.out.println("[Agent] Tool call: " + toolName + "(" + argument + ")");
                steps.add(new AgentStep("ACTION", toolName + "(" + argument + ")"));

                String observation;
                Tool tool = tools.get(toolName);
                if (tool == null) {
                    observation = "ERROR: unknown tool '" + toolName + "'. Available tools: " + tools.keySet();
                } else {
                    try {
                        observation = tool.execute(argument);
                    } catch (RuntimeException e) {
                        observation = "ERROR: " + e.getMessage();
                    }
                }

                System.out.println("[Tool] Observation: " + observation + "\n");
                steps.add(new AgentStep("OBSERVATION", observation));

                history.add(new OllamaChatMessage(OllamaChatMessageRole.USER, "OBSERVATION: " + observation));

            } else {
                System.out.println("[Agent] No action detected; treating reply as final answer.");
                steps.add(new AgentStep("FINAL", reply));
                return new AgentResponse(steps, reply);
            }
        }

        String fallback = "Agent reached the step limit without producing a final answer.";
        steps.add(new AgentStep("FINAL", fallback));
        return new AgentResponse(steps, fallback);
    }

    private String buildSystemPrompt() {
        StringBuilder sb = new StringBuilder();
        sb.append("""
                You are a helpful assistant that can use tools to answer questions.
                
                To use a tool, output exactly:
                  THOUGHT: <your reasoning>
                  ACTION: tool_name(argument)
                
                After you receive an OBSERVATION with the tool result, continue reasoning.
                When you have enough information, output exactly:
                  FINAL ANSWER: <your answer>
                
                Rules:
                - Always think before acting.
                - Only call one tool per step.
                - Never make up tool results; wait for the OBSERVATION.
                - Keep THOUGHT lines concise.
                
                Available tools:
                """);

        for (Tool t : tools.values()) {
            sb.append("  - ").append(t.description()).append("\n");
        }

        return sb.toString();
    }
}