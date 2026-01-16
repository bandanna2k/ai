package ai.vectordatabase;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.Map;

class VectorDatabaseBase {

    private static GenericContainer<?> databaseContainer;
    protected static WeaviateClient weaviate;

    @BeforeAll
    static void beforeAll() throws InterruptedException {
        final Map<String, String> environmentalVariables = Map.of(
                "QUERY_DEFAULTS_LIMIT", "25",
                "AUTHENTICATION_ANONYMOUS_ACCESS_ENABLED", "true",
                "PERSISTENCE_DATA_PATH", "/var/lib/weaviate",
                "DEFAULT_VECTORIZER_MODULE", "none",
                "CLUSTER_HOSTNAME", "node1");

        databaseContainer = new GenericContainer<>(DockerImageName.parse("semitechnologies/weaviate:1.34.10"))
                .withCreateContainerCmdModifier(cmd -> cmd
                        .withName("weaviate")
                        .withHostConfig(
                                new HostConfig().withPortBindings(
                                        new PortBinding(Ports.Binding.bindPort(18080), new ExposedPort(8080))))
                )
                .withEnv(environmentalVariables)
                .waitingFor(Wait.forHttp("/v1/.well-known/ready")
                        .forStatusCode(200)
                        .withStartupTimeout(Duration.ofMinutes(2)));
        databaseContainer.start();

        String hostPort = "localhost:18080";
        System.out.println("Connecting to Weaviate at: http://" + hostPort);

        Config config = new Config("http", hostPort);
        weaviate = new WeaviateClient(config);

        int counter = 30;
        while(counter-- > 0) {
            System.out.println("Testing connection...");
            Result<Boolean> result = weaviate.misc().readyChecker().run();
            if(!result.hasErrors())
                return;

            Thread.sleep(1000);
        }
        throw new RuntimeException("Not ready");
    }

    @AfterAll
    static void afterAll()
    {
        databaseContainer.stop();
    }
}