package dnt.ai.runnerapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import dnt.ai.runnerapp.handlers.AthleteApiHandler;
import dnt.ai.runnerapp.handlers.CoursesApiHandler;
import dnt.ai.runnerapp.handlers.CountriesApiHandler;
import dnt.ai.runnerapp.handlers.EventHistoryApiHandler;
import dnt.ai.runnerapp.handlers.EventResultsApiHandler;
import dnt.ai.runnerapp.handlers.EventVolunteersApiHandler;
import dnt.ai.runnerapp.model.Athlete;
import dnt.ai.runnerapp.model.Country;
import dnt.ai.runnerapp.model.Course;
import dnt.ai.runnerapp.model.ErrorResponse;
import dnt.ai.runnerapp.model.EventHistoryEntry;
import dnt.ai.runnerapp.model.EventResult;
import dnt.ai.runnerapp.model.Volunteer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final Vertx vertx;
    private HttpServer server;

    public static void main(String[] args) {
        new Main().start().toCompletionStage().toCompletableFuture().join();
    }

    public Main() {
        this.vertx = Vertx.vertx();
        Runtime.getRuntime().addShutdownHook(new Thread(vertx::close));
    }

    public Future<HttpServer> start() {
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        router.route().failureHandler(Main::handleFailure);

        addHandler(router, new CountriesApiHandler(COUNTRIES));
        addHandler(router, new CoursesApiHandler(COURSES));
        addHandler(router, new AthleteApiHandler(ATHLETES));
        addHandler(router, new EventHistoryApiHandler(EVENT_HISTORY));
        addHandler(router, new EventResultsApiHandler(EVENT_RESULTS));
        addHandler(router, new EventVolunteersApiHandler(EVENT_VOLUNTEERS));

        return vertx.createHttpServer()
                .requestHandler(router)
                .listen(8080)
                .onSuccess(httpServer -> {
                    this.server = httpServer;
                    LOGGER.info("Runner app started on port {}", httpServer.actualPort());
                })
                .onFailure(t -> LOGGER.error("Failed to start runner app", t));
    }

    public Future<Void> stop() {
        if (server == null) return Future.succeededFuture();
        return server.close().mapEmpty();
    }

    public int actualPort() {
        if (server == null) throw new IllegalStateException("Server not started");
        return server.actualPort();
    }

    // ------------------------------------------------------------------
    // Router wiring (reflection over the JAX-RS annotated API interfaces)
    // ------------------------------------------------------------------

    private static void addHandler(Router router, Handler<RoutingContext> operation) {
        Class<?> operationClass = operation.getClass().getInterfaces()[0];
        Path pathAnnotation = operationClass.getAnnotation(Path.class);
        String path = toVertxPath(pathAnnotation.value());

        for (Method method : operationClass.getMethods()) {
            if (method.isAnnotationPresent(GET.class))    { router.get(path).handler(operation);    return; }
            if (method.isAnnotationPresent(POST.class))   { router.post(path).handler(operation);   return; }
            if (method.isAnnotationPresent(PUT.class))    { router.put(path).handler(operation);    return; }
            if (method.isAnnotationPresent(DELETE.class)) { router.delete(path).handler(operation); return; }
            if (method.isAnnotationPresent(PATCH.class))  { router.patch(path).handler(operation);  return; }
        }
    }

    private static void handleFailure(RoutingContext ctx) {
        Throwable failure = ctx.failure();
        if (failure instanceof IllegalArgumentException) {
            respondError(ctx, 400, "BAD_REQUEST", failure.getMessage());
        } else if (failure != null) {
            LOGGER.error("Request failed", failure);
            respondError(ctx, 500, "SERVER_ERROR", "An unexpected error occurred");
        } else {
            respondError(ctx, 500, "SERVER_ERROR", "An unexpected error occurred");
        }
    }

    // ------------------------------------------------------------------
    // Response helpers
    // ------------------------------------------------------------------

    public static void respondJson(RoutingContext ctx, int status, Object body) {
        try {
            String json = MAPPER.writeValueAsString(body);
            ctx.response()
                    .setStatusCode(status)
                    .putHeader("Content-Type", "application/json")
                    .end(json);
        } catch (Exception e) {
            LOGGER.error("Failed to serialize response", e);
            ctx.response().setStatusCode(500).end("Internal error");
        }
    }

    public static void respondError(RoutingContext ctx, int status, String code, String message) {
        respondJson(ctx, status, new ErrorResponse(code, message));
    }

    static String toVertxPath(String jaxRsPath) {
        return jaxRsPath.replaceAll("\\{([^}]+)\\}", ":$1");
    }

    public static int tryParseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid integer parameter: " + value);
        }
    }

    public static long tryParseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid long parameter: " + value);
        }
    }

    public static List<Long> parseIds(String ids) {
        return Arrays.stream(ids.split(","))
                .map(String::trim)
                .filter(id -> !id.isEmpty())
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }

    // ------------------------------------------------------------------
    // Stub data
    // ------------------------------------------------------------------

    private static final List<Country> COUNTRIES = List.of(
            new Country().code("GB").name("United Kingdom"),
            new Country().code("NZ").name("New Zealand"),
            new Country().code("AU").name("Australia")
    );

    private static final List<Course> COURSES = List.of(
            new Course().courseId(1).name("London 10K").country("GB"),
            new Course().courseId(2).name("Wellington Half Marathon").country("NZ"),
            new Course().courseId(3).name("Sydney Marathon").country("AU")
    );

    private static final List<Athlete> ATHLETES = List.of(
            new Athlete().athleteId(1).name("Alice Smith").country("GB"),
            new Athlete().athleteId(2).name("Bob Jones").country("NZ"),
            new Athlete().athleteId(3).name("Carol Lee").country("AU"),
            new Athlete().athleteId(4).name("David Kim").country("GB")
    );

    private static final List<EventHistoryEntry> EVENT_HISTORY = List.of(
            new EventHistoryEntry().courseId(1).eventNumber(1).name("London 10K 2024").date("2024-05-12").status("COMPLETED"),
            new EventHistoryEntry().courseId(1).eventNumber(2).name("London 10K 2025").date("2025-05-11").status("COMPLETED"),
            new EventHistoryEntry().courseId(2).eventNumber(1).name("Wellington Half 2024").date("2024-06-15").status("COMPLETED"),
            new EventHistoryEntry().courseId(2).eventNumber(2).name("Wellington Half 2025").date("2025-06-14").status("UPCOMING"),
            new EventHistoryEntry().courseId(3).eventNumber(1).name("Sydney Marathon 2024").date("2024-08-17").status("COMPLETED")
    );

    private static final Map<Long, List<EventResult>> EVENT_RESULTS = Map.of(
            1L, List.of(
                    new EventResult().eventNumber(2).position(1).athleteId(1).athleteName("Alice Smith").time("31:42"),
                    new EventResult().eventNumber(2).position(2).athleteId(3).athleteName("Carol Lee").time("32:05"),
                    new EventResult().eventNumber(2).position(3).athleteId(4).athleteName("David Kim").time("33:18")),
            2L, List.of(
                    new EventResult().eventNumber(1).position(1).athleteId(2).athleteName("Bob Jones").time("1:04:32"),
                    new EventResult().eventNumber(1).position(2).athleteId(1).athleteName("Alice Smith").time("1:05:10"))
    );

    private static final Map<Long, Map<Integer, List<Volunteer>>> EVENT_VOLUNTEERS = Map.of(
            1L, Map.of(
                    2, List.of(
                            new Volunteer().volunteerId(1).name("Sam Wilson").role("Water Station"),
                            new Volunteer().volunteerId(2).name("Jane Doe").role("Finish Line"))),
            2L, Map.of(
                    1, List.of(
                            new Volunteer().volunteerId(3).name("Mike Brown").role("Course Marshal")))
    );

    // ------------------------------------------------------------------
    // API interfaces (code-first OpenAPI definitions)
    // ------------------------------------------------------------------

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

    @Path("/athlete")
    public interface AthleteApi extends Handler<RoutingContext> {
        @GET
        @Operation(
                operationId = "getAthlete",
                summary = "Get athletes",
                description = "Returns athlete details for the given ids",
                tags = {"RunnerApp"},
                parameters = {
                        @Parameter(name = "ids", in = ParameterIn.QUERY, required = true,
                                description = "Comma separated list of athlete ids", schema = @Schema(type = "string"))
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

    @Path("/event-history/{courseId}")
    public interface EventHistoryApi extends Handler<RoutingContext> {
        @GET
        @Operation(
                operationId = "getEventHistory",
                summary = "Get event history for a course",
                description = "Returns the history of events held on the given course",
                tags = {"RunnerApp"},
                parameters = {
                        @Parameter(name = "courseId", in = ParameterIn.PATH, required = true,
                                description = "The unique course identifier", schema = @Schema(type = "integer"))
                }
        )
        @ApiResponse(
                responseCode = "200",
                description = "Event history retrieved successfully",
                content = @Content(
                        mediaType = "application/json",
                        array = @ArraySchema(schema = @Schema(implementation = EventHistoryEntry.class))
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

    @Path("/event-results/{courseId}/{eventNumber}")
    public interface EventResultsApi extends Handler<RoutingContext> {
        @GET
        @Operation(
                operationId = "getEventResults",
                summary = "Get results for an event",
                description = "Returns the results of the given event on the given course",
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
                description = "Event results retrieved successfully",
                content = @Content(
                        mediaType = "application/json",
                        array = @ArraySchema(schema = @Schema(implementation = EventResult.class))
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
}
