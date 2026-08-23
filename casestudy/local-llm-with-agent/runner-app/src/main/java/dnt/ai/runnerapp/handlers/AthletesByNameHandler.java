package dnt.ai.runnerapp.handlers;

import dnt.ai.runnerapp.api.AthletesByName;
import dnt.ai.runnerapp.command.GetAthletesByNameCommand;
import dnt.ai.runnerapp.command.AthletesCommandHandler;
import dnt.ai.runnerapp.model.Athlete;
import java.util.List;
import io.vertx.ext.web.RoutingContext;

import static dnt.ai.runnerapp.Main.respondJson;
import static dnt.ai.runnerapp.Main.respondError;

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
}