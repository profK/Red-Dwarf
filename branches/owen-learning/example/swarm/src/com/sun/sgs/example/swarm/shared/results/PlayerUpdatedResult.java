package com.sun.sgs.example.swarm.shared.results;

import java.io.Serializable;

/**
 *
 * @author ok194946
 */
public class PlayerUpdatedResult implements Result, Serializable
{
    /** The version of the serialized form of this class. */
    private static final long serialVersionUID = 1L;
    
    private String uid;
    private int newStrength;
    
    /** Creates a new instance of PlayerUpdatedResult */
    public PlayerUpdatedResult(String uid, int newStrength)
    {
        this.uid = uid;
        this.newStrength = newStrength;
    }
    
    public String getUid() { return uid; }
    public int getNewStrength() { return newStrength; }
    
}
