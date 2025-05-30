package br.com.easylink.easylinkservice.infrastructure.api.controller;

import br.com.easylink.easylinkservice.application.ports.UrlMappingRepositoryPort;
import br.com.easylink.easylinkservice.domain.UrlMapping;
import br.com.easylink.easylinkservice.infrastructure.api.dto.CreateUrlRequestDTO;
import br.com.easylink.easylinkservice.infrastructure.api.dto.UpdateUrlRequestDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;


import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class UrlShortenerControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private UrlMappingRepositoryPort urlMappingRepositoryPort;

    private final String testUser = "test-user";
    private final String baseUrlApi = "/api/v1/urls";

    @BeforeEach
    void setUp() {
        cacheManager.getCacheNames().forEach(cacheName -> {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
        });
    }

    @Test
    @DisplayName("POST /api/v1/urls - Deve criar um novo link encurtado com sucesso")
    void shortenUrl_quandoDadosValidos_deveRetornarCreatedComUrlEncurtada() throws Exception {
        CreateUrlRequestDTO requestDto = new CreateUrlRequestDTO(
                "https://www.example.com/muito/longa/url",
                null, // Sem custom key
                null  // Sem data de expiração
        );

        mockMvc.perform(post(baseUrlApi)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Username", testUser)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.originalUrl", is(requestDto.originalUrl())))
                .andExpect(jsonPath("$.shortKey", is(notNullValue())))
                .andExpect(jsonPath("$.shortUrl", containsString("/")))
                .andExpect(jsonPath("$.createdAt", is(notNullValue())))
                .andExpect(jsonPath("$.expiresAt", is(nullValue())));
    }

    @Test
    @DisplayName("POST /api/v1/urls - Deve criar um novo link encurtado com custom key e data de expiração")
    void shortenUrl_comCustomKeyEDataExpiracao_deveRetornarCreated() throws Exception {
        Instant expiresAt = Instant.now().plus(5, ChronoUnit.DAYS).truncatedTo(ChronoUnit.SECONDS);
        CreateUrlRequestDTO requestDto = new CreateUrlRequestDTO(
                "https://www.another-example.com",
                "meu-link-custom",
                expiresAt
        );

        mockMvc.perform(post(baseUrlApi)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Username", testUser)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.originalUrl", is(requestDto.originalUrl())))
                .andExpect(jsonPath("$.shortKey", is("meu-link-custom")))
                .andExpect(jsonPath("$.expiresAt", is(expiresAt.toString())));
    }


    @Test
    @DisplayName("POST /api/v1/urls - Deve retornar Bad Request para URL original inválida")
    void shortenUrl_quandoUrlOriginalInvalida_deveRetornarBadRequest() throws Exception {
        CreateUrlRequestDTO requestDto = new CreateUrlRequestDTO("url-invalida", null, null);

        mockMvc.perform(post(baseUrlApi)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Username", testUser)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/urls - Deve retornar Conflict para custom key já existente")
    void shortenUrl_quandoCustomKeyJaExiste_deveRetornarConflict() throws Exception {
        urlMappingRepositoryPort.save(new UrlMapping(null, "key-existente", "https://www.original.com", Instant.now(), testUser, null));

        CreateUrlRequestDTO requestDto = new CreateUrlRequestDTO(
                "https://www.another.com",
                "key-existente", //
                null
        );

        mockMvc.perform(post(baseUrlApi)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Username", testUser)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isConflict()); //
    }


    @Test
    @DisplayName("GET /{shortKey} - Deve redirecionar para URL original quando shortKey existe e não está expirada")
    void redirectToOriginalUrl_quandoShortKeyValidaNaoExpirada_deveRedirecionar() throws Exception {
        UrlMapping urlMapping = new UrlMapping(null, "validkey", "https://www.destination.com", Instant.now(), testUser, null);
        urlMappingRepositoryPort.save(urlMapping);

        mockMvc.perform(get("/{shortKey}", "validkey"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(urlMapping.getOriginalUrl()));
    }

    @Test
    @DisplayName("GET /{shortKey} - Deve retornar Not Found quando shortKey não existe")
    void redirectToOriginalUrl_quandoShortKeyNaoExiste_deveRetornarNotFound() throws Exception {
        mockMvc.perform(get("/{shortKey}", "nonexistentkey"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /{shortKey} - Deve retornar Not Found para link expirado e deletá-lo do DB")
    void redirectToOriginalUrl_quandoLinkExpirado_deveRetornarNotFoundEDeletarLinkDoDB() throws Exception {
        String keyToExpire = "expiredkeyfordbtest";
        Instant pastDate = Instant.now().minus(1, ChronoUnit.DAYS);
        UrlMapping expiredMapping = new UrlMapping(null, keyToExpire, "https://www.expired.com", Instant.now().minus(2, ChronoUnit.DAYS), testUser, pastDate);
        urlMappingRepositoryPort.save(expiredMapping);

        // Garante que o link está no banco de dados antes do teste
        assertTrue(urlMappingRepositoryPort.findByShortKey(keyToExpire).isPresent(), "O link deveria existir no banco de dados antes do teste.");

        // Limpa explicitamente esta entrada do cache "redirects" para garantir que o método do use case seja chamado
        Cache redirectsCache = cacheManager.getCache("redirects");
        if (redirectsCache != null) {
            redirectsCache.evictIfPresent(keyToExpire);
        }

        // Faz a requisição HTTP
        mockMvc.perform(get("/{shortKey}", keyToExpire))
                .andExpect(status().isNotFound());

        // Verifica se o link foi deletado do banco de dados após a tentativa de acesso
        assertFalse(urlMappingRepositoryPort.findByShortKey(keyToExpire).isPresent(), "O link deveria ter sido deletado do banco de dados.");
    }


    @Test
    @DisplayName("GET /{shortKey}/qr - Deve retornar imagem PNG do QR Code")
    void getQrCode_quandoShortKeyValida_deveRetornarImagemPng() throws Exception {
        UrlMapping urlMapping = new UrlMapping(null, "qrkey", "https://www.qrcode.com", Instant.now(), testUser, null);
        urlMappingRepositoryPort.save(urlMapping);

        mockMvc.perform(get("/{shortKey}/qr", "qrkey"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG_VALUE));
    }


    @Test
    @DisplayName("PUT /api/v1/urls/{shortKey} - Deve atualizar a URL original com sucesso")
    void updateShortenedUrl_quandoValidoEAutorizado_deveRetornarOkComUrlAtualizada() throws Exception {
        UrlMapping originalMapping = urlMappingRepositoryPort.save(
                new UrlMapping(null, "updatekey", "https://www.originalurl.com", Instant.now(), testUser, null)
        );

        UpdateUrlRequestDTO updateDto = new UpdateUrlRequestDTO("https://www.newupdatedurl.com");

        mockMvc.perform(put(baseUrlApi + "/{shortKey}", "updatekey")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Username", testUser)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.originalUrl", is(updateDto.newOriginalUrl())))
                .andExpect(jsonPath("$.shortKey", is("updatekey")));

        //Verifica no banco
        UrlMapping updatedMapping = urlMappingRepositoryPort.findByShortKey("updatekey").orElseThrow();
        assertEquals(updateDto.newOriginalUrl(), updatedMapping.getOriginalUrl());
    }

    @Test
    @DisplayName("PUT /api/v1/urls/{shortKey} - Deve retornar Forbidden se usuário não for o dono")
    void updateShortenedUrl_quandoUsuarioNaoEDono_deveRetornarForbidden() throws Exception {
        urlMappingRepositoryPort.save(
                new UrlMapping(null, "otheruserkey", "https://www.someurl.com", Instant.now(), "another-owner", null)
        );

        UpdateUrlRequestDTO updateDto = new UpdateUrlRequestDTO("https://www.newurl.com");

        mockMvc.perform(put(baseUrlApi + "/{shortKey}", "otheruserkey")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Username", testUser) // Usuário diferente do dono
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /api/v1/urls/{shortKey} - Deve retornar Not Found se shortKey não existir")
    void updateShortenedUrl_quandoShortKeyNaoExiste_deveRetornarNotFound() throws Exception {
        UpdateUrlRequestDTO updateDto = new UpdateUrlRequestDTO("https://www.newurl.com");

        mockMvc.perform(put(baseUrlApi + "/{shortKey}", "nonexistentputkey")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Username", testUser)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/v1/urls/{shortKey} - Deve deletar o link com sucesso")
    void deleteShortenedUrl_quandoValidoEAutorizado_deveRetornarNoContent() throws Exception {
        urlMappingRepositoryPort.save(
                new UrlMapping(null, "deletekey", "https://www.tobedeleted.com", Instant.now(), testUser, null)
        );

        // Verifica se existe antes
        assertTrue(urlMappingRepositoryPort.findByShortKey("deletekey").isPresent());

        mockMvc.perform(delete(baseUrlApi + "/{shortKey}", "deletekey")
                        .header("X-User-Username", testUser))
                .andExpect(status().isNoContent());

        // Verifica se foi deletado
        assertFalse(urlMappingRepositoryPort.findByShortKey("deletekey").isPresent());
    }

    @Test
    @DisplayName("DELETE /api/v1/urls/{shortKey} - Deve retornar Forbidden se usuário não for o dono")
    void deleteShortenedUrl_quandoUsuarioNaoEDono_deveRetornarForbidden() throws Exception {
        urlMappingRepositoryPort.save(
                new UrlMapping(null, "deletekeyowner", "https://www.ownerurl.com", Instant.now(), "another-owner", null)
        );

        mockMvc.perform(delete(baseUrlApi + "/{shortKey}", "deletekeyowner")
                        .header("X-User-Username", testUser)) // Usuário diferente do dono
                .andExpect(status().isForbidden());

        // Garante que não foi deletado
        assertTrue(urlMappingRepositoryPort.findByShortKey("deletekeyowner").isPresent());
    }

    @Test
    @DisplayName("DELETE /api/v1/urls/{shortKey} - Deve retornar Not Found se shortKey não existir")
    void deleteShortenedUrl_quandoShortKeyNaoExiste_deveRetornarNotFound() throws Exception {
        mockMvc.perform(delete(baseUrlApi + "/{shortKey}", "nonexistentdeletekey")
                        .header("X-User-Username", testUser))
                .andExpect(status().isNotFound());
    }
}
