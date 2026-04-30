package dnt.mcpservice;

import java.time.Instant;

/**
 * A simple tool that returns the current system time.
 */
public class CurrentTimeTool implements McpTool {
    
    @Override
    public String getName() {
        return "get_current_time";
    }
    
    @Override
    public String getDescription() {
        return "Returns the current system time in ISO 8601 format";
    }
    
    @Override
    public String execute(String parameter) {
        return "Current time: " + Instant.now().toString();
    }
}

