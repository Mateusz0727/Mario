package com.mario.net;

import java.io.Serializable;

/**
 * Zintegrowana paczka sieciowa. Potrafi obsługiwać standardowe koordynaty gry (GameData) 
 * lub rozkazy podłączenia/tworzenia pokoju (Handshake komendy).
 */
public class Packet implements Serializable {
    private static final long serialVersionUID = 2L;

    public enum Type {
        CREATE_ROOM,
        JOIN_ROOM,
        START_GAME,
        ERROR,
        UPDATE,
        TILE_SYNC,
        ENTITY_SYNC
    }

    public Type type;
    public String roomCode;
    public String message;
    public GameData data;
    
    // Fields for tile synchronization
    public int tx, ty;
    public boolean tileRemoved;
    public boolean tileActivated;

    // Konstruktor do tworzenia pokojów
    public static Packet createRoom() {
        Packet p = new Packet();
        p.type = Type.CREATE_ROOM;
        return p;
    }

    // Konstruktor do logowania
    public static Packet joinRoom(String roomCode) {
        Packet p = new Packet();
        p.type = Type.JOIN_ROOM;
        p.roomCode = roomCode;
        return p;
    }

    // Konstruktor Update Physics
    public static Packet update(String roomCode, GameData data) {
        Packet p = new Packet();
        p.type = Type.UPDATE;
        p.roomCode = roomCode;
        p.data = data;
        return p;
    }

    // Konstruktor odpowiedzi Serwera (Np. błędy)
    public static Packet error(String message) {
        Packet p = new Packet();
        p.type = Type.ERROR;
        p.message = message;
        return p;
    }

    // Konstruktor Start gry z kodem
    public static Packet start(String roomCode) {
        Packet p = new Packet();
        p.type = Type.START_GAME;
        p.roomCode = roomCode;
        return p;
    }

    // Konstruktor synchronizacji kafelków
    public static Packet tileSync(String roomCode, int tx, int ty, boolean removed, boolean activated) {
        Packet p = new Packet();
        p.type = Type.TILE_SYNC;
        p.roomCode = roomCode;
        p.tx = tx;
        p.ty = ty;
        p.tileRemoved = removed;
        p.tileActivated = activated;
        return p;
    }

    // Konstruktor synchronizacji encji (Goomby, Grzyby)
    public static Packet entitySync(String roomCode, int ex, int ey, boolean removed) {
        Packet p = new Packet();
        p.type = Type.ENTITY_SYNC;
        p.roomCode = roomCode;
        p.tx = ex; // Używamy tx/ty dla spójności i oszczędności pól
        p.ty = ey;
        p.tileRemoved = removed; // reużywamy pola
        return p;
    }
}
