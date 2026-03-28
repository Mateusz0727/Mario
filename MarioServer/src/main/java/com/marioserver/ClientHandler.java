package com.marioserver;

import com.mario.net.GameData;
import com.mario.net.Packet;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

/**
 * Wątek dedykowany połączonemu graczowi. Utrzymuje Handshake.
 */
public class ClientHandler extends Thread {
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    public String playerId;
    private Room currentRoom;
    
    public ClientHandler(Socket socket) {
        this.socket = socket;
    }
    
    public synchronized void sendObject(Object obj) {
        try {
            out.reset();
            out.writeObject(obj);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            // output NAJPIERW zeby uniknac dead-locka handshake javy
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            
            while (true) {
                Object obj = in.readObject(); 
                
                if (obj instanceof Packet) {
                    Packet packet = (Packet) obj;

                    if (packet.type == Packet.Type.CREATE_ROOM) {
                        String code = Server.generateRoomCode();
                        currentRoom = new Room(code);
                        currentRoom.addClient(this);
                        Server.rooms.put(code, currentRoom);
                        
                        Packet response = Packet.joinRoom(code); // Return code to creator
                        sendObject(response);
                        System.out.println("Utworzono Pokój: " + code);
                    } 
                    else if (packet.type == Packet.Type.JOIN_ROOM) {
                        String code = packet.roomCode;
                        if (Server.rooms.containsKey(code)) {
                            currentRoom = Server.rooms.get(code);
                            if (currentRoom.clients.size() >= 2) {
                                sendObject(Packet.error("Room is full!"));
                            } else {
                                currentRoom.addClient(this);
                                System.out.println("Klient wszedł do Pokoju: " + code);
                                
                                // Gdy wejdzie dwóch, uruchom gre po obu stronach
                                currentRoom.isGameStarted = true;
                                currentRoom.broadcast(Packet.start(code));
                            }
                        } else {
                            sendObject(Packet.error("Room not found!"));
                        }
                    }
                    else if (packet.type == Packet.Type.UPDATE) {
                        if (currentRoom != null && currentRoom.isGameStarted) {
                            this.playerId = packet.data.playerId;
                            List<ClientHandler> others = currentRoom.getOtherClients(this);
                            for(ClientHandler other : others) {
                                other.sendObject(packet);
                            }
                        }
                    }
                    else if (packet.type == Packet.Type.TILE_SYNC) {
                        if (currentRoom != null) {
                            List<ClientHandler> others = currentRoom.getOtherClients(this);
                            for (ClientHandler other : others) {
                                other.sendObject(packet);
                            }
                        }
                    }
                    else if (packet.type == Packet.Type.ENTITY_SYNC) {
                        if (currentRoom != null) {
                            List<ClientHandler> others = currentRoom.getOtherClients(this);
                            for (ClientHandler other : others) {
                                other.sendObject(packet);
                            }
                        }
                    }
                }
            }
        } catch (java.io.EOFException e) {
            System.out.println("Gracz odłączył się od sieci.");
        } catch (Exception e) {
            System.out.println("Utracono połączenie z graczem: " + e.getMessage());
        } finally {
            if (currentRoom != null) {
                currentRoom.removeClient(this);
                System.out.println("Gracz opuścił pokój " + currentRoom.roomCode);
                if (currentRoom.clients.isEmpty()) {
                    Server.rooms.remove(currentRoom.roomCode);
                    System.out.println("Zniszczono pusty pokój: " + currentRoom.roomCode);
                }
            }
            try { socket.close(); } catch (Exception ignored) {}
        }
    }
}
