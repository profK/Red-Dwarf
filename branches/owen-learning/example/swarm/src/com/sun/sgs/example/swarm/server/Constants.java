package com.sun.sgs.example.swarm.server;

import com.sun.sgs.example.swarm.shared.util.Location;

/**
 * Set of constants for use in the Swarm world
 */
public class Constants
{
    
    public static final String USER_LIST = "USER_LIST";
    public static final String LOBBY = "LOBBY";
    
    public static final Location DEFAULT_PLAYER_LOCATION = new Location(0,0);
    
    //default strength of a player logging in
    public static final int DEFAULT_PLAYER_STRENGTH = 50;
    //once a building reaches this strength, it can no longer be dammaged
    public static final int MAX_BUILDING_STRENGTH = 1000;
    public static final int MAX_PLAYER_STRENGTH = 1000;
    //key used to locate the building on a grid stack
    public static final String BUILDING_KEY = "BUILDING";
    
}
