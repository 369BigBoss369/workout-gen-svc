package com.workoutgensvc.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AIServiceTest {

    @Test
    void isValidUrl_ShouldReturnFalse_ForNullUrl() {
        AIService aiService = new AIService("gpt-3.5-turbo", "http://localhost:5000", "test-token");

        boolean result = aiService.isValidUrl(null);

        assertFalse(result);
    }

    @Test
    void isValidUrl_ShouldReturnFalse_ForEmptyUrl() {
        AIService aiService = new AIService("gpt-3.5-turbo", "http://localhost:5000", "test-token");

        boolean result = aiService.isValidUrl("");

        assertFalse(result);
    }

    @Test
    void isValidUrl_ShouldReturnFalse_ForWhitespaceOnlyUrl() {
        AIService aiService = new AIService("gpt-3.5-turbo", "http://localhost:5000", "test-token");

        boolean result = aiService.isValidUrl("   ");

        assertFalse(result);
    }
}
