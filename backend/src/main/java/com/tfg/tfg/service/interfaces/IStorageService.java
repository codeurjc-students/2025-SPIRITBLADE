package com.tfg.tfg.service.interfaces;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

public interface IStorageService {

    String store(MultipartFile file, String folder) throws IOException;

    InputStream retrieve(String fileUrl) throws IOException;

    void delete(String fileUrl) throws IOException;

    String getPublicUrl(String fileIdentifier);
}
