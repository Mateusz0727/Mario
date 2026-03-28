package com.marioserver;

import java.util.ArrayList;
import java.util.List;

public class Room {
    public String roomCode;
    public List<ClientHandler> clients;
    public boolean isGameStarted;

    public Room(String roomCode) {
        this.roomCode = roomCode;
        this.clients = new ArrayList<>();
        this.isGameStarted = false;
    }

    public synchronized void addClient(ClientHandler client) {
        clients.add(client);
    }

    public synchronized void removeClient(ClientHandler client) {
        clients.remove(client);
    }

    public synchronized List<ClientHandler> getOtherClients(ClientHandler current) {
        List<ClientHandler> others = new ArrayList<>();
        for (ClientHandler c : clients) {
            if (c != current) {
                others.add(c);
            }
        }
        return others;
    }

    public synchronized void broadcast(Object obj) {
        for (ClientHandler client : clients) {
            client.sendObject(obj);
        }
    }
}
