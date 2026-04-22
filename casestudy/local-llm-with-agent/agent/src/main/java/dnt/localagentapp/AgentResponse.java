package dnt.localagentapp;

import java.util.List;

public record AgentResponse(List<AgentStep> steps, String finalAnswer) {}

