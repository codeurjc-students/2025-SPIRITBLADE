package com.tfg.tfg.unit;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.tfg.tfg.service.storage.MinioStorageService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MinioStorageService.
 * Tests S3/MinIO storage operations with mocked AmazonS3 client.
 */
@ExtendWith(MockitoExtension.class)
class MinioStorageServiceUnitTest {

    @Mock
    private AmazonS3 s3Client;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private MinioStorageService minioStorageService;

    private final String bucketName = "test-bucket";
    private final String minioEndpoint = "http://localhost:9000";
    private final String accessKey = "minioadmin";
    private final String secretKey = "minioadmin";
    private final String region = "us-east-1";

    @BeforeEach
    void setUp() {
        // Set private fields using reflection
        ReflectionTestUtils.setField(minioStorageService, "bucketName", bucketName);
        ReflectionTestUtils.setField(minioStorageService, "minioEndpoint", minioEndpoint);
        ReflectionTestUtils.setField(minioStorageService, "accessKey", accessKey);
        ReflectionTestUtils.setField(minioStorageService, "secretKey", secretKey);
        ReflectionTestUtils.setField(minioStorageService, "region", region);
        ReflectionTestUtils.setField(minioStorageService, "s3Client", s3Client);
    }

    @Test
    void testStoreMultipartFileWithFolderSuccess() throws IOException {
        // Given
        String originalFilename = "test-image.png";
        String contentType = "image/png";
        long fileSize = 1024L;
        // PNG magic bytes: 89 50 4E 47 0D 0A 1A 0A
        byte[] content = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x00};
        InputStream inputStream = new ByteArrayInputStream(content);

        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getOriginalFilename()).thenReturn(originalFilename);
        when(multipartFile.getContentType()).thenReturn(contentType);
        when(multipartFile.getSize()).thenReturn(fileSize);
        when(multipartFile.getInputStream()).thenReturn(inputStream);

        // When
        String storedKey = minioStorageService.store(multipartFile, "avatars");

        // Then
        assertNotNull(storedKey);
        assertTrue(storedKey.startsWith("avatars/"));
        assertTrue(storedKey.endsWith(".png"));
        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class));
    }

    @Test
    void testStoreMultipartFileWithoutFolderSuccess() throws IOException {
        // Given
        String originalFilename = "document.png";
        String contentType = "image/png";
        long fileSize = 2048L;
        // PNG magic bytes: 89 50 4E 47 0D 0A 1A 0A
        byte[] content = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x00};
        InputStream inputStream = new ByteArrayInputStream(content);

        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getOriginalFilename()).thenReturn(originalFilename);
        when(multipartFile.getContentType()).thenReturn(contentType);
        when(multipartFile.getSize()).thenReturn(fileSize);
        when(multipartFile.getInputStream()).thenReturn(inputStream);

        // When
        String storedKey = minioStorageService.store(multipartFile, null);

        // Then
        assertNotNull(storedKey);
        assertFalse(storedKey.contains("/"));
        assertTrue(storedKey.endsWith(".png"));
        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class));
    }

    @Test
    void testStoreMultipartFileEmptyFolderSuccess() throws IOException {
        // Given
        String originalFilename = "file.png";
        // PNG magic bytes: 89 50 4E 47 0D 0A 1A 0A
        byte[] content = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x00};
        
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getOriginalFilename()).thenReturn(originalFilename);
        when(multipartFile.getContentType()).thenReturn("image/png");
        when(multipartFile.getSize()).thenReturn(100L);
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(content));

        // When
        String storedKey = minioStorageService.store(multipartFile, "");

        // Then
        assertNotNull(storedKey);
        assertFalse(storedKey.contains("/"));
        assertTrue(storedKey.endsWith(".png"));
    }

    @Test
    void testStoreMultipartFileNoExtensionPNG() {
        // Given - File without .png extension should be rejected even with PNG content type
        String originalFilename = "noextension";
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getOriginalFilename()).thenReturn(originalFilename);
        when(multipartFile.getContentType()).thenReturn("image/png");

        // When & Then
        IOException exception = assertThrows(IOException.class, () -> {
            minioStorageService.store(multipartFile, "uploads");
        });
        
        assertTrue(exception.getMessage().contains("Invalid file extension"));
        assertTrue(exception.getMessage().contains(".png"));
        verify(s3Client, never()).putObject(any(PutObjectRequest.class));
    }

    @Test
    void testStoreMultipartFileNullFilenamePNG() {
        // Given - Null filename should be rejected even with PNG content type
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getOriginalFilename()).thenReturn(null);
        when(multipartFile.getContentType()).thenReturn("image/png");

        // When & Then
        IOException exception = assertThrows(IOException.class, () -> {
            minioStorageService.store(multipartFile, "docs");
        });
        
        assertTrue(exception.getMessage().contains("Filename is required"));
        verify(s3Client, never()).putObject(any(PutObjectRequest.class));
    }

    @Test
    void testStoreMultipartFileEmptyFileThrowsException() {
        // Given
        when(multipartFile.isEmpty()).thenReturn(true);

        // When & Then
        IOException exception = assertThrows(IOException.class, () -> {
            minioStorageService.store(multipartFile, "folder");
        });

        assertEquals("Cannot store empty file", exception.getMessage());
        verify(s3Client, never()).putObject(any(PutObjectRequest.class));
    }

    // Tests for PNG-only validation - Multipart Files
    @Test
    void testStoreMultipartFileRejectsJPEG() {
        // Given - Validation fails at content type check, filename not needed
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getContentType()).thenReturn("image/jpeg");

        // When & Then
        IOException exception = assertThrows(IOException.class, () -> {
            minioStorageService.store(multipartFile, "images");
        });
        
        assertTrue(exception.getMessage().contains("Invalid file type"));
        assertTrue(exception.getMessage().contains("PNG"));
        assertTrue(exception.getMessage().contains("image/jpeg"));
        verify(s3Client, never()).putObject(any(PutObjectRequest.class));
    }

    @Test
    void testStoreMultipartFileRejectsGIF() {
        // Given - Validation fails at content type check, filename not needed
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getContentType()).thenReturn("image/gif");

        // When & Then
        IOException exception = assertThrows(IOException.class, () -> {
            minioStorageService.store(multipartFile, "images");
        });
        
        assertTrue(exception.getMessage().contains("Invalid file type"));
        verify(s3Client, never()).putObject(any(PutObjectRequest.class));
    }

    @Test
    void testStoreMultipartFileRejectsPDF() {
        // Given - Validation fails at content type check, filename not needed
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getContentType()).thenReturn("application/pdf");

        // When & Then
        IOException exception = assertThrows(IOException.class, () -> {
            minioStorageService.store(multipartFile, "docs");
        });
        
        assertTrue(exception.getMessage().contains("Invalid file type"));
        verify(s3Client, never()).putObject(any(PutObjectRequest.class));
    }

    @Test
    void testStoreMultipartFileRejectsWrongExtension() {
        // Given - PNG content type but non-PNG extension (.jpg)
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getOriginalFilename()).thenReturn("image.jpg");
        when(multipartFile.getContentType()).thenReturn("image/png");

        // When & Then
        IOException exception = assertThrows(IOException.class, () -> {
            minioStorageService.store(multipartFile, "images");
        });
        
        assertTrue(exception.getMessage().contains("Invalid file extension"));
        assertTrue(exception.getMessage().contains(".png"));
        verify(s3Client, never()).putObject(any(PutObjectRequest.class));
    }

    @Test
    void testRetrieveSuccess() throws IOException {
        // Given
        String fileUrl = "avatars/test-file.png";
        S3Object s3Object = mock(S3Object.class);
        S3ObjectInputStream s3InputStream = mock(S3ObjectInputStream.class);

        when(s3Client.getObject(bucketName, fileUrl)).thenReturn(s3Object);
        when(s3Object.getObjectContent()).thenReturn(s3InputStream);

        // When
        InputStream result = minioStorageService.retrieve(fileUrl);

        // Then
        assertNotNull(result);
        assertEquals(s3InputStream, result);
        verify(s3Client, times(1)).getObject(bucketName, fileUrl);
    }

    @Test
    void testRetrieveFileNotFoundThrowsException() {
        // Given
        String fileUrl = "nonexistent/file.txt";
        when(s3Client.getObject(bucketName, fileUrl)).thenThrow(new RuntimeException("Not found"));

        // When & Then
        IOException exception = assertThrows(IOException.class, () -> {
            minioStorageService.retrieve(fileUrl);
        });

        assertTrue(exception.getMessage().contains("Could not retrieve file"));
        assertTrue(exception.getMessage().contains(fileUrl));
    }

    @Test
    void testDeleteSuccess() throws IOException {
        // Given
        String fileUrl = "images/old-photo.jpg";
        doNothing().when(s3Client).deleteObject(bucketName, fileUrl);

        // When
        minioStorageService.delete(fileUrl);

        // Then
        verify(s3Client, times(1)).deleteObject(bucketName, fileUrl);
    }

    @Test
    void testDeleteFileNotFoundThrowsException() {
        // Given
        String fileUrl = "missing/file.txt";
        doThrow(new RuntimeException("Delete failed")).when(s3Client).deleteObject(bucketName, fileUrl);

        // When & Then
        IOException exception = assertThrows(IOException.class, () -> {
            minioStorageService.delete(fileUrl);
        });

        assertTrue(exception.getMessage().contains("Could not delete file"));
        assertTrue(exception.getMessage().contains(fileUrl));
    }

    @Test
    void testGetPublicUrl() {
        // Given
        String fileIdentifier = "avatars/user123.png";

        // When
        String publicUrl = minioStorageService.getPublicUrl(fileIdentifier);

        // Then
        assertNotNull(publicUrl);
        assertEquals("/api/v1/files/" + fileIdentifier, publicUrl);
    }

    @Test
    void testGetPublicUrlDifferentIdentifier() {
        // Given
        String fileIdentifier = "documents/contract-2024.pdf";

        // When
        String publicUrl = minioStorageService.getPublicUrl(fileIdentifier);

        // Then
        assertEquals("/api/v1/files/documents/contract-2024.pdf", publicUrl);
    }
}
