package com.xiaxia.weblog.core.consumers;

import com.lmax.disruptor.EventHandler;
import com.xiaxia.weblog.core.models.FileLoggerEvent;
import org.springframework.messaging.simp.SimpMessagingTemplate;

public class FileLoggerEventHandler implements EventHandler<FileLoggerEvent> {

    private final SimpMessagingTemplate messagingTemplate;
    private final String topicPrefix;

    public FileLoggerEventHandler(final SimpMessagingTemplate messagingTemplate, final String topicPrefix) {
        this.messagingTemplate = messagingTemplate;
        this.topicPrefix = topicPrefix;
    }

    @Override
    public void onEvent(FileLoggerEvent fileLoggerEvent, long l, boolean b) {
        messagingTemplate.convertAndSend(topicPrefix + "/weblog/pullFileLogger/" + fileLoggerEvent.getIndex(), fileLoggerEvent.getLog());
    }
}
