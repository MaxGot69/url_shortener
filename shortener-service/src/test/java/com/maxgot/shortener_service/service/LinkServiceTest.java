package com.maxgot.shortener_service.service;

import com.maxgot.shortener_service.dto.CreateLinkRequest;
import com.maxgot.shortener_service.dto.LinkResponse;
import com.maxgot.shortener_service.entity.Link;
import com.maxgot.shortener_service.exception.LinkExpiredException;
import com.maxgot.shortener_service.exception.LinkNotFoundException;
import com.maxgot.shortener_service.repository.LinkRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LinkServiceTest {

    @Mock
    private LinkRepository linkRepository;
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private LinkService linkService;

    private final String TEST_SHORT_CODE = "abc123";
    private final String TEST_ORIGINAL_URL = "https://example.com";

    /**
     * Этот тест проверяет, что при создании ссылки сервис:
     * - Сохраняет сущность в БД
     * - Возвращает LinkResponse с правильными shortCode и shortUrl
     */
    @Test
    void createLink_shouldSaveAndReturnResponse() {
        // Arrange
        CreateLinkRequest request = new CreateLinkRequest();
        request.setOriginalUrl(TEST_ORIGINAL_URL);

        Link savedLink = new Link();
        savedLink.setId(1L);
        savedLink.setShortCode(TEST_SHORT_CODE);
        savedLink.setOriginalUrl(TEST_ORIGINAL_URL);
        savedLink.setClicks(0L);
        savedLink.setCreatedAt(LocalDateTime.now());
        savedLink.setExpiresAt(LocalDateTime.now().plusDays(30));

        when(linkRepository.save(any(Link.class))).thenReturn(savedLink);

        // Act
        LinkResponse response = linkService.createLink(request);

        // Assert
        verify(linkRepository).save(any(Link.class));
        assertThat(response.getShortCode()).isNotNull().isNotBlank();
        assertThat(response.getShortUrl()).isEqualTo("http://localhost:8080/" + response.getShortCode());
    }

    /**
     * Этот тест проверяет, что при поиске по существующему shortCode сервис:
     * - Возвращает найденную сущность
     * - Увеличивает счётчик кликов на 1
     * - Сохраняет обновлённую сущность
     */
    @Test
    void getLinkByShortCode_shouldReturnLinkAndIncrementClicks() {
        // Arrange
        Link existingLink = new Link();
        existingLink.setId(1L);
        existingLink.setShortCode(TEST_SHORT_CODE);
        existingLink.setOriginalUrl(TEST_ORIGINAL_URL);
        existingLink.setClicks(5L);
        existingLink.setCreatedAt(LocalDateTime.now());
        existingLink.setExpiresAt(LocalDateTime.now().plusDays(30));

        when(linkRepository.findByShortCode(TEST_SHORT_CODE))
                .thenReturn(Optional.of(existingLink));

        // Act
        Link result = linkService.getLinkByShortCode(TEST_SHORT_CODE, "test-user-agent");

        // Assert
        assertThat(result).isEqualTo(existingLink);
        assertThat(result.getClicks()).isEqualTo(6L);
        verify(linkRepository).save(existingLink);
    }

    /**
     * Этот тест проверяет, что при поиске по несуществующему shortCode сервис выбрасывает LinkNotFoundException
     */
    @Test
    void getLinkByShortCode_whenLinkNotFound_shouldThrowException() {
        // Arrange
        String nonExistentShortCode = "nonExistent123";
        when(linkRepository.findByShortCode(nonExistentShortCode))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> linkService.getLinkByShortCode(nonExistentShortCode, "test-user-agent"))
                .isInstanceOf(LinkNotFoundException.class)
                .hasMessageContaining("Link not found: " + nonExistentShortCode);
    }

    /**
     * Этот тест проверяет, что при обращении к просроченной ссылке сервис выбрасывает LinkExpiredException
     */
    @Test
    void getLinkByShortCode_whenLinkExpired_shouldThrowException() {
        // Arrange
        Link expiredLink = new Link();
        expiredLink.setId(1L);
        expiredLink.setShortCode(TEST_SHORT_CODE);
        expiredLink.setOriginalUrl(TEST_ORIGINAL_URL);
        expiredLink.setClicks(0L);
        expiredLink.setCreatedAt(LocalDateTime.now().minusDays(10));
        expiredLink.setExpiresAt(LocalDateTime.now().minusDays(1)); // Просрочена на 1 день

        when(linkRepository.findByShortCode(TEST_SHORT_CODE))
                .thenReturn(Optional.of(expiredLink));

        // Act & Assert
        assertThatThrownBy(() -> linkService.getLinkByShortCode(TEST_SHORT_CODE, "test-user-agent"))
                .isInstanceOf(LinkExpiredException.class)
                .hasMessageContaining("Link expired: " + TEST_SHORT_CODE);
    }

    /**
     * Этот тест проверяет, что getInfoByShortCode возвращает LinkResponse с правильными данными
     * для существующей и не просроченной ссылки
     */
    @Test
    void getInfoByShortCode_shouldReturnLinkResponse() {
        // Arrange
        Link existingLink = new Link();
        existingLink.setId(1L);
        existingLink.setShortCode(TEST_SHORT_CODE);
        existingLink.setOriginalUrl(TEST_ORIGINAL_URL);
        existingLink.setClicks(5L);
        existingLink.setCreatedAt(LocalDateTime.now());
        existingLink.setExpiresAt(LocalDateTime.now().plusDays(30));

        when(linkRepository.findByShortCode(TEST_SHORT_CODE))
                .thenReturn(Optional.of(existingLink));

        // Act
        LinkResponse response = linkService.getInfoByShortCode(TEST_SHORT_CODE);

        // Assert
        assertThat(response.getShortCode()).isEqualTo(TEST_SHORT_CODE);
        assertThat(response.getShortUrl()).isEqualTo("http://localhost:8080/" + TEST_SHORT_CODE);
    }

    /**
     * Этот тест проверяет, что deleteLink вызывает репозиторий для удаления
     */
    @Test
    void deleteLink_shouldDeleteLink() {
        // Arrange
        Link existingLink = new Link();
        existingLink.setId(1L);
        existingLink.setShortCode(TEST_SHORT_CODE);
        existingLink.setOriginalUrl(TEST_ORIGINAL_URL);

        when(linkRepository.findByShortCode(TEST_SHORT_CODE))
                .thenReturn(Optional.of(existingLink));

        // Act
        linkService.deleteLink(TEST_SHORT_CODE);

        // Assert
        verify(linkRepository).delete(existingLink);
    }

    /**
     * Этот тест проверяет, что deleteLink выбрасывает LinkNotFoundException, если ссылка не найдена
     */
    @Test
    void deleteLink_whenLinkNotFound_shouldThrowException() {
        // Arrange
        String nonExistentShortCode = "nonExistent123";
        when(linkRepository.findByShortCode(nonExistentShortCode))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> linkService.deleteLink(nonExistentShortCode))
                .isInstanceOf(LinkNotFoundException.class)
                .hasMessageContaining("Link not found: " + nonExistentShortCode);
    }
}