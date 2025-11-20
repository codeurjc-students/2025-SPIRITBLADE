package com.tfg.tfg.controller;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.tfg.tfg.service.storage.MinioStorageService;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller for file upload and download operations
 */
@RestController
@RequestMapping("/api/v1/files")
@CrossOrigin(origins = "*", allowCredentials = "false")
public class FileController {

    private static final String SUCCESS_KEY = "success";
    private static final String MESSAGE_KEY = "message";
    private static final String FILE_ID_KEY = "fileId";
    private static final String ERROR_KEY = "error";
    private static final String FALSE_VALUE = "false";
    private static final String ALLOWED_CONTENT_TYPE = "image/png";

    private final MinioStorageService storageService;

    public FileController(MinioStorageService storageService) {
        this.storageService = storageService;
    }

    /**
     * Upload a file (only PNG images allowed)
     * @param file The file to upload
     * @param folder Optional folder to organize files
     * @return JSON response with file URL
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", required = false) String folder) {
        
        try {
            // Validate file type - only PNG allowed
            String contentType = file.getContentType();
            if (contentType == null || !ALLOWED_CONTENT_TYPE.equalsIgnoreCase(contentType)) {
                Map<String, String> error = new HashMap<>();
                error.put(SUCCESS_KEY, FALSE_VALUE);
                error.put(ERROR_KEY, "Invalid file type. Only PNG images are allowed");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            String fileIdentifier = storageService.store(file, folder);
            String publicUrl = storageService.getPublicUrl(fileIdentifier);
            
            Map<String, String> response = new HashMap<>();
            response.put(SUCCESS_KEY, "true");
            response.put(FILE_ID_KEY, fileIdentifier);
            response.put("url", publicUrl);
            
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            Map<String, String> error = new HashMap<>();
            error.put(SUCCESS_KEY, FALSE_VALUE);
            error.put(ERROR_KEY, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Delete a file
     * @param fileId The file identifier
     * @return Success response
     */
    @DeleteMapping("/delete/{fileId}")
    public ResponseEntity<Map<String, String>> deleteFile(@PathVariable String fileId) {
        try {
            storageService.delete(fileId);
            
            Map<String, String> response = new HashMap<>();
            response.put(SUCCESS_KEY, "true");
            response.put(MESSAGE_KEY, "File deleted successfully");
            
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            Map<String, String> error = new HashMap<>();
            error.put(SUCCESS_KEY, FALSE_VALUE);
            error.put(ERROR_KEY, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Download/serve a file
     * @param fileId The file identifier (path within storage)
     * @return The file as a stream
     */
    @GetMapping("/{folder}/{filename:.+}")
    public ResponseEntity<InputStreamResource> getFile(
            @PathVariable String folder,
            @PathVariable String filename) {
        
        try {
            // Only PNG images are allowed in MinIO
            String lowerFilename = filename.toLowerCase();
            if (!lowerFilename.endsWith(".png")) {
                return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).build();
            }
            
            String fileId = folder + "/" + filename;
            InputStream inputStream = storageService.retrieve(fileId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(ALLOWED_CONTENT_TYPE));
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(new InputStreamResource(inputStream));
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Download/serve a file from root directory
     * @param filename The filename
     * @return The file as a stream
     */
    @GetMapping("/{filename:.+}")
    public ResponseEntity<InputStreamResource> getFileFromRoot(@PathVariable String filename) {
        try {
            // Only PNG images are allowed in MinIO
            String lowerFilename = filename.toLowerCase();
            if (!lowerFilename.endsWith(".png")) {
                return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).build();
            }
            
            InputStream inputStream = storageService.retrieve(filename);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(ALLOWED_CONTENT_TYPE));
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(new InputStreamResource(inputStream));
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }
}