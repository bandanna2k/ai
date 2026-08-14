package dnt.ai.runnerapp.handlers;

import dnt.ai.runnerapp.Main;
import dnt.ai.runnerapp.model.Country;
import io.vertx.ext.web.RoutingContext;

import java.util.List;

import static dnt.ai.runnerapp.Main.respondJson;

public class CountriesApiHandler implements Main.CountriesApi
{
    private final List<Country> countries;

    public CountriesApiHandler(List<Country> countries)
    {
        this.countries = countries;
    }

    @Override
    public void handle(RoutingContext ctx)
    {
        respondJson(ctx, 200, countries);
    }
}
