FROM eclipse-temurin:25-jre-alpine

WORKDIR /opt/agent

COPY ../build/libs/agent-all.jar agent.jar

ENV OLLAMA_HOST=http://llm:11434
ENV OLLAMA_MODEL=llama3.2
ENV SERVER_PORT=8080

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "agent.jar"]

