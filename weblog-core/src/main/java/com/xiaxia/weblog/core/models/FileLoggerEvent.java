package com.xiaxia.weblog.core.models;

import lombok.Data;

@Data
public class FileLoggerEvent {
    private String log;
    private Integer index;
}
