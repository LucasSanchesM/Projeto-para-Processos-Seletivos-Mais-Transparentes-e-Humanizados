package com.unisal.selectaapi.candidate.storage;

public interface FileStorage {
    StoredFile store(String directory, String fileName, byte[] content, String contentType);
    void deleteQuietly(String key);
}
