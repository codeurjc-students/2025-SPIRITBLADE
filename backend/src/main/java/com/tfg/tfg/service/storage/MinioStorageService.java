package com.tfg.tfg.service.storage;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * MinIO/S3 implementation of StorageService.
 * Uses AWS S3 SDK configured to work with MinIO.
 * Activate with profile "minio" or "prod"
 */
@Service
public class MinioStorageService {

    private static final String ALLOWED_CONTENT_TYPE = "image/png";
    private static final String PNG_EXTENSION = ".png";

    @Value("${minio.endpoint:http://localhost:9000}")
    private String minioEndpoint;

    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String secretKey;

    @Value("${minio.bucket-name:spiritblade-uploads}")
    private String bucketName;

    @Value("${minio.region:us-east-1}")
    private String region;

    private AmazonS3 s3Client;

    @PostConstruct
    public void init() {
        BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        
        this.s3Client = AmazonS3ClientBuilder
                .standard()
                .withEndpointConfiguration(
                    new AwsClientBuilder.EndpointConfiguration(minioEndpoint, region)
                )
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withPathStyleAccessEnabled(true) // Important for MinIO
                .build();

        // Create bucket if it doesn't exist
        if (!s3Client.doesBucketExistV2(bucketName)) {
            s3Client.createBucket(bucketName);
        }
    }

    public String store(MultipartFile file, String folder) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("Cannot store empty file");
        }

        // Validate that only PNG images are allowed
        String contentType = file.getContentType();
        if (contentType == null || !contentType.equalsIgnoreCase(ALLOWED_CONTENT_TYPE)) {
            throw new IOException("Invalid file type. Only PNG images are allowed. Provided: " + contentType);
        }

        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".") 
            ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
            : "";
        
        // Ensure extension is .png
        if (!extension.equalsIgnoreCase(PNG_EXTENSION)) {
            throw new IOException("Invalid file extension. Only .png files are allowed");
        }
        
        String uniqueFileName = UUID.randomUUID().toString() + extension;
        String key = folder != null && !folder.isEmpty() 
            ? folder + "/" + uniqueFileName 
            : uniqueFileName;

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(ALLOWED_CONTENT_TYPE); // Force PNG content type
        metadata.setContentLength(file.getSize());

        try (InputStream inputStream = file.getInputStream()) {
            s3Client.putObject(new PutObjectRequest(bucketName, key, inputStream, metadata));
        }

        return key;
    }

    public String store(InputStream inputStream, String fileName, String contentType, String folder) {
        // Validate that only PNG images are allowed
        if (contentType == null || !contentType.equalsIgnoreCase(ALLOWED_CONTENT_TYPE)) {
            throw new IllegalArgumentException("Invalid file type. Only PNG images are allowed. Provided: " + contentType);
        }
        
        String extension = fileName.contains(".") 
            ? fileName.substring(fileName.lastIndexOf(".")) 
            : "";
        
        // Ensure extension is .png
        if (!extension.equalsIgnoreCase(PNG_EXTENSION)) {
            throw new IllegalArgumentException("Invalid file extension. Only .png files are allowed");
        }
        
        String uniqueFileName = UUID.randomUUID().toString() + extension;
        String key = folder != null && !folder.isEmpty() 
            ? folder + "/" + uniqueFileName 
            : uniqueFileName;

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(ALLOWED_CONTENT_TYPE); // Force PNG content type

        s3Client.putObject(new PutObjectRequest(bucketName, key, inputStream, metadata));

        return key;
    }

    public InputStream retrieve(String fileUrl) throws IOException {
        try {
            S3Object s3Object = s3Client.getObject(bucketName, fileUrl);
            return s3Object.getObjectContent();
        } catch (Exception e) {
            throw new IOException("Could not retrieve file: " + fileUrl, e);
        }
    }

    public void delete(String fileUrl) throws IOException {
        try {
            s3Client.deleteObject(bucketName, fileUrl);
        } catch (Exception e) {
            throw new IOException("Could not delete file: " + fileUrl, e);
        }
    }

    public boolean exists(String fileUrl) {
        try {
            return s3Client.doesObjectExist(bucketName, fileUrl);
        } catch (Exception e) {
            return false;
        }
    }

    public String getPublicUrl(String fileIdentifier) {
        // Return URL that goes through our FileController instead of direct MinIO URL
        // This allows the backend to serve files through the API
        return "/api/v1/files/" + fileIdentifier;
    }
}
