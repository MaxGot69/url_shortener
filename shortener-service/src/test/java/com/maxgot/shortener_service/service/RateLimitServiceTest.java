package com.maxgot.shortener_service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RateLimitServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private RateLimitService rateLimitService;

    private final String TEST_KEY = "rate_limit:127.0.0.1";

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    /**
     * Этот тест проверяет, что первый запрос с нового IP не заблокирован (счётчик = 1)
     */
    @Test
    void isRateLimited_firstRequest_returnsFalse() {
        // Arrange
        when(valueOperations.increment(TEST_KEY)).thenReturn(1L);

        // Act
        boolean result = rateLimitService.isRateLimited(TEST_KEY);

        // Assert
        assertThat(result).isFalse();
    }

    /**
     * Этот тест проверяет, что 11-й запрос с того же IP заблокирован (счётчик = 11)
     */
    @Test
    void isRateLimited_eleventhRequest_returnsTrue() {
        // Arrange
        when(valueOperations.increment(TEST_KEY)).thenReturn(11L);

        // Act
        boolean result = rateLimitService.isRateLimited(TEST_KEY);

        // Assert
        assertThat(result).isTrue();
    }

    /**
     * Этот тест проверяет, что при первом запросе устанавливается TTL на ключ
     */
    @Test
    void isRateLimited_firstRequest_setsTtl() {
        // Arrange
        when(valueOperations.increment(TEST_KEY)).thenReturn(1L);

        // Act
        rateLimitService.isRateLimited(TEST_KEY);

        // Assert
        verify(redisTemplate, times(1)).expire(eq(TEST_KEY), anyLong(), eq(TimeUnit.SECONDS));
    }

    /**
     * Этот тест проверяет, что последующие запросы (счётчик != 1) не сбрасывают TTL
     */
    @Test
    void isRateLimited_subsequentRequests_doNotResetTtl() {
        // Arrange
        when(valueOperations.increment(TEST_KEY)).thenReturn(5L);

        // Act
        rateLimitService.isRateLimited(TEST_KEY);

        // Assert
        verify(redisTemplate, never()).expire(any(String.class), anyLong(), any(TimeUnit.class));
    }
}
