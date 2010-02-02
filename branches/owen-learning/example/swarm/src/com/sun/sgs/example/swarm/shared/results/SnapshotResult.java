package com.sun.sgs.example.swarm.shared.results;

import java.io.Serializable;

import com.sun.sgs.example.swarm.shared.data.RoomDTO;

/**
 *
 * @author ok194946
 */
public class SnapshotResult implements Result, Serializable
{
    /** The version of the serialized form of this class. */
    private static final long serialVersionUID = 1L;
    
    private RoomDTO room;
    
    /** Creates a new instance of SnapshotResult */
    public SnapshotResult(RoomDTO room)
    {
        this.room = room;
    }
    
    public RoomDTO getRoom() { return room; }
    
}
