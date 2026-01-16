package ai.vectordatabase;

import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.data.model.WeaviateObject;
import io.weaviate.client.v1.graphql.model.GraphQLResponse;
import io.weaviate.client.v1.graphql.query.fields.Field;
import io.weaviate.client.v1.schema.model.DataType;
import io.weaviate.client.v1.schema.model.Property;
import io.weaviate.client.v1.schema.model.WeaviateClass;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExampleTest extends VectorDatabaseBase {
    @Test
    void shouldAddAndQueryDatabase() {

        WeaviateClass documentClass = WeaviateClass.builder()
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

        // Add model items
        addDocument(new Float[]{0.9f, 0.1f, 0.2f}, "The quick brown fox", "animals");
        addDocument(new Float[]{0.85f, 0.15f, 0.25f}, "Dogs are loyal pets", "animals");
        addDocument(new Float[]{0.1f, 0.9f, 0.2f}, "Rust programming language", "tech");
        addDocument(new Float[]{0.15f, 0.85f, 0.25f}, "Java is a popular language", "tech");

        // Query by vector similarity
        {
            Result<GraphQLResponse> result = queryDatabase(new Float[]{0.2f, 0.9f, 0.2f});

            assertFalse(result.hasErrors());
            System.out.println("Query results: " + result.getResult().getData());

            // Verify we got tech-related results (closest to our query vector)
            String resultData = result.getResult().getData().toString();
            assertThat(resultData).containsIgnoringCase("Java");
            assertThat(resultData).containsIgnoringCase("Rust");
        }
        {
            Result<GraphQLResponse> result = queryDatabase(new Float[]{0.9f, 0.2f, 0.2f});

            assertFalse(result.hasErrors());
            System.out.println("Query results: " + result.getResult().getData());

            // Verify we got animal-related results (closest to our query vector)
            String resultData = result.getResult().getData().toString();
            assertTrue(resultData.contains("Dogs") || resultData.contains("fox"));
            assertThat(resultData).containsIgnoringCase("Dogs");
            assertThat(resultData).containsIgnoringCase("Fox");
        }
    }

    private static Result<GraphQLResponse> queryDatabase(Float[] vector) {
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

    private void addDocument(Float[] vector, String content, String category) {
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
}
