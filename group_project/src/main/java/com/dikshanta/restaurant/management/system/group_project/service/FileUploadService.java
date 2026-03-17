package com.dikshanta.restaurant.management.system.group_project.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class FileUploadService {
    @Value("${file.upload-dir}")
    private String uploadDir;

    public String uploadFile(MultipartFile file, String folder) {

        try {
            Path uploadPath = Paths.get(STR."\{uploadDir}/\{folder}");

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            String fileName = STR."\{System.currentTimeMillis()}_\{file.getOriginalFilename()}";

            Path filePath = uploadPath.resolve(fileName);

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return STR."/uploads/\{folder}/\{fileName}";

        } catch (IOException e) {
            throw new RuntimeException("File upload failed");
        }
    }
}

