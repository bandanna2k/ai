package dnt.ai.runnerapp.api;

import dnt.ai.runnerapp.model.Athlete;
import dnt.ai.runnerapp.model.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/athlete")
public interface AthletesByName extends Handler<RoutingContext> {
    @GET
    @Operation(
            operationId = "getAthleteByName",
            summary = "Get athletes",
            description = "Returns athlete details for the given name",
            tags = {"RunnerApp"},
            parameters = {
                    @Parameter(name = "name", in = ParameterIn.QUERY, required = true,
                            description = "Comma separated list of partial athlete name", schema = @Schema(type = "string"))
            }
    )
    @ApiResponse(
            responseCode = "200",
            description = "Athletes retrieved successfully",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = Athlete.class))
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
