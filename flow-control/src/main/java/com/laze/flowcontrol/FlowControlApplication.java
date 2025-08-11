package com.laze.flowcontrol;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@RequiredArgsConstructor
public class FlowControlApplication {

	public static void main(String[] args) {
		SpringApplication.run(FlowControlApplication.class, args);
	}

}