package org.example.be_sp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BeSpApplication {

    public static void main(String[] args) {
        SpringApplication.run(BeSpApplication.class, args);

    }

}
