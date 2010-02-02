package com.sun.sgs.example.swarm.shared.results;

import java.io.Serializable;

import com.sun.sgs.example.swarm.shared.util.Location;

/**
 *
 * @author ok194946
 */
public class BuildingUpdatedResult implements Result, Serializable
{
    /** The version of the serialized form of this class. */
    private static final long serialVersionUID = 1L;
    
    private Location location;
    private int newStrength;
    
    /** Creates a new instance of BuildingUpdatedResult */
    public BuildingUpdatedResult(Location location, int newStrength)
    {
        this.location = location;
        this.newStrength = newStrength;
    }
    
    public Location getLocation() { return location; }
    public int getNewStrength() { return newStrength; }
    
}
