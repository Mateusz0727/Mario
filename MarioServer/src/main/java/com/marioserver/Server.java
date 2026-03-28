package com.marioserver;

import com.mario.net.GameData;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Główna pętla serwera autorytatywnego (Host). 
 * Odpowiada wyłącznie za nasłuchiwanie portu 8080 i przekazywanie Sockets do odpowiednich rozgałęzień (Wątków).
 */
public class Server {
    public static final int PORT = 1234;
    
    // Hash map for distinct game room lobbies mapped to a 4 char String code (e.g "X7B1")
    public static ConcurrentHashMap<String, Room> rooms = new ConcurrentHashMap<>();
    
    public static void main(String[] args) {
        System.out.println("Uruchamianie Mario Multiplayer Server (Port: " + PORT + ") ...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Serwer rozpoczął nasłuchiwanie. Oczekiwanie na graczy...");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nowy gracz połączył się z serwerem: " + clientSocket.getInetAddress());
                
                ClientHandler handler = new ClientHandler(clientSocket);
                handler.start();
            }
        } catch (Exception e) {
            System.err.println("Wystąpił błąd krytyczny sieci na Serwerze.");
            e.printStackTrace();
        }
    }

    public static synchronized String generateRoomCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        java.util.Random rnd = new java.util.Random();
        String code;
        do {
            StringBuilder sb = new StringBuilder(4);
            for(int i=0; i<4; i++) sb.append(chars.charAt(rnd.nextInt(chars.length())));
            code = sb.toString();
        } while (rooms.containsKey(code));
        return code;
    }
}
