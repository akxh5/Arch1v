package com.akshansh.organizer;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;

public class FileScanner {

    private final Map<String, String> extensionMap;
    // Maps file hash to first occurrence's path
    private final Map<String, String> hashMap;
    // Maps file hash to list of duplicate file paths (excluding first occurrence)
    private final Map<String, List<String>> duplicateMap;
    private boolean organizeFiles;
    private File baseDirectory;

    public FileScanner() {
        extensionMap = new HashMap<>();
        // Map extensions to folders
        extensionMap.put("jpg", "Images");
        extensionMap.put("jpeg", "Images");
        extensionMap.put("png", "Images");
        extensionMap.put("gif", "Images");

        extensionMap.put("pdf", "Documents");
        extensionMap.put("doc", "Documents");
        extensionMap.put("docx", "Documents");
        extensionMap.put("txt", "Documents");
        extensionMap.put("xlsx", "Documents");
        extensionMap.put("pptx", "Documents");

        extensionMap.put("mp4", "Videos");
        extensionMap.put("mov", "Videos");
        extensionMap.put("avi", "Videos");

        extensionMap.put("mp3", "Audio");
        extensionMap.put("wav", "Audio");

        hashMap = new HashMap<>();
        duplicateMap = new HashMap<>();
        organizeFiles = true; // default to organizing files
        baseDirectory = null;
    }

    public FileScanner(String path) {
        this();
        baseDirectory = new File(path);
        if (!baseDirectory.exists() || !baseDirectory.isDirectory()) {
            System.out.println("❌ Invalid folder path!");
            baseDirectory = null;
        }
    }

    public void organizeFiles() {
        if (baseDirectory == null) {
            System.out.println("❌ Base directory is not set or invalid.");
            return;
        }
        organizeFiles = true;
        System.out.println("✅ Organizing files in folder: " + baseDirectory.getAbsolutePath());
        listFilesRecursively(baseDirectory);
    }

