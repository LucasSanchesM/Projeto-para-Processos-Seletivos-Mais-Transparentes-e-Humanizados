package com.unisal.processoseletivoapi.storage;

import java.io.IOException;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.unisal.processoseletivoapi.exception.BusinessException;

@Component
public class ImageUploadValidator {

    private static final long MAX_SIZE = 2L * 1024 * 1024;
    private static final Map<String, String> ALLOWED = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp");

    public record ValidatedImage(byte[] content, String contentType, String extension) {}

    public ValidatedImage validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Arquivo não enviado");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Imagem deve ter no máximo 2MB");
        }
        String contentType = file.getContentType();
        String extension = contentType == null ? null : ALLOWED.get(contentType);
        if (extension == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Formato inválido (use JPG, PNG ou WEBP)");
        }
        try {
            return new ValidatedImage(file.getBytes(), contentType, extension);
        } catch (IOException e) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Não foi possível ler o arquivo");
        }
    }
}