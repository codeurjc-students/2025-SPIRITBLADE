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
    void testStore_MultipartFile_WithFolder_Success() throws IOException {
        // Given
        String originalFilename = "test-image.png";
        String contentType = "image/png";
        long fileSize = 1024L;
        byte[] content = "test content".getBytes();
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
    void testStore_MultipartFile_WithoutFolder_Success() throws IOException {
        // Given
        String originalFilename = "document.png";
        String contentType = "image/png";
        long fileSize = 2048L;
        byte[] content = "png content".getBytes();
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
    void testStore_MultipartFile_EmptyFolder_Success() throws IOException {
        // Given
        String originalFilename = "file.png";
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getOriginalFilename()).thenReturn(originalFilename);
        when(multipartFile.getContentType()).thenReturn("image/png");
        when(multipartFile.getSize()).thenReturn(100L);
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream("test".getBytes()));

        // When
        String storedKey = minioStorageService.store(multipartFile, "");

        // Then
        assertNotNull(storedKey);
        assertFalse(storedKey.contains("/"));
        assertTrue(storedKey.endsWith(".png"));
    }

    @Test
    void testStore_MultipartFile_NoExtension_PNG() {
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
    void testStore_MultipartFile_NullFilename_PNG() {
        // Given - Null filename should be rejected even with PNG content type
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getOriginalFilename()).thenReturn(null);
        when(multipartFile.getContentType()).thenReturn("image/png");

        // When & Then
        IOException exception = assertThrows(IOException.class, () -> {
            minioStorageService.store(multipartFile, "docs");
        });
        
        assertTrue(exception.getMessage().contains("Invalid file extension"));
        verify(s3Client, never()).putObject(any(PutObjectRequest.class));
    }

    @Test
    void testStore_MultipartFile_EmptyFile_ThrowsException() {
        // Given
        when(multipartFile.isEmpty()).thenReturn(true);

        // When & Then
        IOException exception = assertThrows(IOException.class, () -> {
            minioStorageService.store(multipartFile, "folder");
        });

        assertEquals("Cannot store empty file", exception.getMessage());
        verify(s3Client, never()).putObject(any(PutObjectRequest.class));
    }

    @Test
    void testStore_InputStream_WithFolder_Success() {
        // Given
        String fileName = "upload.png";
        String contentType = "image/png";
        InputStream inputStream = new ByteArrayInputStream("image data".getBytes());

        // When
        String storedKey = minioStorageService.store(inputStream, fileName, contentType, "images");

        // Then
        assertNotNull(storedKey);
        assertTrue(storedKey.startsWith("images/"));
        assertTrue(storedKey.endsWith(".png"));
        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class));
    }

    @Test
    void testStore_InputStream_WithoutFolder_Success() {
        // Given
        String fileName = "data.png";
        String contentType = "image/png";
        InputStream inputStream = new ByteArrayInputStream("{}".getBytes());

        // When
        String storedKey = minioStorageService.store(inputStream, fileName, contentType, null);

        // Then
        assertNotNull(storedKey);
        assertFalse(storedKey.contains("/"));
        assertTrue(storedKey.endsWith(".png"));
    }

    @Test
    void testStore_InputStream_EmptyFolder() {
        // Given
        String fileName = "test.png";
        InputStream inputStream = new ByteArrayInputStream("<xml/>".getBytes());

        // When
        String storedKey = minioStorageService.store(inputStream, fileName, "image/png", "");

        // Then
        assertNotNull(storedKey);
        assertFalse(storedKey.contains("/"));
        assertTrue(storedKey.endsWith(".png"));
    }

    @Test
    void testStore_InputStream_NoExtension_PNG() {
        // Given - File without .png extension should be rejected even with PNG content type
        String fileName = "noext";
        InputStream inputStream = new ByteArrayInputStream("data".getBytes());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            minioStorageService.store(inputStream, fileName, "image/png", "bin");
        });
        
        assertTrue(exception.getMessage().contains("Invalid file extension"));
        assertTrue(exception.getMessage().contains(".png"));
        verify(s3Client, never()).putObject(any(PutObjectRequest.class));
    }

    // Tests for PNG-only validation - Multipart Files
    @Test
    void testStore_MultipartFile_RejectsJPEG() {
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
    void testStore_MultipartFile_RejectsGIF() {
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
    void testStore_MultipartFile_RejectsPDF() {
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
    void testStore_MultipartFile_RejectsWrongExtension() {
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

    // Tests for PNG-only validation - InputStream
    @Test
    void testStore_InputStream_RejectsJPEG() {
        // Given
        String fileName = "photo.jpg";
        InputStream inputStream = new ByteArrayInputStream("jpeg data".getBytes());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            minioStorageService.store(inputStream, fileName, "image/jpeg", "images");
        });
        
        assertTrue(exception.getMessage().contains("Invalid file type"));
        assertTrue(exception.getMessage().contains("PNG"));
        assertTrue(exception.getMessage().contains("image/jpeg"));
        verify(s3Client, never()).putObject(any(PutObjectRequest.class));
    }

    @Test
    void testStore_InputStream_RejectsTextFile() {
        // Given
        String fileName = "document.txt";
        InputStream inputStream = new ByteArrayInputStream("text".getBytes());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            minioStorageService.store(inputStream, fileName, "text/plain", "docs");
        });
        
        assertTrue(exception.getMessage().contains("Invalid file type"));
        verify(s3Client, never()).putObject(any(PutObjectRequest.class));
    }

    @Test
    void testStore_InputStream_RejectsWrongExtension() {
        // Given - PNG content type but non-PNG extension (.jpeg)
        String fileName = "image.jpeg";
        InputStream inputStream = new ByteArrayInputStream("png data".getBytes());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            minioStorageService.store(inputStream, fileName, "image/png", "images");
        });
        
        assertTrue(exception.getMessage().contains("Invalid file extension"));
        assertTrue(exception.getMessage().contains(".png"));
        verify(s3Client, never()).putObject(any(PutObjectRequest.class));
    }

    @Test
    void testRetrieve_Success() throws IOException {
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
    void testRetrieve_FileNotFound_ThrowsException() {
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
    void testDelete_Success() throws IOException {
        // Given
        String fileUrl = "images/old-photo.jpg";
        doNothing().when(s3Client).deleteObject(bucketName, fileUrl);

        // When
        minioStorageService.delete(fileUrl);

        // Then
        verify(s3Client, times(1)).deleteObject(bucketName, fileUrl);
    }

    @Test
    void testDelete_FileNotFound_ThrowsException() {
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
    void testExists_FileExists_ReturnsTrue() {
        // Given
        String fileUrl = "documents/report.pdf";
        when(s3Client.doesObjectExist(bucketName, fileUrl)).thenReturn(true);

        // When
        boolean exists = minioStorageService.exists(fileUrl);

        // Then
        assertTrue(exists);
        verify(s3Client, times(1)).doesObjectExist(bucketName, fileUrl);
    }

    @Test
    void testExists_FileDoesNotExist_ReturnsFalse() {
        // Given
        String fileUrl = "missing/file.doc";
        when(s3Client.doesObjectExist(bucketName, fileUrl)).thenReturn(false);

        // When
        boolean exists = minioStorageService.exists(fileUrl);

        // Then
        assertFalse(exists);
    }

    @Test
    void testExists_ExceptionOccurs_ReturnsFalse() {
        // Given
        String fileUrl = "error/file.txt";
        when(s3Client.doesObjectExist(bucketName, fileUrl)).thenThrow(new RuntimeException("S3 error"));

        // When
        boolean exists = minioStorageService.exists(fileUrl);

        // Then
        assertFalse(exists);
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
    void testGetPublicUrl_DifferentIdentifier() {
        // Given
        String fileIdentifier = "documents/contract-2024.pdf";

        // When
        String publicUrl = minioStorageService.getPublicUrl(fileIdentifier);

        // Then
        assertEquals("/api/v1/files/documents/contract-2024.pdf", publicUrl);
    }
}
