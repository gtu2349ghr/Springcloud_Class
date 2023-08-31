package com.springcloud.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
    @Value("${server.port}")
    private  String serverPort;
        @GetMapping(value = "/index/{id}")
        public String index(@PathVariable("id") Integer id) {
            return "Hello Nacos serverport " +serverPort+ "id "+id;
        }

}
