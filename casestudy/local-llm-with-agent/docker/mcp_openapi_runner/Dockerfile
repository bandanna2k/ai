FROM golang:1.23-alpine AS build

# Build the OpenAPI MCP server binary.
RUN go install github.com/jedisct1/openapi-mcp/cmd/openapi-mcp@latest

FROM alpine:3.20

RUN adduser -D -u 10001 mcp

WORKDIR /opt/mcp

COPY --from=build /go/bin/openapi-mcp /usr/local/bin/openapi-mcp

# Build-time source of truth from runner-app spec generation.
COPY runner-app/build/generated/spec.yaml /opt/mcp/spec.yaml

EXPOSE 8081

USER mcp

ENTRYPOINT ["openapi-mcp", "--http=:8081", "--mount", "/runner:/opt/mcp/spec.yaml"]

