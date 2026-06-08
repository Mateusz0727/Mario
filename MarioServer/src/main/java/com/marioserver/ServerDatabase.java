package com.marioserver;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class ServerDatabase {
    private static final String DB_PATH = "database.txt";

    public static synchronized String loadData(String playerId) {
        File file = new File(DB_PATH);
        if (!file.exists()) {
            return null;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";", 2);
                if (parts.length == 2 && parts[0].equals(playerId)) {
                    return parts[1]; // zwraca ciąg: np. "15;3;3;Level1"
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static synchronized void saveData(String playerId, String payload) {
        File file = new File(DB_PATH);
        Map<String, String> records = new HashMap<>();

        // Odczytaj istniejące rekordy
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(";", 2);
                    if (parts.length == 2) {
                        records.put(parts[0], parts[1]);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Zaktualizuj rekord
        records.put(playerId, payload);

        // Zapisz wszystko z powrotem
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
            for (Map.Entry<String, String> entry : records.entrySet()) {
                writer.write(entry.getKey() + ";" + entry.getValue());
                writer.newLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
