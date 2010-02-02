package com.sun.sgs.example.swarm.server.domain;

import com.sun.sgs.app.ManagedObject;

import com.sun.sgs.example.swarm.shared.util.Team;

/**
 * Represents an Item that can be placed on a board in the Swarm world
 */
public interface BoardItem extends ManagedObject 
{
    /**
     * Attacks this BoardItem with the intention of damaging it with
     * the given intensity
     */
    public void attack(int intensity);
    
    /**
     * Repairs this BoardItem with the specified intensity
     */
    public void repair(int intensity);
    
    /**
     * Return the current strength of the board item.
     */
    public int getStrength();
    
    /**
     * Return the Team that this board item is associated with
     */
    public Team getTeam();
}
