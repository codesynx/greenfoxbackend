package com.greenfox.backend.common.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Service for file storage operations using GCP Cloud Storage.
 * Falls back to local mock storage in development.
 */
@Slf4j
@Service
public class StorageService {

    @Value("${app.gcp.project-id:}")
    private String projectId;

    @Value("${app.gcp.bucket-name:greenfox-storage}")
    private String bucketName;

    private Storage storage;
    private boolean initialized = false;

    /**
     * Initialize GCP storage client.
     * Called lazily on first use.
     * Supports credentials via GOOGLE_APPLICATION_CREDENTIALS environment variable or default credentials.
     */
    private void initStorage() {
        if (initialized) return;

        try {
            if (projectId != null && !projectId.isBlank() && !projectId.startsWith("your-")) {
                StorageOptions.Builder builder = StorageOptions.newBuilder()
                        .setProjectId(projectId);
                
                // Check for JSON credentials in environment variable (for Render/cloud deployments)
                String serviceAccountJson = System.getenv("GCP_SERVICE_ACCOUNT_JSON");
                if (serviceAccountJson != null && !serviceAccountJson.isBlank()) {
                    try {
                        GoogleCredentials credentials = GoogleCredentials.fromStream(
                                new ByteArrayInputStream(serviceAccountJson.getBytes(StandardCharsets.UTF_8)));
                        builder.setCredentials(credentials);
                        log.info("GCP Storage: Using credentials from GCP_SERVICE_ACCOUNT_JSON environment variable");
                    } catch (IOException e) {
                        log.error("Failed to parse GCP_SERVICE_ACCOUNT_JSON: {}", e.getMessage());
                        throw e;
                    }
                } else {
                    // Fall back to GOOGLE_APPLICATION_CREDENTIALS (file path) or default credentials
                    String credentialsPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
                    log.info("GCP Storage: Project ID: {}, Credentials: {}", 
                            projectId, credentialsPath != null ? "File: " + credentialsPath : "Using default credentials");
                    // GCP SDK will automatically use GOOGLE_APPLICATION_CREDENTIALS env var
                    // or try default application credentials
                }
                
                storage = builder.build().getService();
                
                // Test connection by trying to access bucket
                try {
                    Bucket bucket = storage.get(bucketName);
                    if (bucket != null) {
                        initialized = true;
                        log.info("GCP Cloud Storage initialized successfully for project: {}, bucket: {}", projectId, bucketName);
                    } else {
                        log.error("Bucket '{}' not found in project '{}'", bucketName, projectId);
                        initialized = false;
                    }
                } catch (com.google.cloud.storage.StorageException e) {
                    if (e.getCode() == 401 || e.getCode() == 403) {
                        log.error("Authentication failed for GCP. Check GOOGLE_APPLICATION_CREDENTIALS and service account permissions. " +
                                "Service account needs 'Storage Admin' or 'Storage Object Creator' role for bucket '{}'. Error: {}", 
                                bucketName, e.getMessage());
                    } else if (e.getCode() == 404) {
                        log.error("Bucket '{}' not found in project '{}'. Please create the bucket first.", bucketName, projectId);
                    } else {
                        log.error("Failed to access bucket '{}': {}", bucketName, e.getMessage());
                    }
                    initialized = false;
                } catch (Exception e) {
                    log.error("Unexpected error accessing bucket '{}': {}", bucketName, e.getMessage(), e);
                    initialized = false;
                }
            } else {
                log.warn("GCP Cloud Storage not configured - using mock mode");
            }
        } catch (Exception e) {
            log.error("Failed to initialize GCP Cloud Storage: {}", e.getMessage(), e);
            initialized = false;
        }
    }

    /**
     * Upload a file to cloud storage.
     *
     * @param file     MultipartFile to upload
     * @param folder   Folder/prefix in bucket (e.g., "avatars", "resorts")
     * @return Public URL of uploaded file, or mock URL in dev mode
     */
    public String uploadFile(MultipartFile file, String folder) throws IOException {
        return uploadBytes(file.getBytes(), file.getOriginalFilename(), file.getContentType(), folder);
    }

    /**
     * Upload a byte array to cloud storage.
     *
     * @param bytes       File content in bytes
     * @param originalFilename Original filename (used for extension)
     * @param contentType MIME type of the file
     * @param folder      Folder/prefix in bucket
     * @return Public URL of uploaded file
     * @throws IOException if upload fails
     */
    public String uploadBytes(byte[] bytes, String originalFilename, String contentType, String folder) throws IOException {
        initStorage();

        String filename = generateFilename(originalFilename);
        String objectName = folder + "/" + filename;

        if (!initialized) {
            // Mock mode for development
            String mockUrl = "https://storage.googleapis.com/" + bucketName + "/" + objectName;
            log.info("MOCK upload: {} -> {}", originalFilename, mockUrl);
            return mockUrl;
        }

        try {
            BlobId blobId = BlobId.of(bucketName, objectName);
            BlobInfo.Builder blobInfoBuilder = BlobInfo.newBuilder(blobId);
            
            if (contentType != null && !contentType.isBlank()) {
                blobInfoBuilder.setContentType(contentType);
            }
            
            BlobInfo blobInfo = blobInfoBuilder.build();

            storage.create(blobInfo, bytes);

            // Generate public URL
            String publicUrl = String.format("https://storage.googleapis.com/%s/%s", bucketName, objectName);
            log.info("File uploaded successfully: {}", publicUrl);

            return publicUrl;
        } catch (Exception e) {
            log.error("Failed to upload file to GCP: {}", e.getMessage(), e);
            throw new IOException("Failed to upload file to cloud storage: " + e.getMessage(), e);
        }
    }

    /**
     * Delete a file from cloud storage.
     *
     * @param fileUrl Full URL of the file to delete
     */
    public void deleteFile(String fileUrl) {
        initStorage();

        if (!initialized) {
            log.info("MOCK delete: {}", fileUrl);
            return;
        }

        try {
            // Extract object name from URL
            String prefix = "https://storage.googleapis.com/" + bucketName + "/";
            if (fileUrl.startsWith(prefix)) {
                String objectName = fileUrl.substring(prefix.length());
                BlobId blobId = BlobId.of(bucketName, objectName);
                storage.delete(blobId);
                log.info("File deleted: {}", objectName);
            }
        } catch (Exception e) {
            log.error("Failed to delete file: {}", e.getMessage());
        }
    }

    private String generateFilename(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return UUID.randomUUID().toString() + extension;
    }
}

