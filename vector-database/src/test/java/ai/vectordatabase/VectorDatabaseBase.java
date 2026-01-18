package ai.vectordatabase;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.data.model.WeaviateObject;
import io.weaviate.client.v1.graphql.model.GraphQLResponse;
import io.weaviate.client.v1.graphql.query.fields.Field;
import io.weaviate.client.v1.schema.model.DataType;
import io.weaviate.client.v1.schema.model.Property;
import io.weaviate.client.v1.schema.model.WeaviateClass;
import org.junit.jupiter.api.AfterAll;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;

class VectorDatabaseBase {

    private static GenericContainer<?> databaseContainer;
    protected static WeaviateClient weaviate;

    static { initialise(); }

    private static void initialise() {
        try {
            final Map<String, String> environmentalVariables = Map.of(
                    "QUERY_DEFAULTS_LIMIT", "25",
                    "AUTHENTICATION_ANONYMOUS_ACCESS_ENABLED", "true",
                    "PERSISTENCE_DATA_PATH", "/var/lib/weaviate",
                    "DEFAULT_VECTORIZER_MODULE", "none",
                    "CLUSTER_HOSTNAME", "node1");

            databaseContainer = new GenericContainer<>(DockerImageName.parse("semitechnologies/weaviate:1.34.10"))
                    .withExposedPorts(8080)
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
            while (counter-- > 0) {
                System.out.println("Testing connection...");
                Result<Boolean> result = weaviate.misc().readyChecker().run();
                if (!result.hasErrors()) {
                    initialiseDatabase();
                    return;
                }
                Thread.sleep(1000);
            }
            throw new RuntimeException("Not ready");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void initialiseDatabase() {
        final WeaviateClass documentClass = WeaviateClass.builder()
                .className("Document")
                .vectorizer("none")
                .properties(List.of(
                        Property.builder()
                                .name("content")
                                .dataType(List.of(DataType.TEXT))
                                .build(),
                        Property.builder()
                                .name("category")
                                .dataType(List.of(DataType.TEXT))
                                .build()
                ))
                .build();

        Result<Boolean> schemaResult = weaviate.schema().classCreator()
                .withClass(documentClass)
                .run();
        assertFalse(schemaResult.hasErrors());
    }

    @AfterAll
    static void afterAll() {
        databaseContainer.stop();
    }

    protected static void addDocument(Float[] vector, String content, String category) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("content", content);
        properties.put("category", category);

        Result<WeaviateObject> result = weaviate.data().creator()
                .withClassName("Document")
                .withProperties(properties)
                .withVector(vector)
                .run();

        assertFalse(result.hasErrors());
    }

    protected static Result<GraphQLResponse> queryDatabase(Float[] vector) {
        return weaviate.graphQL().get()
                .withClassName("Document")
                .withNearVector(weaviate.graphQL().arguments().nearVectorArgBuilder()
                        .vector(vector)
                        .build())
                .withLimit(2)
                .withFields(
                        Field.builder().name("content").build(),
                        Field.builder().name("category").build(),
                        Field.builder().name("_additional { distance }").build()
                )
                .run();
    }
}