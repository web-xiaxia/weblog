package tset.com.xiaxia.weblog.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@SpringBootApplication
@EnableScheduling
@Slf4j
public class WeblogTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(WeblogTestApplication.class, args);
    }

    @Scheduled(fixedRate = 2000)
    public void outputLogger() {
        final String s = "测试日志输出" + System.currentTimeMillis() / 1000;
        log.error(s, new RuntimeException(s));
    }
}
