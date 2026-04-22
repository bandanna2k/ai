package dnt.localagentapp;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AgentVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgentVerticle.class);

    private final Agent agent;
    private final ExecutorService executor;

    public AgentVerticle(Agent agent) {
        this.agent = agent;
        this.executor = Executors.newVirtualThreadPerTaskExecutor();
    }

    @Override
    public void start(Promise<Void> startPromise) {
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        router.post("/v1/chat").handler(this::handleChat);

        router.route("/*").handler(StaticHandler.create("dist")
                .setCachingEnabled(false)
                .setIndexPage("index.html"));

        int port = Integer.parseInt(System.getenv().getOrDefault("SERVER_PORT", "8080"));
        vertx.createHttpServer()
                .requestHandler(router)
                .listen(port)
                .onSuccess(s -> {
                    LOGGER.info("Agent UI server started on port {}", s.actualPort());
                    System.out.println("Open http://localhost:" + s.actualPort() + " in your browser");
                    startPromise.complete();
                })
                .onFailure(t -> {
                    LOGGER.error("Failed to start server", t);
                    startPromise.fail(t);
                });
    }

    private void handleChat(RoutingContext ctx) {
        JsonObject body = ctx.body().asJsonObject();
        String question = body.getString("question", "").trim();
        if (question.isEmpty()) {
            ctx.response().setStatusCode(400).end(
                    new JsonObject().put("error", "question is required").encode());
            return;
        }
        executor.submit(() -> {
            try {
                AgentResponse response = agent.run(question);
                JsonArray stepsJson = new JsonArray();
                for (AgentStep step : response.steps()) {
                    stepsJson.add(new JsonObject().put("type", step.type()).put("content", step.content()));
                }
                JsonObject result = new JsonObject()
                        .put("steps", stepsJson)
                        .put("answer", response.finalAnswer());
                ctx.response()
                        .putHeader("Content-Type", "application/json")
                        .setStatusCode(200)
                        .end(result.encode());
            } catch (Exception e) {
                LOGGER.error("Agent error", e);
                ctx.response().setStatusCode(500)
                        .end(new JsonObject().put("error", e.getMessage()).encode());
            }
        });
    }
}

