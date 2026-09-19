// 만세력 백엔드 애플리케이션을 시작함.
package com.fourpillars.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class FourPillarsBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(FourPillarsBackendApplication.class, args);
	}

}
