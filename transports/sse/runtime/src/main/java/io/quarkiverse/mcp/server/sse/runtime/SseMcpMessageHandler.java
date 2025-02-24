package io.quarkiverse.mcp.server.sse.runtime;

import org.jboss.logging.Logger;

import io.quarkiverse.mcp.server.McpConnection;
import io.quarkiverse.mcp.server.runtime.ConnectionManager;
import io.quarkiverse.mcp.server.runtime.JsonRPC;
import io.quarkiverse.mcp.server.runtime.McpMessageHandler;
import io.quarkiverse.mcp.server.runtime.PromptManager;
import io.quarkiverse.mcp.server.runtime.ResourceManager;
import io.quarkiverse.mcp.server.runtime.Responder;
import io.quarkiverse.mcp.server.runtime.ToolManager;
import io.quarkiverse.mcp.server.runtime.config.McpRuntimeConfig;
import io.quarkiverse.mcp.server.sse.runtime.config.McpSseRuntimeConfig;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

class SseMcpMessageHandler extends McpMessageHandler implements Handler<RoutingContext> {

    private static final Logger LOG = Logger.getLogger(SseMcpMessageHandler.class);

    private final TrafficLogger trafficLogger;

    protected SseMcpMessageHandler(McpRuntimeConfig config, ConnectionManager connectionManager, PromptManager promptManager,
            ToolManager toolManager, ResourceManager resourceManager, McpSseRuntimeConfig sseConfig) {
        super(config, connectionManager, promptManager, toolManager, resourceManager);
        this.trafficLogger = sseConfig.trafficLogging().enabled() ? new TrafficLogger(sseConfig.trafficLogging().textLimit())
                : null;
    }

    @Override
    public void handle(RoutingContext ctx) {
        HttpServerRequest request = ctx.request();
        String connectionId = ctx.pathParam("id");
        if (connectionId == null) {
            LOG.errorf("Connection id is missing: %s", ctx.normalizedPath());
            ctx.fail(400);
            return;
        }
        if (request.method() != HttpMethod.POST) {
            ctx.response().putHeader(HttpHeaders.ALLOW, "POST");
            LOG.errorf("Invalid HTTP method %s [connectionId: %s]", ctx.request().method(), connectionId);
            ctx.fail(405);
            return;
        }
        McpConnection connection = connectionManager.get(connectionId);
        if (connection == null) {
            LOG.errorf("Connection not found: %s", connectionId);
            ctx.fail(400);
            return;
        }
        SseResponder responder = new SseResponder(trafficLogger, (SseMcpConnection) connection);

        JsonObject message;
        try {
            message = ctx.body().asJsonObject();
        } catch (Exception e) {
            String msg = "Unable to parse the JSON message";
            LOG.errorf(e, msg);
            responder.sendError(null, JsonRPC.PARSE_ERROR, msg);
            ctx.end();
            return;
        }
        if (trafficLogger != null) {
            trafficLogger.messageReceived(message);
        }
        if (JsonRPC.validate(message, responder)) {
            handle(message, connection, responder);
        }
        ctx.end();
    }

    class SseResponder implements Responder {

        final SseMcpConnection connection;
        final TrafficLogger trafficLogger;

        SseResponder(TrafficLogger trafficLogger, SseMcpConnection connection) {
            this.trafficLogger = trafficLogger;
            this.connection = connection;
        }

        @Override
        public void send(JsonObject message) {
            if (message == null) {
                return;
            }
            if (trafficLogger != null) {
                trafficLogger.messageSent(message);
            }
            connection.sendEvent("message", message.encode());
        }

    }
}
