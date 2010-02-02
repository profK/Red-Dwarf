package com.sun.sgs.example.swarm.shared.data;

import java.io.Serializable;

import com.sun.sgs.example.swarm.shared.util.Team;
import com.sun.sgs.example.swarm.shared.util.PlayerType;

/**
 * A Player is an object representing a player's state that can be placed
 * onto a Board
 */
public abstract class PlayerDTO implements BoardItemDTO, Serializable
{
    /** The version of the serialized form of this class. */
    private static final long serialVersionUID = 1L;
    
    /** The uid of the player */
    private String uid;
      
    /** The current strength of the player */
    private int strength;
    
    /** The team of the player */
    private Team team;

    /**
     * Creates a new instance of Player with the given initial strength
     */
    public PlayerDTO(String uid, int strength, Team team)
    {
        this.uid = uid;
        this.strength = strength;
        this.team = team;
    }

    public String getUid() { return uid; }
    public int getStrength() { return strength; }
    public Team getTeam() { return team; }
    public abstract PlayerType getType();
    
}

