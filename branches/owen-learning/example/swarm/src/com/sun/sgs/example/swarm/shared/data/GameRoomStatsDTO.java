package com.sun.sgs.example.swarm.shared.data;

import java.io.Serializable;

/**
 *
 * @author ok194946
 */
public class GameRoomStatsDTO implements Serializable
{
    /** The version of the serialized form of this class. */
    private static final long serialVersionUID = 1L;
    
    /** room name */
    private String name;
    
    /** maximum number of players that can enter this room */
    private int maxPlayers;
    
    /** current number of players in this room */
    private int currentPlayers;
    
    
    /** Creates a new instance of GameRoomStatsDTO */
    public GameRoomStatsDTO(String name, int maxPlayers, int currentPlayers)
    {
        this.name = name;
        this.maxPlayers = maxPlayers;
        this.currentPlayers = currentPlayers;
    }
    
    public String getName() { return name; }
    public int getMaxPlayers() { return maxPlayers; }
    public int getCurrentPlayers() { return currentPlayers; }
    
}
