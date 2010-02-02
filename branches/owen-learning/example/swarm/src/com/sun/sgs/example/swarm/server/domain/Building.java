package com.sun.sgs.example.swarm.server.domain;

import java.io.Serializable;

import com.sun.sgs.example.swarm.shared.util.Team;
import com.sun.sgs.example.swarm.server.Constants;

/**
 * Represents a building entity for a board.
 */
public class Building implements BoardItem, Serializable
{
    /** The version of the serialized form of this class. */
    private static final long serialVersionUID = 1L;
    
    /** The current strength of the building */
    private int strength;
    
    private Team team;

    /**
     * Creates a new instance of Building with the given intensity
     */
    public Building(int intensity, Team team)
    {
        strength = intensity;
        this.team = team;
    }
    
    public void attack(int intensity)
    {
        if(strength < Constants.MAX_BUILDING_STRENGTH) {
            strength -= intensity;
            if (strength < 0)
                strength = 0;
        }
    }
    
    public void repair(int intensity)
    {
        strength += intensity;
        if(strength >= Constants.MAX_BUILDING_STRENGTH) {
            strength = Constants.MAX_BUILDING_STRENGTH;
        }
            
    }
    
    public int getStrength() { return strength; }
    public Team getTeam() { return team; }
    
}