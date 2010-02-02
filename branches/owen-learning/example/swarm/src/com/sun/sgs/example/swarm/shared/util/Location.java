package com.sun.sgs.example.swarm.shared.util;

import java.io.Serializable;

/**
 * Represents a coordinate on the game board
 */
public class Location implements Serializable
{
    /** The version of the serialized form of this class. */
    private static final long serialVersionUID = 1L;
    
    private int row;
    private int col;
    
    /** Creates a new instance of Location */
    public Location(int row, int col)
    {
        this.row = row;
        this.col = col;
    }
    
    public int getRow() { return row; }
    public int getCol() { return col; }
    
    public boolean equals(Object o)
    {
        if(o instanceof Location) {
            Location other = (Location)o;
            return row == other.getRow() && col == other.getCol();
        }
        
        return false;
    }
    
}
