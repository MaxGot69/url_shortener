package com.maxgot.shortener_service.service;

import com.maxgot.shortener_service.dto.CreateLinkRequest;
import com.maxgot.shortener_service.dto.LinkResponse;
import com.maxgot.shortener_service.entity.Link;
import com.maxgot.shortener_service.event.LinkClickedEvent;
import com.maxgot.shortener_service.exception.LinkExpiredException;
import com.maxgot.shortener_service.exception.LinkNotFoundException;
import com.maxgot.shortener_service.repository.LinkRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
public class LinkService {
    private final LinkRepository linkRepository;
    private final KafkaTemplate<String, LinkClickedEvent> kafkaTemplate;

    public LinkService(LinkRepository linkRepository,  KafkaTemplate<String, LinkClickedEvent> kafkaTemplate) {
        this.linkRepository = linkRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    public LinkResponse createLink(CreateLinkRequest request) {
        String originalUrl;
        String shortCode;
        /*
        - Сгенерировать `shortCode` через `UUID.randomUUID().toString().substring(0, 8)`
        - Сохранить в БД
        - Вернуть `LinkResponse`
         */
        originalUrl = request.getOriginalUrl();

        shortCode =  UUID.randomUUID().toString().substring(0, 8);

        Link link = new Link();
        link.setOriginalUrl(originalUrl);
        link.setShortCode(shortCode);
        link.setClicks(0L);
        link.setCreatedAt(LocalDateTime.now());
        link.setExpiresAt(LocalDateTime.now().plusDays(30));

        Link savedLink = linkRepository.save(link);
        return new LinkResponse(
                savedLink.getShortCode(),
                "http://localhost:8080/" + savedLink.getShortCode()
        );
    }

    public Link getLinkByShortCode(String shortCode, String userAgent) {
            Link link = linkRepository.findByShortCode(shortCode)
                    .orElseThrow(() -> new LinkNotFoundException("Link not found: " + shortCode));
            //проверить срок действия (expiresAt)
        if (link.getExpiresAt() != null && LocalDateTime.now().isAfter(link.getExpiresAt())){
            throw new LinkExpiredException("Link expired: " + shortCode);
        }
        link.setClicks(link.getClicks() + 1);
        linkRepository.save(link);
        //создание события
        LinkClickedEvent event = new LinkClickedEvent(
                shortCode,
                link.getOriginalUrl(),
                LocalDateTime.now(),
                userAgent,
                UUID.randomUUID().toString()
        );
        //отправка
        try{
            kafkaTemplate.send("link-clicks", shortCode, event);
        }catch (Exception e){
            log.warn("Не удалось отправить событие: {}", e.getMessage());
        }
        return link;
    }

    @Cacheable
    public LinkResponse getInfoByShortCode(String shortCode) {
        //найти
        Link link = linkRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new LinkNotFoundException("Link not found: " + shortCode));
        //чек срок
        if (link.getExpiresAt() != null && LocalDateTime.now().isAfter(link.getExpiresAt())){
            throw new LinkExpiredException("Link expired: " + shortCode);
        }
        String shortUrl = "http://localhost:8080/" + link.getShortCode();
        return new LinkResponse(link.getShortCode(), shortUrl);
    }

    @CacheEvict
    public void deleteLink(String shortCode) {
        Link link = linkRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new LinkNotFoundException("Link not found: " + shortCode));
        linkRepository.delete(link);
    }

}
