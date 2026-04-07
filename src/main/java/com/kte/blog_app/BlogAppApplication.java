package com.kte.blog_app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.webflux.autoconfigure.WebFluxAutoConfiguration;

@SpringBootApplication(exclude = WebFluxAutoConfiguration.class)
public class BlogAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(BlogAppApplication.class, args);
	}

}
