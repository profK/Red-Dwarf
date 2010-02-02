package com.sun.sgs.example.swarm.server.domain;

import java.io.Serializable;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.Channel;
import com.sun.sgs.app.Delivery;
import com.sun.sgs.app.ClientSession;



/**
 * Represents the base functionality of a Room in the Swarm world.  At a minimum,
 * a Room has a set of Players and a broadcast Channel containing each of the
 * players
 */
public abstract class Room implements ManagedObject, Serializable
{
    /** The version of the serialized form of this class. */
    private static final long serialVersionUID = 1L;
    
    /** room name */
    private String name;
    
    /** maximum number of players that can enter this room */
    private int maxPlayers;
    
    /** current number of players in this room */
    private int currentPlayers;
    
    /** A Channel that each player is joined to when entering the room */
    private ManagedReference<Channel> playerChannelRef;
    
    /**
     * Creates a room with the specified maximum number of players
     * and specified room name
     */
    public Room(String name, int maxPlayers)
    {
        //initialize private variables
        this.name = name;
        this.maxPlayers = maxPlayers;
        this.currentPlayers = 0;
        
        //initialize player channel
        Channel playerChannel = AppContext.getChannelManager().createChannel("PLAYER_CHANNEL_"+name, null, Delivery.RELIABLE);
        playerChannelRef = AppContext.getDataManager().createReference(playerChannel);
    }
    
    /**
     * Add a player to this room
     */
    public boolean joinRoom(Player player, ClientSession client)
    {
        //fail if we are already at the limit
        if(currentPlayers == maxPlayers)
            return false;
        
        //add the player to the channel
        playerChannelRef.getForUpdate().join(client);
        
        currentPlayers++;
        return true;
    }
    
    /**
     * Remove player from this room if it is in the room
     */
    public void leaveRoom(Player player, ClientSession client)
    {
        //remove player from the player channel
        playerChannelRef.getForUpdate().leave(client);
        
        currentPlayers--;
    }
    
    public String getName() { return name; }
    public int getMaxPlayers() { return maxPlayers; }
    public int getCurrentPlayers() { return currentPlayers; }
    
    public Channel getPlayerChannel() { return playerChannelRef.get(); }
    
}
