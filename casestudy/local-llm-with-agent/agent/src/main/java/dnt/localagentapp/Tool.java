package dnt.localagentapp;

/**
 * A tool the agent can call. Each tool has a name, a description
 * (shown to the LLM so it knows when to use it), and an execute method.
 */
public interface Tool {

    /** Short snake_case name the model uses to invoke this tool, e.g. "get_time". */
    String name();

    /**
     * Human-readable description + usage hint included in the system prompt.
     * Keep it concise — the model reads this on every call.
     */
    String description();

    /**
     * Run the tool with the given argument string and return a result string.
     * Throw a RuntimeException if something goes wrong; the agent will relay
     * the error message back to the model.
     */
    String execute(String argument);
}