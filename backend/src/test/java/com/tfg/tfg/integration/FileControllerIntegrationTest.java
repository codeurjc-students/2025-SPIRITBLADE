package com.tfg.tfg.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.CoreMatchers.containsString;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.tfg.tfg.service.storage.MinioStorageService;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Integration tests for FileController
 * Tests file upload, download, and delete operations
 */
@SpringBootTest
@AutoConfigureMockMvc
class FileControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MinioStorageService storageService;

    @BeforeEach
    void setUp() {
        // Reset mock state before each test
        reset(storageService);
    }

    @AfterEach
    void tearDown() {
        // Verify no unexpected interactions
        verifyNoMoreInteractions(storageService);
    }

    @Test
    @WithMockUser
    void testUploadFileSuccess() throws Exception {
        // Mock file upload
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test-image.png",
            MediaType.IMAGE_PNG_VALUE,
            "test image content".getBytes()
        );

        // Mock storage service behavior
        when(storageService.store(any(), isNull())).thenReturn("12345-test-image.png");
        when(storageService.getPublicUrl("12345-test-image.png")).thenReturn("http://localhost:9000/bucket/12345-test-image.png");

        // Perform request
        mockMvc.perform(multipart("/api/v1/files/upload")
                .file(file))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success", is("true")))
                .andExpect(jsonPath("$.fileId", is("12345-test-image.png")))
                .andExpect(jsonPath("$.url", is("http://localhost:9000/bucket/12345-test-image.png")));

        // Verify interactions
        verify(storageService).store(any(), isNull());
        verify(storageService).getPublicUrl("12345-test-image.png");
    }

    @Test
    @WithMockUser
    void testUploadFileWithFolderSuccess() throws Exception {
        // Mock file upload with folder
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "avatar.png",
            MediaType.IMAGE_PNG_VALUE,
            "test avatar content".getBytes()
        );

        // Mock storage service behavior
        when(storageService.store(any(), eq("avatars"))).thenReturn("avatars/12345-avatar.png");
        when(storageService.getPublicUrl("avatars/12345-avatar.png")).thenReturn("http://localhost:9000/bucket/avatars/12345-avatar.png");

        // Perform request
        mockMvc.perform(multipart("/api/v1/files/upload")
                .file(file)
                .param("folder", "avatars"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success", is("true")))
                .andExpect(jsonPath("$.fileId", is("avatars/12345-avatar.png")))
                .andExpect(jsonPath("$.url", is("http://localhost:9000/bucket/avatars/12345-avatar.png")));

        // Verify interactions
        verify(storageService).store(any(), eq("avatars"));
        verify(storageService).getPublicUrl("avatars/12345-avatar.png");
    }

    @Test
    @WithMockUser
    void testUploadFileInvalidType() throws Exception {
        // Mock file upload with invalid type (JPEG)
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "avatar.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "test avatar content".getBytes()
        );

        // Perform request - should be rejected
        mockMvc.perform(multipart("/api/v1/files/upload")
                .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success", is("false")))
                .andExpect(jsonPath("$.error", is("Invalid file type. Only PNG images are allowed")));

        // No storage interactions should happen
    }

    @Test
    @WithMockUser
    void testUploadFileFailure() throws Exception {
        // Mock file upload
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test-image.png",
            MediaType.IMAGE_PNG_VALUE,
            "test image content".getBytes()
        );

        // Mock storage service throwing exception
        when(storageService.store(any(), isNull())).thenThrow(new java.io.IOException("Storage error"));

        // Perform request
        mockMvc.perform(multipart("/api/v1/files/upload")
                .file(file))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success", is("false")))
                .andExpect(jsonPath("$.error", is("Storage error")));

        // Verify interactions
        verify(storageService).store(any(), isNull());
    }

    @Test
    void testGetFileWithFolderSuccess_PNG() throws Exception {
        // Mock file retrieval
        byte[] fileContent = "test png image content".getBytes();
        InputStream inputStream = new ByteArrayInputStream(fileContent);

        when(storageService.retrieve("avatars/test-image.png")).thenReturn(inputStream);

        // Perform request
        mockMvc.perform(get("/api/v1/files/avatars/test-image.png"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("image/png")))
                .andExpect(content().bytes(fileContent));

        // Verify interactions
        verify(storageService).retrieve("avatars/test-image.png");
    }

    @Test
    void testGetFileWithFolderNotFound() throws Exception {
        // Mock storage service throwing exception
        when(storageService.retrieve("avatars/nonexistent.png"))
            .thenThrow(new java.io.IOException("File not found"));

        // Perform request
        mockMvc.perform(get("/api/v1/files/avatars/nonexistent.png"))
                .andExpect(status().isNotFound());

        // Verify interactions
        verify(storageService).retrieve("avatars/nonexistent.png");
    }

    @Test
    void testGetFileFromRootSuccess_PNG() throws Exception {
        // Mock file retrieval from root
        byte[] fileContent = "test png image content".getBytes();
        InputStream inputStream = new ByteArrayInputStream(fileContent);

        when(storageService.retrieve("test-image.png")).thenReturn(inputStream);

        // Perform request
        mockMvc.perform(get("/api/v1/files/test-image.png"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("image/png")))
                .andExpect(content().bytes(fileContent));

        // Verify interactions
        verify(storageService).retrieve("test-image.png");
    }

    @Test
    void testGetFileFromRootNotFound() throws Exception {
        // Mock storage service throwing exception
        when(storageService.retrieve("nonexistent.png"))
            .thenThrow(new java.io.IOException("File not found"));

        // Perform request
        mockMvc.perform(get("/api/v1/files/nonexistent.png"))
                .andExpect(status().isNotFound());

        // Verify interactions
        verify(storageService).retrieve("nonexistent.png");
    }

    @Test
    @WithMockUser
    void testDeleteFileSuccess() throws Exception {
        // Mock successful deletion
        doNothing().when(storageService).delete("12345-test-image.png");

        // Perform request
        mockMvc.perform(delete("/api/v1/files/12345-test-image.png"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success", is("true")))
                .andExpect(jsonPath("$.message", is("File deleted successfully")));

        // Verify interactions
        verify(storageService).delete("12345-test-image.png");
    }

    @Test
    @WithMockUser
    void testDeleteFileFailure() throws Exception {
        // Mock storage service throwing exception
        doThrow(new java.io.IOException("Delete error")).when(storageService).delete("test-image.png");

        // Perform request
        mockMvc.perform(delete("/api/v1/files/test-image.png"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success", is("false")))
                .andExpect(jsonPath("$.error", is("Delete error")));

        // Verify interactions
        verify(storageService).delete("test-image.png");
    }

    @Test
    @WithMockUser
    void testDeleteFileWithPathEncoding() throws Exception {
        // Mock successful deletion with encoded path
        doNothing().when(storageService).delete("avatars-12345-avatar.jpg");

        // Perform request with simple file ID (no slashes)
        mockMvc.perform(delete("/api/v1/files/avatars-12345-avatar.jpg"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success", is("true")))
                .andExpect(jsonPath("$.message", is("File deleted successfully")));

        // Verify interactions
        verify(storageService).delete("avatars-12345-avatar.jpg");
    }

    @Test
    @WithMockUser
    void testUploadEmptyFile() throws Exception {
        // Mock empty file upload
        MockMultipartFile emptyFile = new MockMultipartFile(
            "file",
            "empty.png",
            MediaType.IMAGE_PNG_VALUE,
            new byte[0]
        );

        // Mock storage service behavior
        when(storageService.store(any(), isNull())).thenReturn("12345-empty.png");
        when(storageService.getPublicUrl("12345-empty.png")).thenReturn("http://localhost:9000/bucket/12345-empty.png");

        // Perform request
        mockMvc.perform(multipart("/api/v1/files/upload")
                .file(emptyFile))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success", is("true")))
                .andExpect(jsonPath("$.fileId", is("12345-empty.png")));

        // Verify interactions
        verify(storageService).store(any(), isNull());
        verify(storageService).getPublicUrl("12345-empty.png");
    }

    @Test
    void testGetFileCaseInsensitiveExtension() throws Exception {
        // Test uppercase extension handling
        byte[] fileContent = "test image content".getBytes();
        InputStream inputStream = new ByteArrayInputStream(fileContent);

        when(storageService.retrieve("test-image.PNG")).thenReturn(inputStream);

        // Perform request - should still recognize as PNG
        mockMvc.perform(get("/api/v1/files/test-image.PNG"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("image/png")));

        verify(storageService).retrieve("test-image.PNG");
    }

}
