package dnt.ai.runnerapp.handlers;

import dnt.ai.runnerapp.api.AthletesById;
import dnt.ai.runnerapp.command.AthletesCommandHandler;
import dnt.ai.runnerapp.command.GetAthletesByIdCommand;
import dnt.ai.runnerapp.dao.Athlete;
import io.vertx.ext.web.RoutingContext;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static dnt.ai.runnerapp.Main.respondError;
import static dnt.ai.runnerapp.Main.respondJson;

public class AthletesByIdHandler implements AthletesById
{
    private final AthletesCommandHandler commandHandler;

    public AthletesByIdHandler(AthletesCommandHandler commandHandler)
    {
        this.commandHandler = commandHandler;
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
        final GetAthletesByIdCommand command = new GetAthletesByIdCommand(ids);
        List<Athlete> result = commandHandler.handle(command);
        respondJson(ctx, 200, result);
    }

    private static List<Long> parseIds(String ids) {
        return Arrays.stream(ids.split(","))
                .map(String::trim)
                .filter(id -> !id.isEmpty())
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }
}
