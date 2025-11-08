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

import com.tfg.tfg.service.DataDragonService;

import java.lang.reflect.Field;

@ExtendWith(MockitoExtension.class)
class DataDragonServiceSimpleUnitTest {

    @Mock
    private RestTemplate restTemplate;
    
    private DataDragonService service;
    
    @BeforeEach
    void setUp() throws Exception {
        service = new DataDragonService();
        
        // Inject mocked RestTemplate
        Field restTemplateField = DataDragonService.class.getDeclaredField("restTemplate");
        restTemplateField.setAccessible(true);
        restTemplateField.set(service, restTemplate);
        
        // Mock champion data loading
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
        
        // Load data
        service.loadChampionData();
    }
    
    @Test
    void testLoadChampionDataSuccess() {
        assertTrue(service.isDataLoaded());
        assertEquals(2, service.getChampionCount());
    }
    
    @Test
    void testGetChampionNameByIdFound() {
        String name = service.getChampionNameById(266L);
        assertEquals("Aatrox", name);
    }
    
    @Test
    void testGetChampionNameByIdNotFound() {
        String name = service.getChampionNameById(999L);
        assertEquals("Champion 999", name);
    }
    
    @Test
    void testGetChampionNameByIdNull() {
        String name = service.getChampionNameById(null);
        assertEquals("Unknown Champion", name);
    }
    
    @Test
    void testGetChampionNameByKeyFound() {
        String name = service.getChampionNameByKey("Aatrox");
        assertEquals("Aatrox", name);
    }
    
    @Test
    void testGetChampionNameByKeyNotFound() {
        String name = service.getChampionNameByKey("UnknownChamp");
        assertEquals("UnknownChamp", name);
    }
    
    @Test
    void testGetChampionNameByKeyNull() {
        String name = service.getChampionNameByKey(null);
        assertEquals("Unknown Champion", name);
    }
    
    @Test
    void testGetChampionIconUrlFound() {
        String url = service.getChampionIconUrl(266L);
        assertTrue(url.contains("Aatrox.png"));
    }
    
    @Test
    void testGetChampionIconUrlNotFound() {
        String url = service.getChampionIconUrl(999L);
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
    void testLoadChampionDataApiError() throws Exception {
        DataDragonService serviceWithError = new DataDragonService();
        
        RestTemplate errorRestTemplate = mock(RestTemplate.class);
        when(errorRestTemplate.getForObject(anyString(), eq(String.class)))
            .thenThrow(new RuntimeException("API Error"));
        
        Field restTemplateField = DataDragonService.class.getDeclaredField("restTemplate");
        restTemplateField.setAccessible(true);
        restTemplateField.set(serviceWithError, errorRestTemplate);
        
        // Should not throw exception, just log error
        assertDoesNotThrow(serviceWithError::loadChampionData);
        
        // Service should continue with empty data
        assertFalse(serviceWithError.isDataLoaded());
        assertEquals(0, serviceWithError.getChampionCount());
    }
    
    @Test
    void testLoadChampionDataInvalidJson() throws Exception {
        DataDragonService serviceWithBadData = new DataDragonService();
        
        RestTemplate badRestTemplate = mock(RestTemplate.class);
        when(badRestTemplate.getForObject(anyString(), eq(String.class)))
            .thenReturn("invalid json");
        
        Field restTemplateField = DataDragonService.class.getDeclaredField("restTemplate");
        restTemplateField.setAccessible(true);
        restTemplateField.set(serviceWithBadData, badRestTemplate);
        
        // Should not throw exception
        assertDoesNotThrow(serviceWithBadData::loadChampionData);
        
        // Service should continue with empty data
        assertFalse(serviceWithBadData.isDataLoaded());
    }
    
    @Test
    void testIsDataLoadedEmptyService() {
        DataDragonService emptyService = new DataDragonService();
        assertFalse(emptyService.isDataLoaded());
    }
    
    @Test
    void testGetChampionCount() {
        assertEquals(2, service.getChampionCount());
    }
}
