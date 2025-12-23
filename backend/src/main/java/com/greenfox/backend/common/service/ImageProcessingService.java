package com.greenfox.backend.common.service;

import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Service for image processing and optimization.
 * Compresses images while maintaining quality before uploading to storage.
 */
@Slf4j
@Service
public class ImageProcessingService {

    // Maximum dimensions for images (helps reduce file size)
    private static final int MAX_WIDTH = 2048;
    private static final int MAX_HEIGHT = 2048;

    // Quality setting (0.0 to 1.0, where 1.0 is best quality)
    // 0.85 provides a good balance between quality and file size
    private static final double COMPRESSION_QUALITY = 0.85;

    /**
     * Process and compress an image file.
     * - Resizes images larger than MAX_WIDTH x MAX_HEIGHT
     * - Applies compression to reduce file size
     * - Maintains aspect ratio
     * - Preserves image quality
     *
     * @param file Original image file
     * @return Compressed image as byte array
     * @throws IOException if image processing fails
     */
    public byte[] processImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("Image file is empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !isImageContentType(contentType)) {
            throw new IOException("File is not a valid image");
        }

        byte[] originalBytes = file.getBytes();
        long originalSize = originalBytes.length;

        try {
            // Read the image
            BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(originalBytes));
            if (originalImage == null) {
                throw new IOException("Failed to read image file");
            }

            // Get original dimensions
            int originalWidth = originalImage.getWidth();
            int originalHeight = originalImage.getHeight();

            // Determine output format (preserve original format, default to JPEG)
            String outputFormat = getOutputFormat(contentType);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            // Build thumbnailator pipeline
            Thumbnails.Builder<BufferedImage> builder = Thumbnails.of(originalImage);

            // Only resize if image is larger than max dimensions
            if (originalWidth > MAX_WIDTH || originalHeight > MAX_HEIGHT) {
                builder.size(MAX_WIDTH, MAX_HEIGHT);
                log.debug("Resizing image from {}x{} to fit within {}x{}",
                        originalWidth, originalHeight, MAX_WIDTH, MAX_HEIGHT);
            } else {
                // Keep original size but still apply compression
                builder.size(originalWidth, originalHeight);
            }

            // Apply compression quality
            builder.outputFormat(outputFormat)
                   .outputQuality(COMPRESSION_QUALITY)
                   .toOutputStream(outputStream);

            byte[] compressedBytes = outputStream.toByteArray();
            long compressedSize = compressedBytes.length;

            // Calculate compression ratio
            double compressionRatio = (1 - ((double) compressedSize / originalSize)) * 100;

            log.info("Image processed: {}x{} -> {} KB to {} KB ({:.1f}% reduction)",
                    originalWidth, originalHeight,
                    originalSize / 1024,
                    compressedSize / 1024,
                    compressionRatio);

            return compressedBytes;

        } catch (Exception e) {
            log.error("Failed to process image: {}", e.getMessage(), e);
            throw new IOException("Failed to process image: " + e.getMessage(), e);
        }
    }

    /**
     * Check if content type is a supported image format.
     */
    private boolean isImageContentType(String contentType) {
        return contentType.equals("image/jpeg") ||
               contentType.equals("image/jpg") ||
               contentType.equals("image/png") ||
               contentType.equals("image/webp");
    }

    /**
     * Get output format based on content type.
     * WebP is converted to JPEG for better compatibility.
     */
    private String getOutputFormat(String contentType) {
        if (contentType.equals("image/png")) {
            return "png";
        }
        // Convert WebP and JPEG to JPEG
        return "jpg";
    }
}
