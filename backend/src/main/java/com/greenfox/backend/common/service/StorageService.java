package com.greenfox.backend.common.service;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
     */
    private void initStorage() {
        if (initialized) return;

        try {
            if (projectId != null && !projectId.isBlank() && !projectId.startsWith("your-")) {
                storage = StorageOptions.newBuilder()
                        .setProjectId(projectId)
                        .build()
                        .getService();
                initialized = true;
                log.info("GCP Cloud Storage initialized for project: {}", projectId);
            } else {
                log.warn("GCP Cloud Storage not configured - using mock mode");
            }
        } catch (Exception e) {
            log.error("Failed to initialize GCP Cloud Storage: {}", e.getMessage());
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
     */
    public String uploadBytes(byte[] bytes, String originalFilename, String contentType, String folder) {
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
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(contentType)
                    .build();

            storage.create(blobInfo, bytes);

            String publicUrl = String.format("https://storage.googleapis.com/%s/%s", bucketName, objectName);
            log.info("File uploaded: {}", publicUrl);

            return publicUrl;
        } catch (Exception e) {
            log.error("Failed to upload file: {}", e.getMessage());
            return null;
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

