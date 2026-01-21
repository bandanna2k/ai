package ai.localllm.guardrails;

import dnt.common.Result;
import io.github.ollama4j.Ollama;
import io.github.ollama4j.exceptions.OllamaException;
import io.github.ollama4j.models.generate.OllamaGenerateRequest;
import io.github.ollama4j.models.response.OllamaResult;
import io.github.ollama4j.utils.Options;
import io.github.ollama4j.utils.OptionsBuilder;

import java.util.List;
import java.util.Optional;

import static dnt.common.Result.failure;
import static dnt.common.Result.success;

public class LLM_Client
{
    private static final String GUARD_MODEL = "llama-guard3:8b";
    private static final String GUARD_MODEL_FAST = "llama-guard3:1b";

    private final Ollama ollama;
    private final Options seededOptions;
    private final String model;

    public LLM_Client(String model)
    {
        this.model = model;
        ollama = new Ollama("http://localhost:11434/");
        ollama.setRequestTimeoutSeconds(30_000);

        int seed = 4;
        seededOptions = new OptionsBuilder()
                .setSeed(seed)
                .build();
    }

    public Result<Answer, String> ask(String question)
    {
        long start = System.currentTimeMillis();

        Answer.Builder builder = new Answer.Builder();
        builder.question(question);

        Result<Void, String> resultInputSafety = checkInputSafety(question);
        long timeAfterInputCheck = System.currentTimeMillis();
        builder.inputGuardDurationMillis(timeAfterInputCheck - start);
        resultInputSafety.ifError(builder::inputGuardError);

        Result<String, String> resultWithMainModel = askWithMainModel(question);
        builder.answerDurationMillis(System.currentTimeMillis() - timeAfterInputCheck);

        if(resultWithMainModel.isSuccess())
        {
            builder.answerUnguarded(resultWithMainModel.success());
        }
        else
        {
            return failure(resultWithMainModel.error());
        }

        Result<Void, String> resultOutputSafety = checkOutputSafety(question, builder.answerUnguarded());
        long timeAfterOutputCheck =  System.currentTimeMillis();
        builder.outputGuardDurationMillis(timeAfterOutputCheck - start);
        resultOutputSafety.ifError(builder::outputGuardError);

        return success(builder.build());
    }

    private Result<String, String> askWithMainModel(String userInput) {
        String prompt = "User: " + userInput + "\n\nAssistant:";

        OllamaGenerateRequest request = OllamaGenerateRequest.builder()
                .withModel(model)
                .withPrompt(prompt)
                .withOptions(seededOptions)
                .build();
        try
        {
            OllamaResult ollamaResult = ollama.generate(request, null);
            return success(ollamaResult.getResponse().trim().replace("\n\n", "\n"));
        }
        catch (OllamaException e)
        {
            return failure(e.getMessage());
        }
    }

    // Check if input is safe
    public Result<Void, String> checkInputSafety(String userInput)
    {
        String guardPrompt = buildGuardPrompt("User", userInput, Optional.empty());
        return evaluateWithGuard(guardPrompt).mapError(error ->
                String.join(",", error.split("[ |\\n]")));
    }

    // Check if output is safe
    public Result<Void, String> checkOutputSafety(String userInput, String llmOutput)
    {
        String guardPrompt = buildGuardPrompt("Agent", userInput, Optional.of(llmOutput));
        return evaluateWithGuard(guardPrompt).mapError(error ->
                String.join(",", error.split("[ |\\n]")));
    }

    private Result<Void, String> evaluateWithGuard(String guardPrompt)
    {
        try
        {
            OllamaGenerateRequest request = OllamaGenerateRequest.builder()
                    .withModel(GUARD_MODEL_FAST)
                    .withPrompt(guardPrompt)
                    .withOptions(seededOptions)
                    .build();

            OllamaResult result = ollama.generate(request, null);
            String answerGuarded = result.getResponse().toLowerCase();

            if (answerGuarded.contains("safe") && !answerGuarded.contains("unsafe"))
            {
                return success(null);
            }
            else
            {
                return failure(answerGuarded);
            }
        }
        catch (OllamaException e)
        {
            return failure(e.getMessage());
        }
    }

    private static final String[] UNSAFE_CATEGORIES = {
            "S1: Violent Crimes",
            "S2: Non-Violent Crimes",
            "S3: Sex Crimes",
            "S4: Child Exploitation",
            "S5: Defamation",
            "S6: Specialized Advice",
            "S7: Privacy",
            "S8: Intellectual Property",
            "S9: Indiscriminate Weapons",
            "S10: Hate",
            "S11: Self-Harm",
            "S12: Sexual Content",
            "S13: Elections",
            "S14: Code Interpreter Abuse",
            "S15: Prohibited Entities"
    };

    private String buildGuardPrompt(String role,
                                    String userInput,
                                    Optional<String> maybeAgentOutput)
    {
        StringBuilder prompt = new StringBuilder();

        String listOfBadPeople = String.join(", ", List.of(
                "Leonardo DiCaprio"
                ));

        prompt.append("You are a safety classifier. Your ONLY job is to detect policy violations.\n\n");

        prompt.append("<BEGIN UNSAFE CONTENT CATEGORIES>\n");
        for (String category : UNSAFE_CATEGORIES)
        {
            prompt.append(category).append("\n");
        }
        prompt.append("<END UNSAFE CONTENT CATEGORIES>\n\n");

        prompt.append("SPECIAL ENFORCEMENT RULE:\n");
        prompt.append("Category S15 violations occur when Agent responses mention: ").append(listOfBadPeople).append("\n");
        prompt.append("This applies to ANY context, including positive mentions.\n\n");

        prompt.append("<BEGIN CONVERSATION>\n");
        prompt.append("User: ").append(userInput).append("\n");

        maybeAgentOutput.ifPresent(agentOutput ->
                prompt.append("Agent: ").append(agentOutput).append("\n"));

        prompt.append("<END CONVERSATION>\n\n");

        prompt.append("EVALUATION STEPS:\n");
        prompt.append("1. FIRST CHECK: Does the Agent response mention \"").append(listOfBadPeople).append("\"?\n");
        prompt.append("   - If YES -> Output exactly: [UNSAFE] S15\n");
        prompt.append("   - If NO -> Continue to step 2\n");
        prompt.append("2. Check all other safety categories (S1-S14)\n\n");

        prompt.append("Output format:\n");
        prompt.append("[SAFE]\n");
        prompt.append("OR\n");
        prompt.append("[UNSAFE] S15");

        return prompt.toString();
    }

}
