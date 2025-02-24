# Quarkus Model Context Protocol (MCP) Server
<!-- ALL-CONTRIBUTORS-BADGE:START - Do not remove or modify this section -->
[![All Contributors](https://img.shields.io/badge/all_contributors-1-orange.svg?style=flat-square)](#contributors-)
<!-- ALL-CONTRIBUTORS-BADGE:END -->

[![Version](https://img.shields.io/maven-central/v/io.quarkiverse.mcp/quarkus-mcp-server?logo=apache-maven&style=flat-square)](https://central.sonatype.com/artifact/io.quarkiverse.mcp/quarkus-mcp-server-parent)

_"[Model Context Protocol](https://modelcontextprotocol.io/) (MCP) is an open protocol that enables seamless integration between LLM applications and external data sources and tools."_

This extension provides a declarative API that enables developers to implement the MCP server features easily.

## Get Started

Step #1 - add the following dependency to your POM file:

```xml
<dependency>
    <groupId>io.quarkiverse.mcp</groupId>
    <artifactId>quarkus-mcp-server-sse</artifactId> <!-- use 'quarkus-mcp-server-stdio' if you want to use the STDIO transport instead of the SSE transport -->
    <version>${project-version}</version>
</dependency>
```

Step #2 - add server features (prompts, resources and tools) represented by an _annotated business method_ of a CDI bean.

```java
import jakarta.inject.Inject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import io.quarkiverse.mcp.server.BlobResourceContents;
import io.quarkiverse.mcp.server.Prompt;
import io.quarkiverse.mcp.server.PromptArg;
import io.quarkiverse.mcp.server.PromptMessage;

import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.Resource;
import io.quarkiverse.mcp.server.TextContent;

// This class is automatically registered as a @Singleton CDI bean
public class ServerFeatures {

    @Inject
    CodeService codeService;

    @Tool
    TextContent toLowerCase(String value) {
        return new TextContent(value.toLowerCase());
    }

    @Prompt(name = "code_assist")
    PromptMessage codeAssist(@PromptArg(name = "lang") String language) {
        return PromptMessage.withUserRole(new TextContent(codeService.assist(language)));
    }

    @Resource(uri = "file:///project/alpha")
    BlobResourceContents alpha(String uri) throws IOException{
        return BlobResourceContents.create(uri, Files.readAllBytes(Paths.ALPHA));
    }

}
```

Step #3 - run your Quarkus app!

Read the full [documentation](https://quarkiverse.github.io/quarkiverse-docs/quarkus-mcp-server/dev/index.html).

## Contributors ✨

Thanks goes to these wonderful people ([emoji key](https://allcontributors.org/docs/en/emoji-key)):

<!-- ALL-CONTRIBUTORS-LIST:START - Do not remove or modify this section -->
<!-- prettier-ignore-start -->
<!-- markdownlint-disable -->
<table>
  <tbody>
    <tr>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/mkouba"><img src="https://avatars.githubusercontent.com/u/913004?v=4?s=100" width="100px;" alt="Martin Kouba"/><br /><sub><b>Martin Kouba</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkus-mcp-server/commits?author=mkouba" title="Code">💻</a> <a href="#maintenance-mkouba" title="Maintenance">🚧</a></td>
    </tr>
  </tbody>
</table>

<!-- markdownlint-restore -->
<!-- prettier-ignore-end -->

<!-- ALL-CONTRIBUTORS-LIST:END -->

This project follows the [all-contributors](https://github.com/all-contributors/all-contributors) specification. Contributions of any kind welcome!
