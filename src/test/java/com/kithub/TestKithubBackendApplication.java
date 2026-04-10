package com.kithub;

import org.springframework.boot.SpringApplication;

public class TestKithubBackendApplication {

	public static void main(String[] args) {
		SpringApplication.from(KithubBackendApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
