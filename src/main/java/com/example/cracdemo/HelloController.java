package com.example.cracdemo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Value;

@RestController
public class HelloController {

    @Value("${env:Hello from application.yml}")
    private String env;

    @GetMapping("/hello")
    public String hello() {
        return "Caught You! env='" + env + "'";
    }
}
