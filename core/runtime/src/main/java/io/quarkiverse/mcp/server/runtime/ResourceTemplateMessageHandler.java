package io.quarkiverse.mcp.server.runtime;

import org.jboss.logging.Logger;

import io.quarkiverse.mcp.server.ResourceTemplateManager;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

class ResourceTemplateMessageHandler {

    private static final Logger LOG = Logger.getLogger(ResourceTemplateMessageHandler.class);

    private final ResourceTemplateManagerImpl manager;

    private final int pageSize;

    ResourceTemplateMessageHandler(ResourceTemplateManagerImpl manager, int pageSize) {
        this.manager = manager;
        this.pageSize = pageSize;
    }

    void resourceTemplatesList(JsonObject message, Responder responder) {
        Object id = message.getValue("id");
        Cursor cursor = Messages.getCursor(message, responder);

        LOG.debugf("List resource templates [id: %s, cursor: %s]", id, cursor);

        JsonArray templates = new JsonArray();
        JsonObject result = new JsonObject().put("resourceTemplates", templates);
        Page<ResourceTemplateManager.ResourceTemplateInfo> page = manager.fetchPage(cursor, pageSize);
        for (ResourceTemplateManager.ResourceTemplateInfo info : page) {
            templates.add(info.asJson());
        }
        if (page.hasNextCursor()) {
            ResourceTemplateManager.ResourceTemplateInfo last = page.lastInfo();
            result.put("nextCursor", Cursor.encode(last.createdAt(), last.name()));
        }
        responder.sendResult(id, result);
    }

}
