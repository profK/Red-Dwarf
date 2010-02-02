package com.sun.sgs.example.swarm.shared.data;

import java.util.Map;
import java.util.HashMap;
import java.io.Serializable;

import com.sun.sgs.example.swarm.shared.util.Location;

/**
 *
 * @author ok194946
 */
public class BoardDTO implements Serializable
{
    /** The version of the serialized form of this class. */
    private static final long serialVersionUID = 1L;
    
    /** grid of Map<String, BoardItemDTO> types */
    private Object[][] grid;
    
    /** Creates a new instance of BoardDTO */
    public BoardDTO(int rows, int cols)
    {
        grid = new Object[rows][cols];
        for(int r = 0; r < rows; r++) {
            for(int c = 0; c < cols; c++) {
                grid[r][c] = new HashMap<String, BoardItemDTO>();
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
    
    public Map<String, BoardItemDTO> getBoardItems(Location location)
    {
        return (Map<String, BoardItemDTO>)grid[location.getRow()][location.getCol()];
    }
    public Map<String, BoardItemDTO> getBoardItems(int row, int col)
    {
        return (Map<String, BoardItemDTO>)grid[row][col];
    }
    
}
