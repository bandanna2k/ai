package dnt.ai.runnerapp;

import dnt.ai.runnerapp.api.*;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.jaxrs2.Reader;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SpecGenerator
{
    private static final String GENERATED_FOLDER = "generated";
    private static final String SPEC_FILE_NAME = "spec.yaml";

    public OpenAPI generate()
    {
        try {
            Set<Class<?>> resourceClasses = new HashSet<>(Arrays.asList(
                    CountriesApi.class,
                    CoursesApi.class,
                    AthleteApi.class,
                    EventHistoryApi.class,
                    EventResultsApi.class,
                    EventVolunteersApi.class
            ));

            // Nested classes use binary names (with $) which Class.forName can resolve.
            Set<String> resourceClassStrings = new HashSet<>();
            for (Class<?> resourceClass : resourceClasses) {
                resourceClassStrings.add(resourceClass.getName());
            }

            SwaggerConfiguration config = new SwaggerConfiguration()
                    .resourceClasses(resourceClassStrings);

            Reader reader = new Reader(config);
            OpenAPI openAPI = reader.read(resourceClasses);

            // Remove RoutingContext component if present (picked up from handle() method signature)
            if (openAPI.getComponents() != null && openAPI.getComponents().getSchemas() != null) {
                openAPI.getComponents().getSchemas().remove("RoutingContext");
            }

            // The handle(RoutingContext) signature is an implementation detail, not a request body.
            openAPI.getPaths().values().forEach(pathItem ->
                    pathItem.readOperationsMap().values().forEach(operation -> operation.setRequestBody(null)));

            if (openAPI.getInfo() == null) {
                openAPI.setInfo(new Info()
                        .title("Runner App API")
                        .version("1.0.0")
                        .description("API for querying countries, courses, athletes, and event data"));
            }

            Server server = new Server();
            server.setUrl("http://localhost:8080");
            server.setDescription("Development server");
            openAPI.servers(Arrays.asList(server));

            writeSpecFile(openAPI);

            return openAPI;
        } catch (Exception e) {
            System.err.println("Failed to generate OpenAPI spec: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Resolves the module root directory regardless of the working directory.
     * The compiled class lives under &lt;module-root&gt;/build/classes/..., so we
     * walk up from the code-source location until we find the "build" segment
     * and return its parent.  Falls back to the current working directory when
     * the code source cannot be determined (e.g. running from a fat-jar).
     */
    private Path resolveModuleRoot() {
        try {
            Path classesPath = Path.of(
                    SpecGenerator.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            Path p = classesPath.toAbsolutePath();
            while (p != null) {
                if ("build".equals(p.getFileName() != null ? p.getFileName().toString() : null)) {
                    return p.getParent();
                }
                p = p.getParent();
            }
        } catch (Exception ignored) {
        }
        return Path.of(".").toAbsolutePath();
    }

    private void writeSpecFile(OpenAPI openAPI) throws IOException {
        Path generatedPath = resolveModuleRoot().resolve("build").resolve(GENERATED_FOLDER);
        Files.createDirectories(generatedPath);

        Path specFilePath = generatedPath.resolve(SPEC_FILE_NAME);
        String yamlContent = Yaml.pretty(openAPI);

        try (FileWriter writer = new FileWriter(specFilePath.toFile())) {
            writer.write(yamlContent);
        }

        System.out.println("Generated spec file: " + specFilePath.toAbsolutePath());
    }

    public static void main(String[] args) {
        SpecGenerator generator = new SpecGenerator();
        OpenAPI spec = generator.generate();
        if (spec != null) {
            System.out.println("OpenAPI spec generated successfully!");
        } else {
            System.err.println("Failed to generate OpenAPI spec");
        }
    }
}
