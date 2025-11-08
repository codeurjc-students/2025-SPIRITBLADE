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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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

    private static final String TEST_IMAGE_PNG = "test-image.png";
    private static final String TEST_IMAGE_12345 = "12345-test-image.png";
    private static final String TEST_IMAGE_CONTENT = "test image content";
    private static final String EMPTY_IMAGE_12345 = "12345-empty.png";
    private static final String AVATAR_12345 = "avatars/12345-avatar.png";
    private static final String AVATARS_FOLDER = "avatars";
    private static final String UPLOAD_URL = "/api/v1/files/upload";
    private static final String FILES_API_PATH = "/api/v1/files/";
    private static final String MINIO_BASE_URL = "http://localhost:9000/bucket/";
    private static final String JSON_SUCCESS = "$.success";
    private static final String JSON_FILE_ID = "$.fileId";
    private static final String JSON_ERROR = "$.error";
    private static final String TRUE = "true";
    private static final String FALSE = "false";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String IMAGE_PNG = "image/png";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
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
            TEST_IMAGE_PNG,
            MediaType.IMAGE_PNG_VALUE,
            TEST_IMAGE_CONTENT.getBytes()
        );

        // Mock storage service behavior
        when(storageService.store(any(), isNull())).thenReturn(TEST_IMAGE_12345);
        when(storageService.getPublicUrl(TEST_IMAGE_12345)).thenReturn(MINIO_BASE_URL + TEST_IMAGE_12345);

        // Perform request
        mockMvc.perform(multipart(UPLOAD_URL)
                .file(file))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(JSON_SUCCESS, is(TRUE)))
                .andExpect(jsonPath(JSON_FILE_ID, is(TEST_IMAGE_12345)))
                .andExpect(jsonPath("$.url", is(MINIO_BASE_URL + TEST_IMAGE_12345)));

        // Verify interactions
        verify(storageService).store(any(), isNull());
        verify(storageService).getPublicUrl(TEST_IMAGE_12345);
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
        when(storageService.store(any(), eq(AVATARS_FOLDER))).thenReturn(AVATAR_12345);
        when(storageService.getPublicUrl(AVATAR_12345)).thenReturn(MINIO_BASE_URL + AVATAR_12345);

        // Perform request
        mockMvc.perform(multipart(UPLOAD_URL)
                .file(file)
                .param("folder", AVATARS_FOLDER))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(JSON_SUCCESS, is(TRUE)))
                .andExpect(jsonPath(JSON_FILE_ID, is(AVATAR_12345)))
                .andExpect(jsonPath("$.url", is(MINIO_BASE_URL + AVATAR_12345)));

        // Verify interactions
        verify(storageService).store(any(), eq(AVATARS_FOLDER));
        verify(storageService).getPublicUrl(AVATAR_12345);
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
        mockMvc.perform(multipart(UPLOAD_URL)
                .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(JSON_SUCCESS, is(FALSE)))
                .andExpect(jsonPath(JSON_ERROR, is("Invalid file type. Only PNG images are allowed")));

        // No storage interactions should happen
    }

    @Test
    @WithMockUser
    void testUploadFileFailure() throws Exception {
        // Mock file upload
        MockMultipartFile file = new MockMultipartFile(
            "file",
            TEST_IMAGE_PNG,
            MediaType.IMAGE_PNG_VALUE,
            TEST_IMAGE_CONTENT.getBytes()
        );

        // Mock storage service throwing exception
        when(storageService.store(any(), isNull())).thenThrow(new java.io.IOException("Storage error"));

        // Perform request
        mockMvc.perform(multipart(UPLOAD_URL)
                .file(file))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(JSON_SUCCESS, is(FALSE)))
                .andExpect(jsonPath(JSON_ERROR, is("Storage error")));

        // Verify interactions
        verify(storageService).store(any(), isNull());
    }

    @Test
    void testGetFileWithFolderSuccessPng() throws Exception {
        // Mock file retrieval
        byte[] fileContent = "test png image content".getBytes();
        InputStream inputStream = new ByteArrayInputStream(fileContent);

        when(storageService.retrieve("avatars/test-image.png")).thenReturn(inputStream);

        // Perform request
        mockMvc.perform(get("/api/v1/files/avatars/test-image.png"))
                .andExpect(status().isOk())
                .andExpect(header().string(CONTENT_TYPE, containsString(IMAGE_PNG)))
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
    void testGetFileFromRootSuccessPng() throws Exception {
        // Mock file retrieval from root
        byte[] fileContent = "test png image content".getBytes();
        InputStream inputStream = new ByteArrayInputStream(fileContent);

        when(storageService.retrieve(TEST_IMAGE_PNG)).thenReturn(inputStream);

        // Perform request
        mockMvc.perform(get(FILES_API_PATH + TEST_IMAGE_PNG))
                .andExpect(status().isOk())
                .andExpect(header().string(CONTENT_TYPE, containsString(IMAGE_PNG)))
                .andExpect(content().bytes(fileContent));

        // Verify interactions
        verify(storageService).retrieve(TEST_IMAGE_PNG);
    }

    @Test
    void testGetFileFromRootNotFound() throws Exception {
        // Mock storage service throwing exception
        when(storageService.retrieve("nonexistent.png"))
            .thenThrow(new java.io.IOException("File not found"));

        // Perform request
        mockMvc.perform(get(FILES_API_PATH + "nonexistent.png"))
                .andExpect(status().isNotFound());

        // Verify interactions
        verify(storageService).retrieve("nonexistent.png");
    }

    @Test
    @WithMockUser
    void testDeleteFileSuccess() throws Exception {
        // Mock successful deletion
        doNothing().when(storageService).delete(TEST_IMAGE_12345);

        // Perform request
        mockMvc.perform(delete(FILES_API_PATH + TEST_IMAGE_12345))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(JSON_SUCCESS, is(TRUE)))
                .andExpect(jsonPath("$.message", is("File deleted successfully")));

        // Verify interactions
        verify(storageService).delete(TEST_IMAGE_12345);
    }

    @Test
    @WithMockUser
    void testDeleteFileFailure() throws Exception {
        // Mock storage service throwing exception
        doThrow(new java.io.IOException("Delete error")).when(storageService).delete(TEST_IMAGE_PNG);

        // Perform request
        mockMvc.perform(delete(FILES_API_PATH + TEST_IMAGE_PNG))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(JSON_SUCCESS, is(FALSE)))
                .andExpect(jsonPath(JSON_ERROR, is("Delete error")));

        // Verify interactions
        verify(storageService).delete(TEST_IMAGE_PNG);
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
                .andExpect(jsonPath(JSON_SUCCESS, is(TRUE)))
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
        when(storageService.store(any(), isNull())).thenReturn(EMPTY_IMAGE_12345);
        when(storageService.getPublicUrl(EMPTY_IMAGE_12345)).thenReturn(MINIO_BASE_URL + EMPTY_IMAGE_12345);

        // Perform request
        mockMvc.perform(multipart(UPLOAD_URL)
                .file(emptyFile))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(JSON_SUCCESS, is(TRUE)))
                .andExpect(jsonPath(JSON_FILE_ID, is(EMPTY_IMAGE_12345)));

        // Verify interactions
        verify(storageService).store(any(), isNull());
        verify(storageService).getPublicUrl(EMPTY_IMAGE_12345);
    }

    @Test
    void testGetFileCaseInsensitiveExtension() throws Exception {
        // Test uppercase extension handling
        byte[] fileContent = TEST_IMAGE_CONTENT.getBytes();
        InputStream inputStream = new ByteArrayInputStream(fileContent);

        when(storageService.retrieve("test-image.PNG")).thenReturn(inputStream);

        // Perform request - should still recognize as PNG
        mockMvc.perform(get("/api/v1/files/test-image.PNG"))
                .andExpect(status().isOk())
                .andExpect(header().string(CONTENT_TYPE, containsString(IMAGE_PNG)));

        verify(storageService).retrieve("test-image.PNG");
    }

}
