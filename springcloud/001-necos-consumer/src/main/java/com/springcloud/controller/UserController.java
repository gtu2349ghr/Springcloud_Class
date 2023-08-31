package com.springcloud.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;

@RestController
public class UserController {
    @Resource
    RestTemplate restTemplate;
    @Value("${server-url.nacos-user-service}")
    private String URL;
        @GetMapping(value = "/consumer/index/{id}")
        public String echo(@PathVariable("id") Integer id) {
            return restTemplate.getForObject(URL+"/index/"+id,String.class);
        }

}
