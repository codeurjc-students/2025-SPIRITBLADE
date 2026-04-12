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

        ReflectionTestUtils.setField(minioStorageService, "bucketName", bucketName);
        ReflectionTestUtils.setField(minioStorageService, "minioEndpoint", minioEndpoint);
        ReflectionTestUtils.setField(minioStorageService, "accessKey", accessKey);
        ReflectionTestUtils.setField(minioStorageService, "secretKey", secretKey);
        ReflectionTestUtils.setField(minioStorageService, "region", region);
        ReflectionTestUtils.setField(minioStorageService, "s3Client", s3Client);
    }

    @Test
    void testStoreMultipartFileWithFolderSuccess() throws IOException {

        String originalFilename = "test-image.png";
        String contentType = "image/png";
        long fileSize = 1024L;

        byte[] content = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x00};
        InputStream inputStream = new ByteArrayInputStream(content);

        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getOriginalFilename()).thenReturn(originalFilename);
        when(multipartFile.getContentType()).thenReturn(contentType);
        when(multipartFile.getSize()).thenReturn(fileSize);
        when(multipartFile.getInputStream()).thenReturn(inputStream);

        String storedKey = minioStorageService.store(multipartFile, "avatars");

        assertNotNull(storedKey);
        assertTrue(storedKey.startsWith("avatars/"));
        assertTrue(storedKey.endsWith(".png"));
        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class));
    }

    @Test
    void testStoreMultipartFileWithoutFolderSuccess() throws IOException {

        String originalFilename = "document.png";
        String contentType = "image/png";
        long fileSize = 2048L;

        byte[] content = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x00};
        InputStream inputStream = new ByteArrayInputStream(content);

        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getOriginalFilename()).thenReturn(originalFilename);
        when(multipartFile.getContentType()).thenReturn(contentType);
        when(multipartFile.getSize()).thenReturn(fileSize);
        when(multipartFile.getInputStream()).thenReturn(inputStream);

        String storedKey = minioStorageService.store(multipartFile, null);

        assertNotNull(storedKey);
        assertFalse(storedKey.contains("/"));
        assertTrue(storedKey.endsWith(".png"));
        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class));
    }

    @Test
    void testStoreMultipartFileEmptyFolderSuccess() throws IOException {

        String originalFilename = "file.png";

        byte[] content = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x00};
        
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getOriginalFilename()).thenReturn(originalFilename);
        when(multipartFile.getContentType()).thenReturn("image/png");
        when(multipartFile.getSize()).thenReturn(100L);
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(content));

        String storedKey = minioStorageService.store(multipartFile, "");

        assertNotNull(storedKey);
        assertFalse(storedKey.contains("/"));
        assertTrue(storedKey.endsWith(".png"));
    }

    @Test
    void testStoreMultipartFileNoExtensionPNG() {

        String originalFilename = "noextension";
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getOriginalFilename()).thenReturn(originalFilename);
        when(multipartFile.getContentType()).thenReturn("image/png");

        IOException exception = assertThrows(IOException.class, () -> {
            minioStorageService.store(multipartFile, "uploads");
        });
        
        assertTrue(exception.getMessage().contains("Invalid file extension"));
        assertTrue(exception.getMessage().contains(".png"));
        verify(s3Client, never()).putObject(any(PutObjectRequest.class));
    }

    @Test
    void testStoreMultipartFileNullFilenamePNG() {

        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getOriginalFilename()).thenReturn(null);
        when(multipartFile.getContentType()).thenReturn("image/png");

        IOException exception = assertThrows(IOException.class, () -> {
            minioStorageService.store(multipartFile, "docs");
        });
        
        assertTrue(exception.getMessage().contains("Filename is required"));
        verify(s3Client, never()).putObject(any(PutObjectRequest.class));
    }

    @Test
    void testStoreMultipartFileEmptyFileThrowsException() {

        when(multipartFile.isEmpty()).thenReturn(true);

        IOException exception = assertThrows(IOException.class, () -> {
            minioStorageService.store(multipartFile, "folder");
        });

        assertEquals("Cannot store empty file", exception.getMessage());
        verify(s3Client, never()).putObject(any(PutObjectRequest.class));
    }

    @Test
    void testStoreMultipartFileRejectsJPEG() {

        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getContentType()).thenReturn("image/jpeg");

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

        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getContentType()).thenReturn("image/gif");

        IOException exception = assertThrows(IOException.class, () -> {
            minioStorageService.store(multipartFile, "images");
        });
        
        assertTrue(exception.getMessage().contains("Invalid file type"));
        verify(s3Client, never()).putObject(any(PutObjectRequest.class));
    }

    @Test
    void testStoreMultipartFileRejectsPDF() {

        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getContentType()).thenReturn("application/pdf");

        IOException exception = assertThrows(IOException.class, () -> {
            minioStorageService.store(multipartFile, "docs");
        });
        
        assertTrue(exception.getMessage().contains("Invalid file type"));
        verify(s3Client, never()).putObject(any(PutObjectRequest.class));
    }

    @Test
    void testStoreMultipartFileRejectsWrongExtension() {

        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getOriginalFilename()).thenReturn("image.jpg");
        when(multipartFile.getContentType()).thenReturn("image/png");

        IOException exception = assertThrows(IOException.class, () -> {
            minioStorageService.store(multipartFile, "images");
        });
        
        assertTrue(exception.getMessage().contains("Invalid file extension"));
        assertTrue(exception.getMessage().contains(".png"));
        verify(s3Client, never()).putObject(any(PutObjectRequest.class));
    }

    @Test
    void testRetrieveSuccess() throws IOException {

        String fileUrl = "avatars/test-file.png";
        S3Object s3Object = mock(S3Object.class);
        S3ObjectInputStream s3InputStream = mock(S3ObjectInputStream.class);

        when(s3Client.getObject(bucketName, fileUrl)).thenReturn(s3Object);
        when(s3Object.getObjectContent()).thenReturn(s3InputStream);

        InputStream result = minioStorageService.retrieve(fileUrl);

        assertNotNull(result);
        assertEquals(s3InputStream, result);
        verify(s3Client, times(1)).getObject(bucketName, fileUrl);
    }

    @Test
    void testRetrieveFileNotFoundThrowsException() {

        String fileUrl = "nonexistent/file.txt";
        when(s3Client.getObject(bucketName, fileUrl)).thenThrow(new RuntimeException("Not found"));

        IOException exception = assertThrows(IOException.class, () -> {
            minioStorageService.retrieve(fileUrl);
        });

        assertTrue(exception.getMessage().contains("Could not retrieve file"));
        assertTrue(exception.getMessage().contains(fileUrl));
    }

    @Test
    void testDeleteSuccess() throws IOException {

        String fileUrl = "images/old-photo.jpg";
        doNothing().when(s3Client).deleteObject(bucketName, fileUrl);

        minioStorageService.delete(fileUrl);

        verify(s3Client, times(1)).deleteObject(bucketName, fileUrl);
    }

    @Test
    void testDeleteFileNotFoundThrowsException() {

        String fileUrl = "missing/file.txt";
        doThrow(new RuntimeException("Delete failed")).when(s3Client).deleteObject(bucketName, fileUrl);

        IOException exception = assertThrows(IOException.class, () -> {
            minioStorageService.delete(fileUrl);
        });

        assertTrue(exception.getMessage().contains("Could not delete file"));
        assertTrue(exception.getMessage().contains(fileUrl));
    }

    @Test
    void testGetPublicUrl() {

        String fileIdentifier = "avatars/user123.png";

        String publicUrl = minioStorageService.getPublicUrl(fileIdentifier);

        assertNotNull(publicUrl);
        assertEquals("/api/v1/files/" + fileIdentifier, publicUrl);
    }

    @Test
    void testGetPublicUrlDifferentIdentifier() {

        String fileIdentifier = "documents/contract-2024.pdf";

        String publicUrl = minioStorageService.getPublicUrl(fileIdentifier);

        assertEquals("/api/v1/files/documents/contract-2024.pdf", publicUrl);
    }
}
