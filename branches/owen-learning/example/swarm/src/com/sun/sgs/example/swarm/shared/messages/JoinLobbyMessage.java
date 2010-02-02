package com.sun.sgs.example.swarm.shared.messages;

import java.io.Serializable;

import com.sun.sgs.example.swarm.shared.util.Team;
import com.sun.sgs.example.swarm.shared.util.PlayerType;

/**
 * Message to join the lobby
 */
public class JoinLobbyMessage implements Message, Serializable
{
    /** The version of the serialized form of this class. */
    private static final long serialVersionUID = 1L;
    
    private Team team;
    private PlayerType playerType;
    
    /** Creates a new instance of JoinLobbyMessage */
    public JoinLobbyMessage(Team team, PlayerType playerType)
    {
        this.team = team;
        this.playerType = playerType;
    }
    
    public Team getTeam() { return team; }
    public PlayerType getPlayerType() { return playerType; }
    
}
