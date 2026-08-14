package dnt.ai.runnerapp.handlers;

import dnt.ai.runnerapp.Main;
import dnt.ai.runnerapp.model.EventResult;
import io.vertx.ext.web.RoutingContext;

import java.util.List;
import java.util.Map;

import static dnt.ai.runnerapp.Main.respondJson;
import static dnt.ai.runnerapp.Main.tryParseInt;
import static dnt.ai.runnerapp.Main.tryParseLong;

public class EventResultsApiHandler implements Main.EventResultsApi
{
    private final Map<Long, List<EventResult>> eventResults;

    public EventResultsApiHandler(Map<Long, List<EventResult>> eventResults)
    {
        this.eventResults = eventResults;
    }

    @Override
    public void handle(RoutingContext ctx)
    {
        long courseId = tryParseLong(ctx.pathParam("courseId"));
        int eventNumber = tryParseInt(ctx.pathParam("eventNumber"));
        List<EventResult> results = eventResults.getOrDefault(courseId, List.of()).stream()
                .filter(result -> result.eventNumber == eventNumber)
                .toList();
        respondJson(ctx, 200, results);
    }
}
