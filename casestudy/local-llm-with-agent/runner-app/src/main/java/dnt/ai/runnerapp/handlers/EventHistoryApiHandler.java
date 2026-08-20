package dnt.ai.runnerapp.handlers;

import dnt.ai.runnerapp.api.EventHistoryApi;
import dnt.ai.runnerapp.model.EventHistoryEntry;
import io.vertx.ext.web.RoutingContext;

import java.util.List;

import static dnt.ai.runnerapp.Main.respondJson;
import static dnt.ai.runnerapp.Main.tryParseLong;

public class EventHistoryApiHandler implements EventHistoryApi
{
    private final List<EventHistoryEntry> eventHistory;

    public EventHistoryApiHandler(List<EventHistoryEntry> eventHistory)
    {
        this.eventHistory = eventHistory;
    }

    @Override
    public void handle(RoutingContext ctx)
    {
        long courseId = tryParseLong(ctx.pathParam("courseId"));
        List<EventHistoryEntry> events = eventHistory.stream()
                .filter(event -> event.courseId == courseId)
                .toList();
        respondJson(ctx, 200, events);
    }
}
