package com.sun.sgs.example.swarm.shared.data;

import java.io.Serializable;

import com.sun.sgs.example.swarm.shared.util.Team;
import com.sun.sgs.example.swarm.shared.util.PlayerType;


/**
 * An attacker
 */
public class AttackPlayerDTO extends PlayerDTO implements Serializable
{
    /** The version of the serialized form of this class. */
    private static final long serialVersionUID = 1L;
    
    /** Creates a new instance of AttackPlayer */
    public AttackPlayerDTO(String uid, int strength, Team team)
    {
        super(uid, strength, team);
    }
    
    public PlayerType getType() { return PlayerType.ATTACKER; }
    
}

