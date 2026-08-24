package dnt.ai.runnerapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import dnt.ai.runnerapp.command.AthletesCommandHandler;
import dnt.ai.runnerapp.dao.AthleteDao;
import dnt.ai.runnerapp.handlers.*;
import dnt.ai.runnerapp.model.*;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
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

        DataSource ds = null;
        NamedParameterJdbcTemplate namedJdbc = new NamedParameterJdbcTemplate(ds);

        addHandler(router, new CountriesApiHandler(COUNTRIES));
        addHandler(router, new CoursesApiHandler(COURSES));
        addHandler(router, new AthletesByIdHandler(new AthletesCommandHandler(new AthleteDao(namedJdbc))));
        addHandler(router, new AthletesByNameHandler(new AthletesCommandHandler(new AthleteDao(namedJdbc))));
        addHandler(router, new EventHistoryApiHandler(EVENT_HISTORY));
        addHandler(router, new EventResultsApiHandler(EVENT_RESULTS));
        addHandler(router, new EventVolunteersApiHandler(EVENT_VOLUNTEERS));

        return vertx.createHttpServer()
                .requestHandler(router)
                .listen(8080)
                .onSuccess(httpServer -> {
                    LOGGER.info("Runner app started on port {}", httpServer.actualPort());
                })
                .onFailure(t -> LOGGER.error("Failed to start runner app", t));
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

    private static final List<Athlete> ATHLETES_BY_NAME = List.of(
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
}
