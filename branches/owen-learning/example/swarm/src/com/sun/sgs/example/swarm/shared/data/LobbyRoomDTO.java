package com.sun.sgs.example.swarm.shared.data;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

/**
 *
 * @author ok194946
 */
public class LobbyRoomDTO extends RoomDTO implements Serializable
{
    /** The version of the serialized form of this class. */
    private static final long serialVersionUID = 1L;
    
    private List<GameRoomStatsDTO> rooms;
    
    /** Creates a new instance of LobbyRoomDTO */
    public LobbyRoomDTO(int maxPlayers, int currentPlayers)
    {
        super("Lobby", maxPlayers, currentPlayers);
        
        rooms = new ArrayList<GameRoomStatsDTO>();
    }
    
    public List<GameRoomStatsDTO> getGameRooms() { return rooms; }
    
}
