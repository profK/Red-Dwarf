package com.sun.sgs.example.swarm.shared.results;

import java.io.Serializable;

import com.sun.sgs.example.swarm.shared.util.Location;

/**
 *
 * @author ok194946
 */
public class PlayerMovedResult implements Result, Serializable
{
    /** The version of the serialized form of this class. */
    private static final long serialVersionUID = 1L;
    
    private String uid;
    private Location newLocation;
    
    /** Creates a new instance of PlayerMovedResult */
    public PlayerMovedResult(String uid, Location newLocation)
    {
        this.uid = uid;
        this.newLocation = newLocation;
    }
    
    public String getUid() { return uid; }
    public Location getNewLocation() { return newLocation; }
    
}
