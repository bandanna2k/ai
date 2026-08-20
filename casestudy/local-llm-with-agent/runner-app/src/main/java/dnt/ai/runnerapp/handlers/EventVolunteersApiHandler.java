package dnt.ai.runnerapp.handlers;

import dnt.ai.runnerapp.api.EventVolunteersApi;
import dnt.ai.runnerapp.model.Volunteer;
import io.vertx.ext.web.RoutingContext;

import java.util.List;
import java.util.Map;

import static dnt.ai.runnerapp.Main.respondJson;
import static dnt.ai.runnerapp.Main.tryParseInt;
import static dnt.ai.runnerapp.Main.tryParseLong;

public class EventVolunteersApiHandler implements EventVolunteersApi
{
    private final Map<Long, Map<Integer, List<Volunteer>>> eventVolunteers;

    public EventVolunteersApiHandler(Map<Long, Map<Integer, List<Volunteer>>> eventVolunteers)
    {
        this.eventVolunteers = eventVolunteers;
    }

    @Override
    public void handle(RoutingContext ctx)
    {
        long courseId = tryParseLong(ctx.pathParam("courseId"));
        int eventNumber = tryParseInt(ctx.pathParam("eventNumber"));
        List<Volunteer> volunteers = eventVolunteers
                .getOrDefault(courseId, Map.of())
                .getOrDefault(eventNumber, List.of());
        respondJson(ctx, 200, volunteers);
    }
}
