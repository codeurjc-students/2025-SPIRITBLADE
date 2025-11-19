package com.tfg.tfg.util;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * Utility class for PNG file validation.
 * Centralizes validation logic to avoid code duplication across services.
 */
public final class PngFileValidator {

    private static final String ALLOWED_CONTENT_TYPE = "image/png";
    private static final String PNG_EXTENSION = ".png";
    private static final byte[] PNG_MAGIC_BYTES = {
        (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
    };

    private PngFileValidator() {
        // Utility class - prevent instantiation
    }

    /**
     * Validates that a MultipartFile is a PNG image.
     *
     * @param file The file to validate
     * @throws IllegalArgumentException if validation fails
     * @throws IOException if file reading fails
     */
    public static void validatePngFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be null or empty");
        }

        validateContentType(file.getContentType());
        
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new IllegalArgumentException("Filename is required");
        }
        
        validateExtension(originalFilename);
        
        // Optional: Validate magic bytes for extra security
        try (InputStream inputStream = file.getInputStream()) {
            validateMagicBytes(inputStream);
        }
    }

    /**
     * Validates that the content type is image/png.
     *
     * @param contentType The content type to validate
     * @throws IllegalArgumentException if content type is invalid
     */
    public static void validateContentType(String contentType) {
        if (contentType == null || !contentType.equalsIgnoreCase(ALLOWED_CONTENT_TYPE)) {
            throw new IllegalArgumentException(
                "Invalid file type. Only PNG images are allowed. Provided: " + contentType
            );
        }
    }

    /**
     * Validates that the file extension is .png.
     *
     * @param fileName The file name to validate
     * @throws IllegalArgumentException if extension is invalid
     */
    public static void validateExtension(String fileName) {
        if (fileName == null || !fileName.toLowerCase().endsWith(PNG_EXTENSION)) {
            throw new IllegalArgumentException(
                "Invalid file extension. Only .png files are allowed"
            );
        }
    }

    /**
     * Validates that the file starts with PNG magic bytes.
     * Reads the first 8 bytes of the stream to verify it's a valid PNG file.
     *
     * @param inputStream The input stream to validate
     * @throws IOException if reading fails
     * @throws IllegalArgumentException if magic bytes don't match
     */
    public static void validateMagicBytes(InputStream inputStream) throws IOException {
        byte[] header = new byte[PNG_MAGIC_BYTES.length];
        int bytesRead = inputStream.read(header);
        
        if (bytesRead != PNG_MAGIC_BYTES.length) {
            throw new IllegalArgumentException(
                "Invalid PNG file: unable to read file header"
            );
        }
        
        for (int i = 0; i < PNG_MAGIC_BYTES.length; i++) {
            if (header[i] != PNG_MAGIC_BYTES[i]) {
                throw new IllegalArgumentException(
                    "Invalid PNG file: file header does not match PNG signature"
                );
            }
        }
    }

    /**
     * Returns the allowed content type.
     *
     * @return The PNG content type string
     */
    public static String getAllowedContentType() {
        return ALLOWED_CONTENT_TYPE;
    }

    /**
     * Returns the PNG file extension.
     *
     * @return The .png extension string
     */
    public static String getPngExtension() {
        return PNG_EXTENSION;
    }
}
