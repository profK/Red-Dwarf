package com.sun.sgs.example.swarm.shared.data;

import com.sun.sgs.example.swarm.shared.util.Team;

/**
 * Represents an Item that can be placed on a board in the Swarm world
 */
public interface BoardItemDTO
{
    /**
     * Return the current strength of the board item.
     */
    public int getStrength();
    
    /**
     * Return the Team that this board item is associated with
     */
    public Team getTeam();
}

