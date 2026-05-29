package dnt.mcpservice;

import dnt.mcpservice.tools.CurrentTimeTool;
import io.modelcontextprotocol.json.jackson3.JacksonMcpJsonMapper;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.HttpServletSseServerTransportProvider;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.server.Server;
import tools.jackson.databind.json.JsonMapper;

public class McpService {

    private final int port;
    private Server jettyServer;
    private McpSyncServer mcpServer;

    public McpService(int port) {
        this.port = port;
    }

    public void start() throws Exception {
        var jsonMapper = new JacksonMcpJsonMapper(new JsonMapper());
        var transportProvider = HttpServletSseServerTransportProvider.builder()
                .jsonMapper(jsonMapper)
                .baseUrl("http://localhost:" + port)
                .sseEndpoint("/sse")
                .messageEndpoint("/message")
                .build();

        mcpServer = McpServer.sync(transportProvider)
                .serverInfo("mcp-service", "0.1.0")
                .tools(new CurrentTimeTool().toToolSpecification(jsonMapper))
                .build();

        jettyServer = new Server(port);
        var servletContext = new ServletContextHandler(ServletContextHandler.SESSIONS);
        servletContext.setContextPath("/");
        servletContext.addServlet(new ServletHolder(transportProvider), "/*");
        jettyServer.setHandler(servletContext);

        Runtime.getRuntime().addShutdownHook(new Thread(this::stopGracefully));

        jettyServer.start();
        System.out.println("MCP service started on http://localhost:" + port + " (SSE: /sse, message: /message)");
        jettyServer.join();
    }

    private void stopGracefully() {
        if (mcpServer != null) {
            mcpServer.closeGracefully();
        }
        if (jettyServer != null) {
            try {
                jettyServer.stop();
            }
            catch (Exception ignored) {
                // Best-effort shutdown on JVM exit.
            }
        }
    }
}
