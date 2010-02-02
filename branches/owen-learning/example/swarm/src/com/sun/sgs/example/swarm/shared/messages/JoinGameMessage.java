package com.sun.sgs.example.swarm.shared.messages;

import java.io.Serializable;

/**
 *
 * @author ok194946
 */
public class JoinGameMessage implements Message, Serializable
{
    /** The version of the serialized form of this class. */
    private static final long serialVersionUID = 1L;
    
    private int room;
    
    /** Creates a new instance of JoinGameMessage */
    public JoinGameMessage(int room)
    {
        this.room = room;
    }
    
    public int getRoom() { return room; }
    
}
