package dev.yukmekim.settlement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class SettlementBatchApplication {

	public static void main(String[] args) {
		SpringApplication.run(SettlementBatchApplication.class, args);
	}

}
