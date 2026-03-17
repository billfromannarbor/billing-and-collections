package org.aaa.billingandcollections;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class BillingAndCollectionsApplication {

    public static void main(String[] args) {
        SpringApplication.run(BillingAndCollectionsApplication.class, args);
    }
}