    public String detectDuplicates() {
        if (baseDirectory == null) {
            return "❌ Base directory is not set or invalid.";
        }
        organizeFiles = false;
        hashMap.clear();
        duplicateMap.clear();
        listFilesRecursively(baseDirectory);
        if (!hashMap.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, List<String>> entry : duplicateMap.entrySet()) {
                List<String> duplicates = entry.getValue();
                if (duplicates != null && !duplicates.isEmpty()) {
                    sb.append("Duplicates of ").append(hashMap.get(entry.getKey())).append(":\n");
                    for (String dup : duplicates) {
                        sb.append(" - ").append(dup).append("\n");
                    }
                }
            }
            if (sb.length() == 0) {
                return "No duplicates found.";
            } else {
                return sb.toString();
            }
        } else {
            return "No duplicates found.";
        }
    }

    public void decategorizeFiles() {
        if (baseDirectory == null) {
            System.out.println("❌ Base directory is not set or invalid.");
            return;
        }
        organizeFiles = false;
        System.out.println("✅ Decategorizing files in folder: " + baseDirectory.getAbsolutePath());
        decategorizeFiles(baseDirectory);
    }

    public void scanFolder(String path) {
        File folder = new File(path);

        if (!folder.exists() || !folder.isDirectory()) {
            System.out.println("❌ Invalid folder path!");
            return;
        }

        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("Choose an option:");
            System.out.println("1. Organize files (categorize by type)");
            System.out.println("2. Decategorize files (move all files back to base folder)");
            System.out.println("3. Detect duplicates");
            System.out.print("Enter choice (1/2/3): ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    organizeFiles = true;
                    System.out.println("✅ Organizing files in folder: " + folder.getAbsolutePath());
                    listFilesRecursively(folder);
                    break;
                case "2":
                    organizeFiles = false;
                    System.out.println("✅ Decategorizing files in folder: " + folder.getAbsolutePath());
                    decategorizeFiles(folder);
                    break;
                case "3":
                    organizeFiles = false;
                    System.out.println("✅ Detecting duplicates in folder: " + folder.getAbsolutePath());
                    listFilesRecursively(folder);
                    if (!hashMap.isEmpty()) {
                        System.out.print("Do you want to delete duplicate files? (y/n): ");
                        String deleteChoice = scanner.nextLine();
                        if (deleteChoice.equalsIgnoreCase("y")) {
                            deleteDuplicates();
                        }
                    } else {
                        System.out.println("No duplicates found.");
                    }
                    break;
                default:
                    System.out.println("❌ Invalid choice!");
                    break;
            }
        }
    }

    private void listFilesRecursively(File dir) {
        File[] files = dir.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                // Skip folders that are categorized folders when decategorizing or detecting duplicates
                if (!organizeFiles && extensionMap.containsValue(file.getName())) {
                    continue;
                }
                listFilesRecursively(file); // recursion
            } else {
                try {
                    String fileHash = computeSHA256(file.toPath());
                    if (hashMap.containsKey(fileHash)) {
                        System.out.println("⚠️ Duplicate detected: " + file.getAbsolutePath() + " (duplicate of " + hashMap.get(fileHash) + ")");
                        // Store duplicate for deletion
                        duplicateMap.computeIfAbsent(fileHash, k -> new ArrayList<>()).add(file.getAbsolutePath());
                        continue;
                    } else {
                        hashMap.put(fileHash, file.getAbsolutePath());
                        // No duplicates yet, so no need to add to duplicateMap
                    }
                } catch (IOException | NoSuchAlgorithmException e) {
                    System.out.println("❌ Failed to compute hash for " + file.getAbsolutePath() + ": " + e.getMessage());
                    // Proceed with organizing the file even if hash computation fails
                }
                System.out.println("📄 " + file.getAbsolutePath());
                if (organizeFiles) {
                    organizeFile(file, dir);
                }
            }
        }
    }

    private void organizeFile(File file, File baseDir) {
        String fileName = file.getName();
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex == -1) return; // no extension

        String ext = fileName.substring(dotIndex + 1).toLowerCase();
        String folderName = extensionMap.get(ext);
        if (folderName == null) return; // unknown type

        File parentDir = file.getParentFile();
        if (parentDir != null && parentDir.getName().equals(folderName)) {
            // File is already inside the correct categorized folder, skip moving
            return;
        }

        File targetDir = new File(baseDir, folderName);
        if (!targetDir.exists()) {
            targetDir.mkdir();
        }

        Path sourcePath = file.toPath();
        Path targetPath = new File(targetDir, fileName).toPath();

        try {
            Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("✅ Moved " + fileName + " → " + folderName);
        } catch (IOException e) {
            System.out.println("❌ Failed to move " + fileName + ": " + e.getMessage());
        }
    }

    private void decategorizeFiles(File baseDir) {
        // Recursively find and flatten categorized folders
        for (String folderName : extensionMap.values()) {
            File[] foundDirs = findDirectoriesByName(baseDir, folderName);
            for (File categoryDir : foundDirs) {
                if (categoryDir.exists() && categoryDir.isDirectory()) {
                    File[] files = categoryDir.listFiles();
                    if (files == null) continue;
                    for (File file : files) {
                        if (file.isFile()) {
                            Path sourcePath = file.toPath();
                            Path targetPath = new File(baseDir, file.getName()).toPath();
                            try {
                                Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                                System.out.println("✅ Moved " + file.getName() + " → " + baseDir.getName());
                            } catch (IOException e) {
                                System.out.println("❌ Failed to move " + file.getName() + ": " + e.getMessage());
                            }
                        }
                    }
                    // After moving files, try deleting the empty category directory
                    if (categoryDir.listFiles() != null && categoryDir.listFiles().length == 0) {
                        if (categoryDir.delete()) {
                            System.out.println("✅ Deleted empty folder: " + categoryDir.getAbsolutePath());
                        }
                    }
                }
            }
        }
    }

    private File[] findDirectoriesByName(File baseDir, String targetName) {
        List<File> result = new ArrayList<>();
        findDirectoriesByNameHelper(baseDir, targetName, result);
        return result.toArray(new File[0]);
    }

    private void findDirectoriesByNameHelper(File dir, String targetName, List<File> result) {
        if (dir == null || !dir.exists() || !dir.isDirectory()) return;
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (file.isDirectory()) {
                if (file.getName().equals(targetName)) {
                    result.add(file);
                }
                findDirectoriesByNameHelper(file, targetName, result);
            }
        }
    }

    private void deleteDuplicates() {
        boolean anyDeleted = false;
        for (Map.Entry<String, List<String>> entry : duplicateMap.entrySet()) {
            List<String> duplicates = entry.getValue();
            if (duplicates == null) continue;
            for (String dupPath : duplicates) {
                File dupFile = new File(dupPath);
                if (dupFile.exists()) {
                    boolean deleted = dupFile.delete();
                    if (deleted) {
                        System.out.println("✅ Deleted duplicate: " + dupPath);
                        anyDeleted = true;
                    } else {
                        System.out.println("❌ Failed to delete duplicate: " + dupPath);
                    }
                } else {
                    System.out.println("❌ Duplicate file not found: " + dupPath);
                }
            }
        }
        if (!anyDeleted) {
            System.out.println("No duplicate files were deleted.");
        }
    }

    private String computeSHA256(Path path) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream is = Files.newInputStream(path)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
        }
        byte[] hashBytes = digest.digest();

        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}