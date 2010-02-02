package com.sun.sgs.example.swarm.server.domain;

import java.io.Serializable;

import com.sun.sgs.example.swarm.shared.util.Team;
import com.sun.sgs.example.swarm.shared.util.PlayerType;
import com.sun.sgs.example.swarm.server.Constants;

/**
 * A Player is an object representing a player's state that can be placed
 * onto a Board
 */
public abstract class Player implements BoardItem, Serializable
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
    public Player(String uid, int strength, Team team)
    {
        this.uid = uid;
        this.strength = strength;
        this.team = team;
    }
    
    public void attack(int intensity)
    {
        strength -= intensity;
        if(strength <= 0)
            strength = 0;
    }
    
    public void repair(int intensity)
    {
        strength += intensity;
        if(strength >= Constants.MAX_PLAYER_STRENGTH)
            strength = Constants.MAX_PLAYER_STRENGTH;
    }

    public String getUid() { return uid; }
    public int getStrength() { return strength; }
    public Team getTeam() { return team; }
    public abstract PlayerType getType();
    
}
