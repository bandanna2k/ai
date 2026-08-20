package dnt.ai.runnerapp.api;

import dnt.ai.runnerapp.model.Course;
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

@Path("/courses")
public interface CoursesApi extends Handler<RoutingContext> {
    @GET
    @Operation(
            operationId = "getCourses",
            summary = "Get courses",
            description = "Returns the list of courses, optionally filtered by country",
            tags = {"RunnerApp"},
            parameters = {
                    @Parameter(name = "country", in = ParameterIn.QUERY, required = false,
                            description = "ISO country code to filter courses by", schema = @Schema(type = "string"))
            }
    )
    @ApiResponse(
            responseCode = "200",
            description = "Courses retrieved successfully",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = Course.class))
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
