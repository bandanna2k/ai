package ai.localllm.guardrails;

import java.util.List;
import java.util.Optional;

public final class Answer
{
    private final String question;
    private final String answerUnguarded;
    private final long answerDurationMillis;
    private final String inputGuardError;
    private final long inputGuardDurationMillis;
    private final String outputGuardError;
    private final long outputGuardDurationMillis;

    public Answer(
            String question,
            String answerUnguarded,
            long answerDurationMillis,
            String inputGuardError,
            long inputGuardDurationMillis,
            String outputGuardError,
            long outputGuardDurationMillis)
    {
        this.question = question;
        this.answerUnguarded = answerUnguarded;
        this.answerDurationMillis = answerDurationMillis;
        this.inputGuardError = inputGuardError;
        this.inputGuardDurationMillis = inputGuardDurationMillis;
        this.outputGuardError = outputGuardError;
        this.outputGuardDurationMillis = outputGuardDurationMillis;
    }

    public String question()
    {
        return question;
    }

    public String getAnswer()
    {
        if (inputGuardError != null)
        {
            return inputGuardError;
        }
        if (outputGuardError != null)
        {
            return outputGuardError;
        }
        return answerUnguarded;
    }

    public Optional<String> getInputGuardMessage()
    {
        return Optional.ofNullable(inputGuardError);
    }

    public Optional<String> getOutputGuardMessage()
    {
        return Optional.ofNullable(outputGuardError);
    }

    public static class Builder
    {
        private String question;
        private String answerUnguarded;
        private long answerDurationMillis;
        private String inputGuardError;
        private long inputGuardDurationMillis;
        private String outputGuardError;
        private long outputGuardDurationMillis;

        Answer build()
        {
            return new Answer(question,
                    answerUnguarded,
                    answerDurationMillis,
                    inputGuardError,
                    inputGuardDurationMillis,
                    outputGuardError,
                    outputGuardDurationMillis);
        }

        public Builder question(String question)
        {
            this.question = question;
            return this;
        }

        public Builder answerUnguarded(String answerUnguarded)
        {
            this.answerUnguarded = answerUnguarded;
            return this;
        }

        public String answerUnguarded()
        {
            return answerUnguarded;
        }

        public Builder inputGuardError(String inputGuardError)
        {
            this.inputGuardError = inputGuardError;
            return this;
        }

        public Builder outputGuardError(String outputGuardError)
        {
            this.outputGuardError = outputGuardError;
            return this;
        }

        public Builder answerDurationMillis(long answerDurationMillis)
        {
            this.answerDurationMillis = answerDurationMillis;
            return this;
        }

        public Builder inputGuardDurationMillis(long inputGuardDurationMillis) {
            this.inputGuardDurationMillis = inputGuardDurationMillis;
            return this;
        }

        public Builder outputGuardDurationMillis(long outputGuardDurationMillis) {
            this.outputGuardDurationMillis = inputGuardDurationMillis;
            return this;
        }

    }

    @Override
    public String toString() {
        return String.format("""
                        Question:     %s
                        Input Guard (%.1f):  %s
                        Answer (%.1f):       %s
                        Output Guard (%.1f): %s
                        """,
                question,
                ((double) inputGuardDurationMillis) / 1000.0,
                inputGuardError,
                ((double) answerDurationMillis) / 1000.0,
                answerUnguarded,
                ((double) outputGuardDurationMillis) / 1000.0,
                outputGuardError);
    }
}
