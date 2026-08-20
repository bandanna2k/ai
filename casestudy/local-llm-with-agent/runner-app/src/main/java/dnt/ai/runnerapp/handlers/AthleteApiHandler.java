package dnt.ai.runnerapp.handlers;

import dnt.ai.runnerapp.api.AthleteApi;
import dnt.ai.runnerapp.model.Athlete;
import io.vertx.ext.web.RoutingContext;

import java.util.List;

import static dnt.ai.runnerapp.Main.parseIds;
import static dnt.ai.runnerapp.Main.respondError;
import static dnt.ai.runnerapp.Main.respondJson;

public class AthleteApiHandler implements AthleteApi
{
    private final List<Athlete> athletes;

    public AthleteApiHandler(List<Athlete> athletes)
    {
        this.athletes = athletes;
    }

    @Override
    public void handle(RoutingContext ctx)
    {
        String idsParam = ctx.request().getParam("ids");
        if (idsParam == null || idsParam.isBlank()) {
            respondError(ctx, 400, "BAD_REQUEST", "Query parameter 'ids' is required");
            return;
        }
        List<Long> ids = parseIds(idsParam);
        List<Athlete> result = athletes.stream()
                .filter(athlete -> ids.contains(athlete.athleteId))
                .toList();
        respondJson(ctx, 200, result);
    }
}
