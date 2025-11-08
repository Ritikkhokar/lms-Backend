package com.example.lmsProject.ServiceImpl;

import com.example.lmsProject.service.StorageService;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import java.io.InputStream;

@Service
public class S3Service implements StorageService {

    private final S3Client s3Client;
    private final String bucketName;

    public S3Service(
            @Value("${cloud.aws.region.static}") String region,
            @Value("${s3.bucket.name}") String bucketName
    ) {
        Dotenv dotenv = Dotenv.load();
        String accessKey = dotenv.get("AWS_ACCESS_KEY");
        String secretKey = dotenv.get("AWS_SECRET_KEY");
        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .build();
        this.bucketName = bucketName;
    }

    public String uploadFile(String key, InputStream inputStream, long contentLength, String contentType) {
        PutObjectRequest putReq = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)
//                .acl(ObjectCannedACL.PUBLIC_READ) // Or private, if you want signed URLs
                .build();
        s3Client.putObject(putReq, software.amazon.awssdk.core.sync.RequestBody.fromInputStream(inputStream, contentLength));
        return getFileUrl(key);
    }

    @Override
    public String getFileUrl(String key) {
        return String.format("https://%s.s3.amazonaws.com/%s", bucketName, key);
    }

    @Override
    public boolean deleteFile(String key) {
        try {
            DeleteObjectRequest deleteReq = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            s3Client.deleteObject(deleteReq);
            return true;
        } catch (Exception e) {
            // Optionally log or throw
            return false;
        }
    }


}

