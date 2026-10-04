package com.unisal.processoseletivoapi.storage;

public interface FileStorage {
    StoredFile store(String directory, String fileName, byte[] content, String contentType);
    void deleteQuietly(String key);
}
