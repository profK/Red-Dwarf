package com.sun.sgs.example.swarm.server.domain;

import java.io.Serializable;

import com.sun.sgs.example.swarm.shared.util.Team;
import com.sun.sgs.example.swarm.shared.util.PlayerType;

/**
 * A builder
 */
public class BuildPlayer extends Player implements Serializable
{
    /** The version of the serialized form of this class. */
    private static final long serialVersionUID = 1L;
    
    /** Creates a new instance of AttackPlayer */
    public BuildPlayer(String uid, int strength, Team team)
    {
        super(uid, strength, team);
    }
    
    public PlayerType getType() { return PlayerType.BUILDER; }
    
}
