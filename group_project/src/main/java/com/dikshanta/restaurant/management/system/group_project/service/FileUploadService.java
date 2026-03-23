package com.dikshanta.restaurant.management.system.group_project.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
public class FileUploadService {

    // Absolute base directory inside project folder
    private static final String BASE_DIR = System.getProperty("user.dir") + "/uploads/";

    public String uploadFile(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        // Check file type
        String contentType = file.getContentType();
        if (contentType == null ||
                !(contentType.equals("image/png") ||
                        contentType.equals("image/jpeg") ||
                        contentType.equals("image/jpg"))) {
            throw new RuntimeException("Only image files are allowed");
        }

        try {
            // Create main uploads folder
            File baseDir = new File(BASE_DIR);
            if (!baseDir.exists()) {
                boolean createdBase = baseDir.mkdirs();
                System.out.println("Created base uploads folder: " + createdBase);
            }

            // Create subfolder (users, restaurant, foodItem)
            File subFolder = new File(BASE_DIR + folder);
            if (!subFolder.exists()) {
                boolean createdSub = subFolder.mkdirs();
                System.out.println("Created subfolder '" + folder + "': " + createdSub);
            }

            // Sanitize original filename and create unique name
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                originalFilename = "unknown_file";
            }
            String safeFilename = originalFilename.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
            String fileName = UUID.randomUUID() + "_" + safeFilename;

            // Full path to save the file
            String filePath = BASE_DIR + folder + "/" + fileName;

            // Debug logging
            System.out.println("---- File Upload Debug ----");
            System.out.println("Original filename: " + originalFilename);
            System.out.println("Safe filename: " + fileName);
            System.out.println("Folder: " + folder);
            System.out.println("Saving to path: " + filePath);
            System.out.println("Parent folder exists: " + new File(filePath).getParentFile().exists());
            System.out.println("----------------------------");

            // Save the file locally
            file.transferTo(new File(filePath));

            // Return URL accessible via Spring's ResourceHandler
            return "/uploads/" + folder + "/" + fileName;

        } catch (IOException e) {
            // Print full stack trace for debugging
            e.printStackTrace();
            throw new RuntimeException("File upload failed", e);
        }
    }
}