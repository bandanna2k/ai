package dnt.ai.runnerapp.api;

import dnt.ai.runnerapp.model.ErrorResponse;
import dnt.ai.runnerapp.model.Volunteer;
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

@Path("/event-volunteers/{courseId}/{eventNumber}")
public interface EventVolunteersApi extends Handler<RoutingContext> {
    @GET
    @Operation(
            operationId = "getEventVolunteers",
            summary = "Get volunteers for an event",
            description = "Returns the volunteers assigned to the given event on the given course",
            tags = {"RunnerApp"},
            parameters = {
                    @Parameter(name = "courseId", in = ParameterIn.PATH, required = true,
                            description = "The unique course identifier", schema = @Schema(type = "integer")),
                    @Parameter(name = "eventNumber", in = ParameterIn.PATH, required = true,
                            description = "The event number within the course", schema = @Schema(type = "integer"))
            }
    )
    @ApiResponse(
            responseCode = "200",
            description = "Event volunteers retrieved successfully",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = Volunteer.class))
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
