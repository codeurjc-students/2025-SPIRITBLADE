package com.tfg.tfg.service.storage;

import org.springframework.web.multipart.MultipartFile;

public interface IUserAvatarService {

    String uploadAvatar(String username, MultipartFile file);

    void deleteAvatar(String username);
}
