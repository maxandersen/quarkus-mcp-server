package io.quarkiverse.mcp.server.test.prompts;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URISyntaxException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkiverse.mcp.server.runtime.JsonRPC;
import io.quarkiverse.mcp.server.test.Checks;
import io.quarkiverse.mcp.server.test.FooService;
import io.quarkiverse.mcp.server.test.McpServerTest;
import io.quarkiverse.mcp.server.test.Options;
import io.quarkus.test.QuarkusUnitTest;
import io.vertx.core.json.JsonObject;

public class InvalidPromptNameTest extends McpServerTest {

    @RegisterExtension
    static final QuarkusUnitTest config = defaultConfig()
            .withApplicationRoot(
                    root -> root.addClasses(FooService.class, Options.class, Checks.class, MyPrompts.class));

    @Test
    public void testError() throws URISyntaxException {
        initClient();
        JsonObject message = newMessage("prompts/get")
                .put("params", new JsonObject()
                        .put("name", "nonexistent")
                        .put("arguments", new JsonObject()));
        send(message);
        JsonObject response = waitForLastResponse();
        assertEquals(JsonRPC.INVALID_PARAMS, response.getJsonObject("error").getInteger("code"));
        assertEquals("Invalid prompt name: nonexistent", response.getJsonObject("error").getString("message"));
    }

}
