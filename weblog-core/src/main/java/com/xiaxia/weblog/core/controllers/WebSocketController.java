package com.xiaxia.weblog.core.controllers;

import com.xiaxia.weblog.core.properties.WeblogConfig;
import com.xiaxia.weblog.core.properties.WeblogConfigPath;
import com.xiaxia.weblog.core.publish.FileLogListening;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

@Controller
public class WebSocketController {
    @Autowired
    public SimpMessagingTemplate messagingTemplate;
    @Autowired
    private WeblogConfig config;


    @MessageMapping("/weblog/init")
    public void queuw(Map rm) {
        messagingTemplate.convertAndSendToUser(rm.get("weblogClientId").toString(), "/init", config.getLogPaths());
    }

    @MessageMapping("/weblog/need")
    public void need(Map rm) throws FileNotFoundException {
        messagingTemplate.convertAndSend(config.getTopicPrefix() + "/weblog/pullFileLogger/" + rm.get("index").toString(), xx(Integer.parseInt(rm.get("index").toString())));
    }

    private String xx(Integer index) throws FileNotFoundException {
        WeblogConfigPath weblogConfigPath = config.getLogPaths().get(index);
        File file = ResourceUtils.getFile(weblogConfigPath.getPath());
        FileLogListening.XxRunnable.XXX xx = FileLogListening.XxRunnable.xx(file, (o) -> {
                    try {
                        return o.length() - 50000;
                    } catch (IOException e) {
                        return -1L;
                    }
                }
        );
        return xx.getStr();
    }

}