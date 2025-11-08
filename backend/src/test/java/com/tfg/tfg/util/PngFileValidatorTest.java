package com.tfg.tfg.util;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PngFileValidator utility class
 */
class PngFileValidatorTest {

    private static final byte[] PNG_MAGIC_BYTES = {
        (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
    };

    @Test
    void testValidateContentTypeValid() {
        assertDoesNotThrow(() -> PngFileValidator.validateContentType("image/png"));
        assertDoesNotThrow(() -> PngFileValidator.validateContentType("IMAGE/PNG"));
    }

    @Test
    void testValidateContentTypeInvalid() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> PngFileValidator.validateContentType("image/jpeg")
        );
        assertTrue(exception.getMessage().contains("Only PNG images are allowed"));
    }

    @Test
    void testValidateContentTypeNull() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> PngFileValidator.validateContentType(null)
        );
        assertTrue(exception.getMessage().contains("Only PNG images are allowed"));
    }

    @Test
    void testValidateExtensionValid() {
        assertDoesNotThrow(() -> PngFileValidator.validateExtension("file.png"));
        assertDoesNotThrow(() -> PngFileValidator.validateExtension("file.PNG"));
        assertDoesNotThrow(() -> PngFileValidator.validateExtension("path/to/file.png"));
    }

    @Test
    void testValidateExtensionInvalid() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> PngFileValidator.validateExtension("file.jpg")
        );
        assertTrue(exception.getMessage().contains("Only .png files are allowed"));
    }

    @Test
    void testValidateExtensionNoExtension() {
        assertThrows(
            IllegalArgumentException.class,
            () -> PngFileValidator.validateExtension("file")
        );
    }

    @Test
    void testValidateMagicBytesValid() throws IOException {
        byte[] validPngHeader = PNG_MAGIC_BYTES.clone();
        try (InputStream inputStream = new ByteArrayInputStream(validPngHeader)) {
            assertDoesNotThrow(() -> PngFileValidator.validateMagicBytes(inputStream));
        }
    }

    @Test
    void testValidateMagicBytesInvalid() throws IOException {
        byte[] invalidHeader = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        try (InputStream inputStream = new ByteArrayInputStream(invalidHeader)) {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> PngFileValidator.validateMagicBytes(inputStream)
            );
            assertTrue(exception.getMessage().contains("file header does not match"));
        }
    }

    @Test
    void testValidateMagicBytesTooShort() throws IOException {
        byte[] shortHeader = {0x00, 0x00};
        try (InputStream inputStream = new ByteArrayInputStream(shortHeader)) {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> PngFileValidator.validateMagicBytes(inputStream)
            );
            assertTrue(exception.getMessage().contains("unable to read file header"));
        }
    }

    @Test
    void testValidatePngFileMultipartFileValid() {
        byte[] content = new byte[100];
        System.arraycopy(PNG_MAGIC_BYTES, 0, content, 0, PNG_MAGIC_BYTES.length);
        
        MultipartFile file = new MockMultipartFile(
            "file",
            "test.png",
            "image/png",
            content
        );

        assertDoesNotThrow(() -> PngFileValidator.validatePngFile(file));
    }

    @Test
    void testValidatePngFileMultipartFileInvalidContentType() {
        MultipartFile file = new MockMultipartFile(
            "file",
            "test.png",
            "image/jpeg",
            new byte[100]
        );

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> PngFileValidator.validatePngFile(file)
        );
        assertTrue(exception.getMessage().contains("Only PNG images are allowed"));
    }

    @Test
    void testValidatePngFileMultipartFileInvalidExtension() {
        MultipartFile file = new MockMultipartFile(
            "file",
            "test.jpg",
            "image/png",
            new byte[100]
        );

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> PngFileValidator.validatePngFile(file)
        );
        assertTrue(exception.getMessage().contains("Only .png files are allowed"));
    }

    @Test
    void testValidatePngFileMultipartFileInvalidMagicBytes() {
        byte[] invalidContent = new byte[100]; // All zeros
        
        MultipartFile file = new MockMultipartFile(
            "file",
            "test.png",
            "image/png",
            invalidContent
        );

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> PngFileValidator.validatePngFile(file)
        );
        assertTrue(exception.getMessage().contains("file header does not match"));
    }

    @Test
    void testValidatePngFileMultipartFileEmpty() {
        MultipartFile file = new MockMultipartFile(
            "file",
            "test.png",
            "image/png",
            new byte[0]
        );

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> PngFileValidator.validatePngFile(file)
        );
        assertTrue(exception.getMessage().contains("cannot be null or empty"));
    }

    @Test
    void testValidatePngFileStringParamsValid() {
        assertDoesNotThrow(() -> 
            PngFileValidator.validatePngFile("image/png", "test.png")
        );
    }

    @Test
    void testValidatePngFileStringParamsInvalid() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> PngFileValidator.validatePngFile("image/jpeg", "test.jpg")
        );
        assertTrue(exception.getMessage().contains("Only PNG images are allowed"));
    }

    @Test
    void testGetAllowedContentType() {
        assertEquals("image/png", PngFileValidator.getAllowedContentType());
    }

    @Test
    void testGetPngExtension() {
        assertEquals(".png", PngFileValidator.getPngExtension());
    }
}
