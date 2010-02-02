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
import com.sun.sgs.example.swarm.shared.results.BuildingUpdatedResult;
import com.sun.sgs.example.swarm.shared.results.PlayerUpdatedResult;
import com.sun.sgs.example.swarm.server.factories.ResultSender;

/**
 *
 */
public class AttackBoardItemTask implements Task, Serializable
{
    /** The version of the serialized form of this class. */
    private static final long serialVersionUID = 1L;
    
    /** The {@link Logger} for this class. */
    private static final Logger logger =
            Logger.getLogger(AttackBoardItemTask.class.getName());
    
    private ManagedReference<BoardItem> itemRef;
    private ManagedReference<GameRoom> gameRef;
    private Location location;
    private int intensity;
    
    public AttackBoardItemTask(ManagedReference<BoardItem> itemRef,
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
        itemRef.getForUpdate().attack(intensity);
        
        //check to see if item is destroyed
        BoardItem item = itemRef.get();
        if(item.getStrength() <= 0) {
            if(item instanceof Building) {
                gameRef.getForUpdate().destroyBuilding(location);
                logger.log(Level.INFO, "Building at location "+
                        location.getRow()+", "+location.getCol()+
                        " destroyed in room "+gameRef.get().getName());
            }
            else if(item instanceof Player) {
                gameRef.getForUpdate().destroyPlayer((Player)item);
                logger.log(Level.INFO, "Player "+((Player)item).getUid()+" destroyed");
            }
        }
        
        //send update to clients
        if(item instanceof Building)
            ResultSender.sendResult(new BuildingUpdatedResult(location, item.getStrength()), gameRef.get().getPlayerChannel());
        else if(item instanceof Player)
            ResultSender.sendResult(new PlayerUpdatedResult(((Player)item).getUid(), item.getStrength()), gameRef.get().getPlayerChannel());
    }
}
