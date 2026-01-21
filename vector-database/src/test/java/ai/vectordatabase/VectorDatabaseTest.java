package ai.vectordatabase;

import io.weaviate.client.base.Result;
import io.weaviate.client.v1.graphql.model.GraphQLResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VectorDatabaseTest extends VectorDatabaseBase {

    @BeforeAll
    static void beforeAll() {

        // Add model items
        addDocument(new Float[]{0.9f, 0.1f, 0.2f}, "The quick brown fox", "animals");
        addDocument(new Float[]{0.85f, 0.15f, 0.25f}, "Dogs are loyal pets", "animals");
        addDocument(new Float[]{0.1f, 0.9f, 0.2f}, "Rust programming language", "tech");
        addDocument(new Float[]{0.15f, 0.85f, 0.25f}, "Java is a popular language", "tech");
    }

    @Test
    void shouldQueryForProgrammingLanguage() {
        Result<GraphQLResponse> result = queryDatabase(new Float[]{0.2f, 0.9f, 0.2f});

        assertFalse(result.hasErrors());
        System.out.println("Query results: " + result.getResult().getData());

        // Verify we got tech-related results (closest to our query vector)
        String resultData = result.getResult().getData().toString();
        assertThat(resultData).containsIgnoringCase("Java");
        assertThat(resultData).containsIgnoringCase("Rust");
    }

    @Test
    void shouldQueryForAnimals() {

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
