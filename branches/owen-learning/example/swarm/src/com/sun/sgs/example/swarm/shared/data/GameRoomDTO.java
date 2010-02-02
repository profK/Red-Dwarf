package com.sun.sgs.example.swarm.shared.data;

import java.io.Serializable;
import java.util.Map;

import com.sun.sgs.example.swarm.shared.util.Location;

/**
 *
 * @author ok194946
 */
public class GameRoomDTO extends RoomDTO implements Serializable
{
    /** The version of the serialized form of this class. */
    private static final long serialVersionUID = 1L;
    
    /** The playing board in the room */
    private BoardDTO board;
    
    /** Map of player ids to locations on the game board */
    private Map<String, Location> playerLocations;
    
    /** Creates a new instance of GameRoomDTO */
    public GameRoomDTO(String name, int maxPlayers, int currentPlayers, BoardDTO board, Map<String, Location> playerLocations)
    {
        super(name, maxPlayers, currentPlayers);
        
        this.board = board;
        this.playerLocations = playerLocations;
    }
    
    public BoardDTO getBoard() { return board; }
    public Map<String, Location> getPlayerLocations() { return playerLocations; }
    
}
