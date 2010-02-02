package com.sun.sgs.example.swarm.server.domain;

import java.util.List;
import java.util.Map;
import java.util.Iterator;
import java.util.Queue;
import java.util.LinkedList;
import java.io.Serializable;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.DataManager;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.util.ScalableHashMap;

import com.sun.sgs.example.swarm.shared.util.Location;
import com.sun.sgs.example.swarm.shared.util.Direction;



/**
 * Represents a game board in the swarm world.  A board consists of a two-dimensional
 * grid where each node contains an unlimited stack of elements.
 */
public class Board implements ManagedObject, Serializable
{
    /** The version of the serialized form of this class. */
    private static final long serialVersionUID = 1L;
    
    /** grid of ManagedReference<Map<String, ManagedReference<BoardItem>>> types */
    private Object[][] grid;
    
    /** 
     * Creates a new instance of Board with the specified size
     */
    public Board(int rows, int cols)
    {
        DataManager manager = AppContext.getDataManager();
        grid = new Object[rows][cols];
        for(int r = 0; r < rows; r++) {
            for(int c = 0; c < cols; c++) {
                Map<String, ManagedReference<BoardItem>> map = new ScalableHashMap<String, ManagedReference<BoardItem>>();
                grid[r][c] = manager.createReference(map);
            }
        }
    }
    
    public int getRows()
    {
        return grid.length;
    }
    public int getCols()
    {
        return grid[0].length;
    }
    
    public ManagedReference<Map<String, ManagedReference<BoardItem>>> getBoardItems(Location location)
    {
        return (ManagedReference<Map<String, ManagedReference<BoardItem>>>)grid[location.getRow()][location.getCol()];
    }
    public ManagedReference<Map<String, ManagedReference<BoardItem>>> getBoardItems(int row, int col)
    {
        return (ManagedReference<Map<String, ManagedReference<BoardItem>>>)grid[row][col];
    }
    
    public Location getDirectionLocation(Location location, Direction direction)
    {
        Location newLocation = null;
        switch(direction) {
            case UP:
                newLocation = new Location(location.getRow()-1, location.getCol());
                break;
            case DOWN:
                newLocation = new Location(location.getRow()+1, location.getCol());
                break;
            case LEFT:
                newLocation = new Location(location.getRow(), location.getCol()-1);
                break;
            case RIGHT:
                newLocation = new Location(location.getRow(), location.getCol()+1);
                break;
            case CENTER:
                newLocation = location;
                break;
            default:
                newLocation = null;
        }
        
        if(newLocation != null 
                && (newLocation.getRow() >= 0 && newLocation.getRow() < grid.length)
                && (newLocation.getCol() >= 0 && newLocation.getCol() < grid[0].length))
            return newLocation;
        return null;
    }
    
    private Map<String, ManagedReference<BoardItem>> getGridStack(int row, int col)
    {
        return (Map<String, ManagedReference<BoardItem>>)grid[row][col];
    }
    
    public void addPlayer(Location location, Player player)
    {
        BoardItem item = player;
        ManagedReference<BoardItem> playerRef = AppContext.getDataManager().createReference(item);
        getGridStack(location.getRow(), location.getCol()).put(player.getUid(), playerRef);
    }
    
    public void removePlayer(Location location, Player player)
    {
        getGridStack(location.getRow(), location.getCol()).remove(player.getUid());
    }
    

    
}
