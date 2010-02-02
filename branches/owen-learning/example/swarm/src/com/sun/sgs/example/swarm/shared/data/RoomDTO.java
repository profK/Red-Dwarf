/*
 * RoomDTO.java
 *
 * Created on May 5, 2008, 8:54 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.sgs.example.swarm.shared.data;

import java.io.Serializable;

/**
 *
 * @author ok194946
 */
public abstract class RoomDTO implements Serializable
{
    /** The version of the serialized form of this class. */
    private static final long serialVersionUID = 1L;
    
    /** room name */
    private String name;
    
    /** maximum number of players that can enter this room */
    private int maxPlayers;
    
    /** current number of players in this room */
    private int currentPlayers;
    
    /**
     * Creates a room with the specified maximum number of players
     * and specified room name
     */
    public RoomDTO(String name, int maxPlayers, int currentPlayers)
    {
        //initialize private variables
        this.name = name;
        this.maxPlayers = maxPlayers;
        this.currentPlayers = currentPlayers;
    }
    
    public String getName() { return name; }
    public int getMaxPlayers() { return maxPlayers; }
    public int getCurrentPlayers() { return currentPlayers; }
    
}
