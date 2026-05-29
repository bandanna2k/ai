package dnt.mcpservice.tools;

/**
 * Interface for MCP tools that can be registered and called by clients
 */
public interface McpTool {
    
    /**
     * Unique name identifier for this tool
     */
    String getName();
    
    /**
     * Human-readable description of what this tool does
     */
    String getDescription();
    
    /**
     * Execute the tool with the given parameter
     * @param parameter the input parameter as a JSON string or simple value
     * @return the result as a string
     */
    String execute(String parameter);
}

