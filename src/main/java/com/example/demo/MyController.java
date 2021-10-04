package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
public class MyController {

    @Autowired
    private MockRedis mockRedis;

    @GetMapping("/hello-world")
    public ResponseEntity<Map> helloWorld(HttpServletRequest httpRequest) {
        return ResponseEntity.ok(Map.of("current", httpRequest.getSession().getId(), "redis", mockRedis.getSessionId()));
    }

}
