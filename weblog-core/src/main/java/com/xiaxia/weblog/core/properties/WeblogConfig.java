package com.xiaxia.weblog.core.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "weblog")
public class WeblogConfig {
    private List<WeblogConfigPath> logPaths;
    private String endpoint = "/websocket/weblog";
    private String topicPrefix = "/topic";
    private String userDestinationPrefix = "/user";
    private String applicationDestinationPrefix = "/app";
}
