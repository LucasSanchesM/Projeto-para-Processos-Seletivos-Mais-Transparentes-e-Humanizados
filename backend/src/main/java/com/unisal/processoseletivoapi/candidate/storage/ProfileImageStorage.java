package com.unisal.processoseletivoapi.candidate.storage;

import java.util.UUID;

public interface ProfileImageStorage {

    StoredProfileImage store(UUID userId, byte[] content, String contentType, String extension);

    void deleteQuietly(String key);
}
