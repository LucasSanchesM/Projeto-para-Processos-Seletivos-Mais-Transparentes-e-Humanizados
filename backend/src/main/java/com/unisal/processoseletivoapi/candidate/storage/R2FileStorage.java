package com.unisal.processoseletivoapi.candidate.storage;

import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.unisal.processoseletivoapi.exception.BusinessException;
import jakarta.annotation.PreDestroy;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Service
public class R2FileStorage implements FileStorage {

    private static final Logger LOGGER = LoggerFactory.getLogger(R2FileStorage.class);

    private final String endpoint;
    private final String accessKeyId;
    private final String secretAccessKey;
    private final String bucket;
    private final String publicBaseUrl;
    private volatile S3Client client;

    public R2FileStorage(
            @Value("${r2.endpoint:}") String endpoint,
            @Value("${r2.access-key-id:}") String accessKeyId,
            @Value("${r2.secret-access-key:}") String secretAccessKey,
            @Value("${r2.bucket:}") String bucket,
            @Value("${r2.public-base-url:}") String publicBaseUrl) {
        this.endpoint = endpoint.trim();
        this.accessKeyId = accessKeyId.trim();
        this.secretAccessKey = secretAccessKey.trim();
        this.bucket = bucket.trim();
        this.publicBaseUrl = removeTrailingSlash(publicBaseUrl.trim());
    }

    @Override
    public StoredFile store(String directory, String fileName, byte[] content, String contentType) {
        ensureConfigured();
        
        String key = directory + "/" + fileName;

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .contentLength((long) content.length)
                .build();

        try {
            getClient().putObject(request, RequestBody.fromBytes(content));
            return new StoredFile(key, publicBaseUrl + "/" + key);
        } catch (S3Exception exception) {
            LOGGER.error("Falha ao enviar arquivo ao R2: status={}", exception.statusCode());
            throw new BusinessException(HttpStatus.BAD_GATEWAY, "Não foi possível armazenar o arquivo");
        } catch (RuntimeException exception) {
            LOGGER.error("Falha ao enviar arquivo ao R2", exception);
            throw new BusinessException(HttpStatus.BAD_GATEWAY, "Não foi possível armazenar o arquivo");
        }
    }

    @Override
    public void deleteQuietly(String key) {
        if (key == null || key.isBlank() || !isConfigured()) {
            return;
        }
        try {
            getClient().deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
        } catch (RuntimeException exception) {
            LOGGER.warn("Não foi possível remover o arquivo antigo do R2: key={}", key, exception);
        }
    }

    private S3Client getClient() {
        S3Client current = client;
        if (current == null) {
            synchronized (this) {
                current = client;
                if (current == null) {
                    current = S3Client.builder()
                            .endpointOverride(URI.create(endpoint))
                            .region(Region.of("auto"))
                            .credentialsProvider(StaticCredentialsProvider.create(
                                    AwsBasicCredentials.create(accessKeyId, secretAccessKey)))
                            .serviceConfiguration(S3Configuration.builder()
                                    .pathStyleAccessEnabled(true)
                                    .build())
                            .build();
                    client = current;
                }
            }
        }
        return current;
    }

    private void ensureConfigured() {
        if (!isConfigured()) {
            throw new BusinessException(HttpStatus.SERVICE_UNAVAILABLE, "Upload de arquivos não foi configurado");
        }
    }

    private boolean isConfigured() {
        return !endpoint.isBlank()
                && !accessKeyId.isBlank()
                && !secretAccessKey.isBlank()
                && !bucket.isBlank()
                && !publicBaseUrl.isBlank();
    }

    private String removeTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    @PreDestroy
    void close() {
        S3Client current = client;
        if (current != null) {
            current.close();
        }
    }
}