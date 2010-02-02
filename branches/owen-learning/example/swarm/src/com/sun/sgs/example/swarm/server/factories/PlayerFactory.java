package com.sun.sgs.example.swarm.server.factories;

import com.sun.sgs.example.swarm.server.domain.Player;
import com.sun.sgs.example.swarm.server.domain.AttackPlayer;
import com.sun.sgs.example.swarm.server.domain.BuildPlayer;
import com.sun.sgs.example.swarm.shared.util.Team;
import com.sun.sgs.example.swarm.shared.util.PlayerType;

/**
 * factory to generate player entities
 */
public class PlayerFactory
{
    
    public static Player generatePlayer(PlayerType type, Team team, String uid, int strength)
    {
        switch(type)
        {
            case ATTACKER:
                return new AttackPlayer(uid, strength, team);
            case BUILDER:
                return new BuildPlayer(uid, strength, team);
            default:
                return null;
        }
    }
    
}
