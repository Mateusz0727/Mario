package com.mario.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class GameFileManager {
    private static final DateTimeFormatter REPORT_TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    public Map<String, String> loadConfig(String path) throws IOException {
        Map<String, String> values = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(path, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                String[] parts = trimmed.split("=", 2);
                if (parts.length == 2) {
                    values.put(parts[0].trim(), parts[1].trim());
                }
            }
        }
        return values;
    }



    public GameData loadGameData(String path) throws IOException, ClassNotFoundException {
        File file = new File(path);
        if (!file.exists()) {
            return null;
        }
        try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(file))) {
            return (GameData) inputStream.readObject();
        }
    }

    public String writeReport(String directory, String reportBody) throws IOException {
        File dir = new File(directory);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String fileName = "report-" + LocalDateTime.now().format(REPORT_TIMESTAMP) + ".txt";
        File reportFile = new File(dir, fileName);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(reportFile, StandardCharsets.UTF_8))) {
            writer.write(reportBody);
        }
        return reportFile.getPath();
    }
}
