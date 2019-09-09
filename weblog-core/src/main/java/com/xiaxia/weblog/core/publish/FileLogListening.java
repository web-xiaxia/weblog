package com.xiaxia.weblog.core.publish;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.xiaxia.weblog.core.consumers.FileLoggerEventHandler;
import com.xiaxia.weblog.core.models.FileLoggerEvent;
import com.xiaxia.weblog.core.properties.WeblogConfig;
import com.xiaxia.weblog.core.properties.WeblogConfigPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ResourceUtils;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Component
public class FileLogListening {
    private Logger logger = LoggerFactory.getLogger(FileLogListening.class);

    @Autowired
    private Environment environment;
    @Autowired
    private WeblogConfig config;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * 监听日志文件
     *
     * @throws IOException
     */
    @PostConstruct
    public void start() {
        List<WeblogConfigPath> logPaths = config.getLogPaths();
        if (CollectionUtils.isEmpty(logPaths)) {
            return;
        }


        for (int i = 0; i < logPaths.size(); i++) {
            try {
                String logPath = logPaths.get(i).getPath();
                //ThreadFactory
                //指定文件可读可写
                ScheduledExecutorService exec = new ScheduledThreadPoolExecutor(2, (o) -> new Thread(o, "FileLogListening"));
                exec.scheduleWithFixedDelay(new XxRunnable(logPath, i, messagingTemplate, config.getTopicPrefix()), 0, 1, TimeUnit.SECONDS);
            } catch (IOException e) {
                logger.error("文件日志读取错误 {}", i, e);
            }
        }

    }


    public static class XxRunnable implements Runnable {
        /**
         * 上次文件大小
         */
        private long lastTimeFileSize = 0;

        private final File logFile;
        private final int index;

        private static final Map<String, String> COLOR_MAP = new HashMap<String, String>() {
            {
                put("30", "#000000");
                put("31", "#FF0000");
                put("32", "#00CC00");
                put("33", "#FFFF00");
                put("34", "#3366FF");
                put("35", "#CC33CC");
                put("36", "#66FFFF");
                put("37", "#FFFFFF");
            }
        };

        private final RingBuffer<FileLoggerEvent> ringBuffer;


        private XxRunnable(String logPath, int index, SimpMessagingTemplate messagingTemplate, String topicPrefix) throws FileNotFoundException {

            this.logFile = ResourceUtils.getFile(logPath);

            this.index = index;

            Disruptor<FileLoggerEvent> fileLoggerEventDisruptor = new Disruptor<>(FileLoggerEvent::new, 2 * 1024, Executors.defaultThreadFactory());
            fileLoggerEventDisruptor.handleEventsWith(new FileLoggerEventHandler(messagingTemplate, topicPrefix));
            this.ringBuffer = fileLoggerEventDisruptor.getRingBuffer();
            fileLoggerEventDisruptor.start();
        }

        public static class XXX {
            private String str;
            private long lastTimeFileSize;

            public String getStr() {
                return str;
            }

            public long getLastTimeFileSize() {
                return lastTimeFileSize;
            }
        }


        public static String xx(String x) {
            String s = x.replace(" ", "&nbsp;")
                    .replaceAll("\t", "&nbsp;&nbsp;&nbsp;&nbsp;")
                    .replaceAll(((char) 27) + "\\[0;39m", "</span>")
                    .replaceAll(((char) 27) + "\\[(\\d{1,2})m", "<span style='color:$1'>");


            for (Map.Entry<String, String> entry : COLOR_MAP.entrySet()) {
                s = s.replaceAll("style='color:" + entry.getKey() + "'", "style='color:" + entry.getValue() + "'");
            }
            return s;
        }

        public static XXX xx(File logFile, Function<RandomAccessFile, Long> function) {
            XXX xxx = new XXX();
            xxx.lastTimeFileSize = -1;
            xxx.str = "";
            if (!logFile.exists() || !logFile.isFile()) {
                return xxx;
            }
            try (RandomAccessFile randomFile = new RandomAccessFile(logFile, "r")) {

                long lastTimeFileSize = function.apply(randomFile);
                if (lastTimeFileSize < 0) {
                    lastTimeFileSize = randomFile.length() - 1000;
                }
                if (randomFile.length() < lastTimeFileSize) {
                    lastTimeFileSize = randomFile.length() - 1000;
                }

                if (lastTimeFileSize < 0) {
                    lastTimeFileSize = 0;
                }

                randomFile.seek(lastTimeFileSize);
                String tmp = "";
                StringBuilder stringBuilder = new StringBuilder();
                while ((tmp = randomFile.readLine()) != null) {
                    stringBuilder.append(tmp).append("<br>");
                }
                xxx.lastTimeFileSize = randomFile.length();
                if (stringBuilder.length() > 0) {
                    xxx.str = new String(xx(stringBuilder.toString()).getBytes(StandardCharsets.ISO_8859_1));
                }

            } catch (IOException e) {
                //
            }
            return xxx;
        }

        @Override
        public void run() {
            XXX xx = xx(logFile, (o) -> lastTimeFileSize);

            if (xx.lastTimeFileSize > 0) {
                lastTimeFileSize = xx.lastTimeFileSize;
            }

            if (xx.str.length() > 0) {
                long sequence = ringBuffer.next();
                try {
                    // Get the entry in the Disruptor
                    FileLoggerEvent event = ringBuffer.get(sequence);
                    // for the sequence
                    // Fill with data
                    event.setLog(xx.str);
                    event.setIndex(index);
                } finally {
                    ringBuffer.publish(sequence);
                }
            }
        }
    }

}