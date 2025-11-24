package com.tfg.tfg.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import com.tfg.tfg.model.entity.Champion;
import com.tfg.tfg.repository.ChampionRepository;
import com.tfg.tfg.service.DataDragonService;

import java.lang.reflect.Field;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class DataDragonServiceSimpleUnitTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ChampionRepository championRepository;

    private DataDragonService service;

    @BeforeEach
    void setUp() throws Exception {
        service = new DataDragonService(championRepository);

        // Inject mocked RestTemplate
        Field restTemplateField = DataDragonService.class.getDeclaredField("restTemplate");
        restTemplateField.setAccessible(true);
        restTemplateField.set(service, restTemplate);
    }

    @Test
    void testUpdateChampionDatabaseSuccess() {
        // Given
        String mockJson = """
                {
                    "data": {
                        "Aatrox": {
                            "name": "Aatrox",
                            "key": "266"
                        },
                        "Ahri": {
                            "name": "Ahri",
                            "key": "103"
                        }
                    }
                }
                """;

        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(mockJson);

        // When
        service.updateChampionDatabase();

        // Then
        verify(championRepository, times(2)).save(any(Champion.class));
    }

    @Test
    void testGetChampionNameByIdFound() {
        // Given
        Champion champion = new Champion(266L, "Aatrox", "Aatrox", "url");
        when(championRepository.findById(266L)).thenReturn(Optional.of(champion));

        // When
        String name = service.getChampionNameById(266L);

        // Then
        assertEquals("Aatrox", name);
    }

    @Test
    void testGetChampionNameByIdNotFound() {
        // Given
        when(championRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        String name = service.getChampionNameById(999L);

        // Then
        assertEquals("Champion 999", name);
    }

    @Test
    void testGetChampionNameByIdNull() {
        String name = service.getChampionNameById(null);
        assertEquals("Unknown Champion", name);
    }

    @Test
    void testGetChampionIconUrlFound() {
        // Given
        Champion champion = new Champion(266L, "Aatrox", "Aatrox", "http://example.com/Aatrox.png");
        when(championRepository.findById(266L)).thenReturn(Optional.of(champion));

        // When
        String url = service.getChampionIconUrl(266L);

        // Then
        assertEquals("http://example.com/Aatrox.png", url);
    }

    @Test
    void testGetChampionIconUrlNotFound() {
        // Given
        when(championRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        String url = service.getChampionIconUrl(999L);

        // Then
        assertEquals("", url);
    }

    @Test
    void testGetProfileIconUrlValid() {
        String url = service.getProfileIconUrl(1);
        assertTrue(url.contains("profileicon/1.png"));
    }

    @Test
    void testGetProfileIconUrlNull() {
        String url = service.getProfileIconUrl(null);
        assertEquals("", url);
    }

    @Test
    void testUpdateChampionDatabaseApiError() {
        // Given
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenThrow(new RuntimeException("API Error"));

        // When
        assertDoesNotThrow(service::updateChampionDatabase);

        // Then
        verify(championRepository, never()).save(any(Champion.class));
    }

    @Test
    void testUpdateChampionDatabaseInvalidJson() {
        // Given
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn("invalid json");

        // When
        assertDoesNotThrow(service::updateChampionDatabase);

        // Then
        verify(championRepository, never()).save(any(Champion.class));
    }
}
