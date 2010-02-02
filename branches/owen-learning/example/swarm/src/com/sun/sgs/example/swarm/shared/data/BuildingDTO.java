package com.sun.sgs.example.swarm.shared.data;

import java.io.Serializable;

import com.sun.sgs.example.swarm.shared.util.Team;

/**
 * Represents a building entity for a board.
 */
public class BuildingDTO implements BoardItemDTO, Serializable
{
    /** The version of the serialized form of this class. */
    private static final long serialVersionUID = 1L;
    
    /** The current strength of the building */
    private int strength;
    
    private Team team;

    /**
     * Creates a new instance of Building with the given intensity
     */
    public BuildingDTO(int intensity, Team team)
    {
        strength = intensity;
        this.team = team;
    }
    
    public int getStrength() { return strength; }
    public Team getTeam() { return team; }
    
}
