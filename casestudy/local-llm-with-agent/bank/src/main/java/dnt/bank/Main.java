package dnt.bank;

import dnt.bank.responses.Balance;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    private static BankService bankService1;
    private final Vertx vertx;
    private final BankService bankService;

    public static void main(String[] args) {
        Main main = new Main();
        main.start().toCompletionStage().toCompletableFuture().join();
    }

    public Main()
    {
        this.vertx = Vertx.vertx();
        Runtime.getRuntime().addShutdownHook(new Thread(vertx::close));

        bankService = new BankService();
    }

    public Future<HttpServer> start()
    {
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        router.get("/openapi.yaml").handler(ctx ->
                vertx.fileSystem().readFile("openapi.yaml")
                        .onSuccess(buffer -> ctx.response()
                                .putHeader("Content-Type", "application/yaml")
                                .putHeader("Content-Disposition", "attachment; filename=\"openapi.yaml\"")
                                .end(buffer))
                        .onFailure(err -> ctx.response().setStatusCode(404).end("openapi.yaml not found")));

        router.get("/balance").handler(this::balance);
        router.post("/deposit").handler(this::deposit);
        router.post("/withdrawal").handler(this::withdrawal);

        return vertx.createHttpServer()
                .requestHandler(router)
                .listen(8080)
                .onSuccess(successfulHttpServer -> {
                    LOGGER.info("Server started on port {}", successfulHttpServer.actualPort());
                })
                .onFailure(t -> LOGGER.error("Failed to start server", t));
    }

    private void balance(RoutingContext routingContext)
    {
        JsonObject body = routingContext.body().asJsonObject();
        long accountId = body.getLong("accountId");

        bankService.getBalance(accountId).consume(
                currentBalance -> {
                    routingContext.response().setStatusCode(200);
                    routingContext.json(new Balance(currentBalance.toPlainString()));
                },
                errorResponse -> {
                    routingContext.response().setStatusCode(400);
                    routingContext.json(errorResponse);
                }
        );
    }

    private void withdrawal(RoutingContext routingContext)
    {
        JsonObject body = routingContext.body().asJsonObject();
        long accountId = body.getLong("accountId");
        BigDecimal amount = BigDecimal.valueOf(body.getDouble("amount"));

        bankService.submitWithdrawal(accountId, amount)
                .consume(
                        currentBalance ->
                        {
                            routingContext.response().setStatusCode(200);
                            routingContext.json(new Balance(currentBalance.toPlainString()));
                        },
                        errorResponse -> {
                            routingContext.response().setStatusCode(400);
                            routingContext.json(errorResponse);
                        }
                );
    }

    private void deposit(RoutingContext routingContext)
    {
        JsonObject body = routingContext.body().asJsonObject();
        long accountId = body.getLong("accountId");
        BigDecimal amount = BigDecimal.valueOf(body.getDouble("amount"));

        bankService.submitDeposit(accountId, amount)
                .consume(
                        currentBalance ->
                        {
                            routingContext.response().setStatusCode(200);
                            routingContext.json(new Balance(currentBalance.toPlainString()));
                        },
                        errorResponse -> {
                            routingContext.response().setStatusCode(400);
                            routingContext.json(errorResponse);
                        }
                );
    }
}