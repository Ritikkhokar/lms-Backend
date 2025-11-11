package com.example.lmsProject.service;

import java.io.InputStream;

public interface StorageService {
    /**
     * Upload a file.
     *
     * @param key key or path within the "bucket"/"container"
     * @param inputStream the file data
     * @param contentLength file size in bytes
     * @param contentType MIME type (e.g., "application/pdf")
     * @return Public or downloadable URL for the file, or Object Storage key
     */
    String uploadFile(String key, InputStream inputStream, long contentLength, String contentType);

    /**
     * Get a public or pre-signed file URL for downloading.
     *
     * @param key key/path in storage
     * @return public or temporary download URL
     */
    String getFileUrl(String key);

    // Add other standard methods if needed:
    boolean deleteFile(String key);
}
