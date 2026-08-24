package dnt.ai.runnerapp.handlers;

import dnt.ai.runnerapp.api.AthletesByName;
import dnt.ai.runnerapp.command.AthletesCommandHandler;
import dnt.ai.runnerapp.dao.Athlete;
import io.vertx.ext.web.RoutingContext;

import java.util.List;

import static dnt.ai.runnerapp.Main.respondError;
import static dnt.ai.runnerapp.Main.respondJson;

public class AthletesByNameHandler implements AthletesByName
{
    private final AthletesCommandHandler commandHandler;

    public AthletesByNameHandler(AthletesCommandHandler commandHandler)
    {
        this.commandHandler = commandHandler;
    }

    @Override
    public void handle(RoutingContext ctx)
    {
        String nameParam = ctx.request().getParam("name");
        if (nameParam == null || nameParam.isBlank()) {
            respondError(ctx, 400, "BAD_REQUEST", "Query parameter 'name' is required");
            return;
        }
        GetAthletesByNameCommand command = new GetAthletesByNameCommand(nameParam);
        List<Athlete> result = commandHandler.handle(command);
        respondJson(ctx, 200, result);
    }

    public record GetAthletesByNameCommand(String name) {}
}