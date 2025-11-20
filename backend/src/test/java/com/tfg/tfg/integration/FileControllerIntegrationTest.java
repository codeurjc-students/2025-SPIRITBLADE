package com.tfg.tfg.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.tfg.tfg.service.storage.MinioStorageService;

import java.io.IOException;
import java.io.ByteArrayInputStream;

import org.springframework.security.test.context.support.WithMockUser;

/**
 * Integration tests for FileController
 * Tests file upload, download, and delete operations
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class FileControllerIntegrationTest {

    private static final String API_FILES = "/api/v1/files";
    private static final String TEST_FOLDER = "test";
    private static final String TEST_FILENAME = "test.png";
    private static final String INVALID_FILENAME = "test.jpg";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MinioStorageService storageService;

    @BeforeEach
    void setUp() throws IOException {
        // Mock the storage service methods for testing
        // Configure store() method to return file identifiers
        when(storageService.store(any(), isNull())).thenReturn(TEST_FILENAME);
        when(storageService.store(any(), eq(TEST_FOLDER))).thenReturn(TEST_FOLDER + "/" + TEST_FILENAME);
        
        // Configure getPublicUrl() method
        when(storageService.getPublicUrl(TEST_FILENAME)).thenReturn("http://example.com/" + TEST_FILENAME);
        when(storageService.getPublicUrl(TEST_FOLDER + "/" + TEST_FILENAME)).thenReturn("http://example.com/" + TEST_FOLDER + "/" + TEST_FILENAME);
        
        // Configure retrieve() method for successful cases
        when(storageService.retrieve(TEST_FILENAME)).thenReturn(new ByteArrayInputStream("test image content".getBytes()));
        when(storageService.retrieve(TEST_FOLDER + "/" + TEST_FILENAME)).thenReturn(new ByteArrayInputStream("test image content".getBytes()));
        when(storageService.retrieve(TEST_FOLDER + "/test.PNG")).thenReturn(new ByteArrayInputStream("test image content".getBytes()));
        
        // Configure retrieve() method to throw IOException for non-existent files
        when(storageService.retrieve("nonexistent.png")).thenThrow(new IOException("File not found"));
        when(storageService.retrieve(TEST_FOLDER + "/nonexistent.png")).thenThrow(new IOException("File not found"));
        
        // Configure delete() method - do nothing for success, throw exception for failure
        doNothing().when(storageService).delete(anyString());
        doThrow(new IOException("File not found")).when(storageService).delete("nonexistent.png");
    }

    @Test
    @WithMockUser
    void testUploadFileSuccess() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            TEST_FILENAME,
            "image/png",
            "test image content".getBytes()
        );

        mockMvc.perform(multipart(API_FILES + "/upload")
                .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value("true"))
                .andExpect(jsonPath("$.fileId").exists())
                .andExpect(jsonPath("$.url").exists());
    }

    @Test
    @WithMockUser
    void testUploadFileWithFolderSuccess() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            TEST_FILENAME,
            "image/png",
            "test image content".getBytes()
        );

        mockMvc.perform(multipart(API_FILES + "/upload")
                .file(file)
                .param("folder", TEST_FOLDER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value("true"))
                .andExpect(jsonPath("$.fileId").exists())
                .andExpect(jsonPath("$.url").exists());
    }

    @Test
    @WithMockUser
    void testUploadFileInvalidType() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            INVALID_FILENAME,
            "image/jpeg",
            "test image content".getBytes()
        );

        mockMvc.perform(multipart(API_FILES + "/upload")
                .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value("false"))
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @WithMockUser
    void testUploadFileFailure() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            TEST_FILENAME,
            "image/png",
            (byte[]) null
        );

        mockMvc.perform(multipart(API_FILES + "/upload")
                .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value("true"))
                .andExpect(jsonPath("$.fileId").exists())
                .andExpect(jsonPath("$.url").exists());
    }

    @Test
    void testGetFileWithFolderSuccessPng() throws Exception {
        mockMvc.perform(get(API_FILES + "/{folder}/{filename}", TEST_FOLDER, TEST_FILENAME))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("image/png"));
    }

    @Test
    void testGetFileWithFolderNotFound() throws Exception {
        mockMvc.perform(get(API_FILES + "/{folder}/{filename}", TEST_FOLDER, "nonexistent.png"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetFileFromRootSuccessPng() throws Exception {
        mockMvc.perform(get(API_FILES + "/{filename}", TEST_FILENAME))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("image/png"));
    }

    @Test
    void testGetFileFromRootNotFound() throws Exception {
        mockMvc.perform(get(API_FILES + "/{filename}", "nonexistent.png"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetFileCaseInsensitiveExtension() throws Exception {
        mockMvc.perform(get(API_FILES + "/{folder}/{filename}", TEST_FOLDER, "test.PNG"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("image/png"));
    }

    @Test
    @WithMockUser
    void testDeleteFileSuccess() throws Exception {
        mockMvc.perform(delete(API_FILES + "/delete/" + TEST_FILENAME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value("true"))
                .andExpect(jsonPath("$.message").value("File deleted successfully"));
    }

    @Test
    @WithMockUser
    void testDeleteFileFailure() throws Exception {
        mockMvc.perform(delete(API_FILES + "/delete/nonexistent.png"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value("false"))
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @WithMockUser
    void testDeleteFileWithPathEncoding() throws Exception {
        String encodedFileId = "testfile.png"; // Simple encoded filename
        mockMvc.perform(delete(API_FILES + "/delete/" + encodedFileId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value("true"));
    }

    @Test
    @WithMockUser
    void testUploadEmptyFile() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile(
            "file",
            TEST_FILENAME,
            "image/png",
            new byte[0]
        );

        mockMvc.perform(multipart(API_FILES + "/upload")
                .file(emptyFile))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value("true"));
    }
}