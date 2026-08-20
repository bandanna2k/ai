package dnt.ai.runnerapp.api;

import dnt.ai.runnerapp.model.Country;
import dnt.ai.runnerapp.model.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/countries")
public interface CountriesApi extends Handler<RoutingContext> {
    @GET
    @Operation(
            operationId = "getCountries",
            summary = "Get countries",
            description = "Returns the list of countries with available courses",
            tags = {"RunnerApp"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "Countries retrieved successfully",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = Country.class))
            )
    )
    @ApiResponse(
            responseCode = "500",
            description = "Server error",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @Override
    void handle(RoutingContext ctx);
}
