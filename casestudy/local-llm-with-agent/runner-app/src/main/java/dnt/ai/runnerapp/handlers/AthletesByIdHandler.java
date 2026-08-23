package dnt.ai.runnerapp.handlers;

import dnt.ai.runnerapp.api.AthletesById;
import dnt.ai.runnerapp.command.GetAthletesByIdCommand;
import dnt.ai.runnerapp.command.AthletesCommandHandler;
import dnt.ai.runnerapp.model.Athlete;
import io.vertx.ext.web.RoutingContext;

import java.util.List;

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
        List<Long> ids = dnt.ai.runnerapp.Main.parseIds(idsParam);
        GetAthletesByIdCommand command = new GetAthletesByIdCommand(ids);
        List<Athlete> result = commandHandler.handle(command);
        respondJson(ctx, 200, result);
    }
}
