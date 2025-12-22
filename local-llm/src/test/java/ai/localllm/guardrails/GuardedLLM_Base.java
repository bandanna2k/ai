package ai.localllm.guardrails;

import org.junit.jupiter.api.AfterAll;

public class GuardedLLM_Base
{
    private static final TestContainerLocalLLM llm = new TestContainerLocalLLM();
    @AfterAll
    static void afterAll()
    {
        llm.close();
    }
}
