package com.dikshanta.restaurant.management.system.group_project.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
public class FileUploadService {
    private static final String BASE_DIR = "uploads/";

    public String uploadFile(MultipartFile file, String folder) {

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        String contentType = file.getContentType();
        if (contentType == null ||
                !(contentType.equals("image/png") ||
                        contentType.equals("image/jpeg") ||
                        contentType.equals("image/jpg"))) {
            throw new RuntimeException("Only image files are allowed");
        }
        try {
            File directory = new File(BASE_DIR + folder);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

            String filePath = BASE_DIR + folder + "/" + fileName;

            file.transferTo(new File(filePath));
            return "/" + filePath;
        } catch (IOException e) {
            throw new RuntimeException("File upload failed", e);
        }
    }
}
