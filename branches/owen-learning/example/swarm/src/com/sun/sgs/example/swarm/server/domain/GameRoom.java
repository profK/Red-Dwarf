package com.sun.sgs.example.swarm.server.domain;

import java.util.Map;
import java.util.HashMap;
import java.io.Serializable;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.ClientSession;

import com.sun.sgs.example.swarm.server.Constants;
import com.sun.sgs.example.swarm.shared.util.Location;
import com.sun.sgs.example.swarm.shared.util.Direction;

/**
 * Represents a Game room in the Swarm world.
 */
public class GameRoom extends Room implements Serializable
{
    /** The version of the serialized form of this class. */
    private static final long serialVersionUID = 1L;
    
    /** The playing board in the room */
    private ManagedReference<Board> boardRef;
    
    /** Map of player ids to locations on the game board */
    private Map<String, Location> playerLocations;
    
    /** Creates a new instance of GameRoom */
    public GameRoom(String name, int maxPlayers, int boardRows, int boardCols)
    {
        super(name, maxPlayers);
        
        Board board = new Board(boardRows, boardCols);
        boardRef = AppContext.getDataManager().createReference(board);
        
        playerLocations = new HashMap<String, Location>();
    }
    
    public ManagedReference<Board> getBoardRef() { return boardRef; }
    public Map<String, Location> getPlayerLocations() { return playerLocations; }
    
    public ManagedReference<Map<String, ManagedReference<BoardItem>>> getBoardItems(Location location)
    {
        return boardRef.get().getBoardItems(location);
    }
    
    public void destroyPlayer(Player player)
    {
        Location location = playerLocations.get(player.getUid());
        getBoardItems(location).getForUpdate().remove(player.getUid());
    }
    
    public void destroyBuilding(Location location)
    {
        getBoardItems(location).getForUpdate().remove(Constants.BUILDING_KEY);
    }
    
    public Location getPlayerLocation(Player player)
    {
        return playerLocations.get(player.getUid());
    }
    
    public Location getDirectionLocation(Location location, Direction direction)
    {
        return boardRef.get().getDirectionLocation(location, direction);
    }
    
    /**
     * Override so the player is added to the game room board and location map
     */
    public boolean joinRoom(Player player, ClientSession client)
    {
        //join the room using the superclass method
        if(!super.joinRoom(player, client))
            return false;
        
        playerLocations.put(player.getUid(), Constants.DEFAULT_PLAYER_LOCATION);
        boardRef.get().addPlayer(Constants.DEFAULT_PLAYER_LOCATION, player);
        
        return true;
    }
    
    /**
     * Override so the player is removed from the game room board and location map
     */
    public void leaveRoom(Player player, ClientSession client)
    {
        //remove player from board
        Location playerLocation = playerLocations.get(player.getUid());
        boardRef.get().removePlayer(playerLocation, player);
        
        //leave the room using the superclass method
        super.leaveRoom(player, client);
    }
    
}
