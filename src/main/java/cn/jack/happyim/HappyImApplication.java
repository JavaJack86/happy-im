package cn.jack.happyim;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HappyImApplication {

    public static void main(String[] args) {
        SpringApplication.run(HappyImApplication.class, args);
    }

}
