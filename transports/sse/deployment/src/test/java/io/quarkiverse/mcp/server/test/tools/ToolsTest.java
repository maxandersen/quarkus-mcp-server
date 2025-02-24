package io.quarkiverse.mcp.server.test.tools;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkiverse.mcp.server.test.Checks;
import io.quarkiverse.mcp.server.test.FooService;
import io.quarkiverse.mcp.server.test.McpServerTest;
import io.quarkiverse.mcp.server.test.Options;
import io.quarkus.test.QuarkusUnitTest;
import io.restassured.http.ContentType;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class ToolsTest extends McpServerTest {

    @RegisterExtension
    static final QuarkusUnitTest config = defaultConfig()
            .withApplicationRoot(
                    root -> root.addClasses(FooService.class, Options.class, Checks.class, MyTools.class));

    @Test
    public void testTools() throws URISyntaxException {
        URI endpoint = initClient();

        JsonObject toolListMessage = newMessage("tools/list");

        given().contentType(ContentType.JSON)
                .when()
                .body(toolListMessage.encode())
                .post(endpoint)
                .then()
                .statusCode(200);

        JsonObject toolListResponse = waitForLastJsonMessage();

        JsonObject toolListResult = assertResponseMessage(toolListMessage, toolListResponse);
        assertNotNull(toolListResult);
        JsonArray tools = toolListResult.getJsonArray("tools");
        assertEquals(4, tools.size());

        // alpha, bravo, uni_alpha, uni_bravo
        assertTool(tools.getJsonObject(0), "alpha", null, schema -> {
            JsonObject properties = schema.getJsonObject("properties");
            assertEquals(1, properties.size());
            JsonObject priceProperty = properties.getJsonObject("price");
            assertNotNull(priceProperty);
            assertEquals("integer", priceProperty.getString("type"));
            assertEquals("Define the price...", priceProperty.getString("description"));
        });
        assertTool(tools.getJsonObject(2), "uni_alpha", null, schema -> {
            JsonObject properties = schema.getJsonObject("properties");
            assertEquals(1, properties.size());
            JsonObject priceProperty = properties.getJsonObject("uni_price");
            assertNotNull(priceProperty);
            assertEquals("number", priceProperty.getString("type"));
        });

        assertToolCall("Hello 1!", endpoint, "alpha", new JsonObject()
                .put("price", 1));
        assertToolCall("Hello 1.0!", endpoint, "uni_alpha", new JsonObject()
                .put("uni_price", 1));
        assertToolCall("Hello 1!", endpoint, "bravo", new JsonObject()
                .put("price", 1));
        assertToolCall("Hello 1!", endpoint, "uni_bravo", new JsonObject()
                .put("price", 1));
    }

    private void assertTool(JsonObject tool, String name, String description, Consumer<JsonObject> inputSchemaAsserter) {
        assertEquals(name, tool.getString("name"));
        if (description != null) {
            assertEquals(description, tool.getString("description"));
        }
        if (inputSchemaAsserter != null) {
            inputSchemaAsserter.accept(tool.getJsonObject("inputSchema"));
        }
    }

    private void assertToolCall(String expectedText, URI endpoint, String name, JsonObject arguments) {
        JsonObject toolGetMessage = newMessage("tools/call")
                .put("params", new JsonObject()
                        .put("name", name)
                        .put("arguments", arguments));

        given()
                .contentType(ContentType.JSON)
                .when()
                .body(toolGetMessage.encode())
                .post(endpoint)
                .then()
                .statusCode(200);

        JsonObject toolGetResponse = waitForLastJsonMessage();

        JsonObject toolGetResult = assertResponseMessage(toolGetMessage, toolGetResponse);
        assertNotNull(toolGetResult);
        JsonArray content = toolGetResult.getJsonArray("content");
        assertEquals(1, content.size());
        JsonObject textContent = content.getJsonObject(0);
        assertEquals("text", textContent.getString("type"));
        assertEquals(expectedText, textContent.getString("text"));
    }

}
