package com.sun.sgs.example.swarm.server.tasks;

import java.io.Serializable;
import java.util.logging.Logger;
import java.util.logging.Level;

import com.sun.sgs.app.Task;
import com.sun.sgs.app.ManagedReference;

import com.sun.sgs.example.swarm.server.domain.BoardItem;
import com.sun.sgs.example.swarm.server.domain.GameRoom;
import com.sun.sgs.example.swarm.server.domain.Building;
import com.sun.sgs.example.swarm.server.domain.Player;
import com.sun.sgs.example.swarm.shared.util.Location;
import com.sun.sgs.example.swarm.server.factories.ResultSender;
import com.sun.sgs.example.swarm.shared.results.BuildingUpdatedResult;
import com.sun.sgs.example.swarm.shared.results.PlayerUpdatedResult;

/**
 *
 * @author owen
 */
public class RepairBoardItemTask implements Task, Serializable
{
    /** The version of the serialized form of this class. */
    private static final long serialVersionUID = 1L;
    
    /** The {@link Logger} for this class. */
    private static final Logger logger =
            Logger.getLogger(RepairBoardItemTask.class.getName());
    
    private ManagedReference<BoardItem> itemRef;
    private ManagedReference<GameRoom> gameRef;
    private Location location;
    private int intensity;
    
    public RepairBoardItemTask(ManagedReference<BoardItem> itemRef, 
            ManagedReference<GameRoom> gameRef,
            Location location,
            int intensity)
    {
        this.itemRef = itemRef;
        this.gameRef = gameRef;
        this.location = location;
        this.intensity = intensity;
    }
    
    public void run()
    {
        itemRef.getForUpdate().repair(intensity);
        
        //send update to clients
        BoardItem item = itemRef.get();
        if(item instanceof Building)
            ResultSender.sendResult(new BuildingUpdatedResult(location, item.getStrength()), gameRef.get().getPlayerChannel());
        else if(item instanceof Player)
            ResultSender.sendResult(new PlayerUpdatedResult(((Player)item).getUid(), item.getStrength()), gameRef.get().getPlayerChannel());
    }
}
