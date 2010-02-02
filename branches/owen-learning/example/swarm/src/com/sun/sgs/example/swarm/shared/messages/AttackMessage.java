package com.sun.sgs.example.swarm.shared.messages;

import java.io.Serializable;

import com.sun.sgs.example.swarm.shared.util.Direction;

/**
 *
 */
public class AttackMessage implements Message, Serializable
{
    /** The version of the serialized form of this class. */
    private static final long serialVersionUID = 1L;
    
    private Direction direction;
    
    /** Creates a new instance of AttackMessage */
    public AttackMessage(Direction direction)
    {
        this.direction = direction;
    }
    
    public Direction getDirection() { return direction; }
    
}
