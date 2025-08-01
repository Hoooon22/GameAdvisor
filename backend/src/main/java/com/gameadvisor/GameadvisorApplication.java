package com.gameadvisor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan("com.gameadvisor")
@EnableJpaRepositories("com.gameadvisor")
public class GameadvisorApplication {

	public static void main(String[] args) {
		SpringApplication.run(GameadvisorApplication.class, args);
	}

}
