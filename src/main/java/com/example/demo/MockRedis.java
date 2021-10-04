package com.example.demo;

import org.springframework.stereotype.Component;

@Component
public class MockRedis {
    private String sessionId;

    public void save(String sessionId) {
        this.sessionId = sessionId;
    }

    public boolean isLatest(String sessionId) {
        return sessionId.equals(this.sessionId);
    }

    public void delete(String sessionId) {
        sessionId = null;
    }

    public String getSessionId() {
        return sessionId;
    }
}
