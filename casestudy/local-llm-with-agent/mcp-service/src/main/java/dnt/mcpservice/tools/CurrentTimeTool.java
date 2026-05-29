package dnt.mcpservice.tools;

import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;

import java.time.Instant;

/**
 * A simple tool that returns the current system time.
 */
public class CurrentTimeTool implements McpTool {

    private static final String INPUT_SCHEMA = """
            {
              "type": "object",
              "properties": {},
              "additionalProperties": false
            }
            """;
    
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

    public McpServerFeatures.SyncToolSpecification toToolSpecification(McpJsonMapper jsonMapper) {
        var tool = McpSchema.Tool.builder()
                .name(getName())
                .description(getDescription())
                .inputSchema(jsonMapper, INPUT_SCHEMA)
                .build();

        return McpServerFeatures.SyncToolSpecification.builder()
                .tool(tool)
                .callHandler((exchange, request) -> McpSchema.CallToolResult.builder()
                        .addTextContent(execute(""))
                        .build())
                .build();
    }
}

