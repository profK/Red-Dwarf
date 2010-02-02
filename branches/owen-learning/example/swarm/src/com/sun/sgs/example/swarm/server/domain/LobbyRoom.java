package com.sun.sgs.example.swarm.server.domain;

import java.util.List;
import java.util.ArrayList;
import java.io.Serializable;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedReference;

/**
 * Represents the main lobby of the Swarm app
 */
public class LobbyRoom extends Room implements Serializable
{
    /** The version of the serialized form of this class. */
    private static final long serialVersionUID = 1L;
    
    private List<ManagedReference<GameRoom>> rooms;
    
    /** 
     * Creates a new instance of Lobby
     * with the specified number of available rooms
     */
    public LobbyRoom(int maxTotalPlayers, int totalRooms, int maxRoomPlayers, int roomRows, int roomCols)
    {
        super("Lobby", maxTotalPlayers);
        
        rooms = new ArrayList<ManagedReference<GameRoom>>(totalRooms);
        for(int i = 0; i < totalRooms; i++) {
            GameRoom r = new GameRoom("Room"+String.valueOf(i), maxRoomPlayers, roomRows, roomCols);
            rooms.add(AppContext.getDataManager().createReference(r));
        }
    }
    
    public ManagedReference<GameRoom> getRoom(int index) { return rooms.get(index); }
    public int getTotalRooms() { return rooms.size(); }
    
}
